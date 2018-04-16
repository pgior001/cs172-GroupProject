package com.web.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

//core of the crawler containing functions to call to run the crawler
public class CrawlerThread extends Thread {
	private int threadNumber;
    private static Map<String, ArrayList<String>> crawlPermissions = new HashMap<String, ArrayList<String>>();

    // all urls in this queue are unique and should be processed
    final private static Queue<String> urlsToCrawl = new LinkedList<>(CrawlerMain.rootPages);
    // keeps track of all pages that are in urlsToCrawl (about to be processed) or pages already processed
    final private static Set<String> seenPages = new HashSet<>(CrawlerMain.rootPages);

	
	public CrawlerThread(int number) {
		threadNumber = number;
	}
	
	public void run() {
	    crawl();
    }

    private void crawl() {
	    for (String url = getNextUrl(); url != null; url = getNextUrl()) {
            try {

                Document document = Jsoup.connect(url).get();
                // document successfully retrieved

                // save page
                savePage(document, url);

                // add new links in the document to crawl
                addNewLinksFromDocument(document);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                /* This catches "...Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml..." trace in output, which
                 * happens when we try to parse unsupported content type, such as PDF. This helps us satisfy the requirement that PDF should not be
                 * parsed. Need to confirm that this exception appears when downloading an image file, and we should be set in this department.
                 */
            }
        }
    }

    //function to get and update and save robot.txt permissions to crawlerPermissions
    private void getRobotPermission(String rootUrl) {
	    URL url;
	    Scanner s = null;
		try {
			url = new URL("http://" + rootUrl + "/robots.txt");
			s = new Scanner(url.openStream());
			//TODO use the scanner to read this file and store disallowed
			ArrayList<String> commands = new ArrayList<>();
			while(s.hasNextLine()) {
				String next = s.nextLine();
				if(next.toLowerCase().equals("user-agent: *")) {
					String command;
					while(s.hasNextLine() && (!(command = s.nextLine().toLowerCase()).contains("user-agent:"))){
						if(command.contains("disallow:")) {
							commands.add(command.split(":")[1].trim());
						}
					}
				}
			}
			s.close();
			synchronized(this.crawlPermissions) {
				this.crawlPermissions.put(rootUrl, commands);
			}
		} catch (MalformedURLException e) {
//			e.printStackTrace();
		} catch (IOException e) {
//			e.printStackTrace();
		} finally {
			if(s != null)
				s.close();
		}
	}

	private String getNextUrl() {
	    synchronized(urlsToCrawl) {
            if(!urlsToCrawl.isEmpty()) {
                return urlsToCrawl.remove();
            } else {
                return null;
            }
	    }
	}

	private static void savePage(Document doc, String pageUrl) {
	    try {
            // TODO come up with a better file naming convention that will actually work. this does not work all the time
            // it was just a test to see if it would work. My thought was we might need to backtrack to the url the html
            //comes from in the second stage to get a more complete page
	    	BufferedWriter writer =
              new BufferedWriter(new FileWriter(".\\data\\" +pageUrl.replaceAll("[:]", "").replaceAll("[/]", "") + ".html"));

            writer.write(doc.html());
            writer.close();
	    }
	    // Exceptions
        catch (MalformedURLException mue) {
	        System.out.println("Malformed URL Exception raised");
	    }
	    catch (IOException ie) {
	        System.out.println("IOException raised");
	    }
	}

    private void addNewLinksFromDocument(Document document) {
		ArrayList<String> linkUrls = new ArrayList<>();
        Elements links = document.select("a[href]");
        for (int i = 0; i < links.size(); i++) {
            String newLink = links.get(i).attr("abs:href"); // get absolute path when possible
            if (!newLink.startsWith("http://")) continue; // parse only http links (avoid ftp, https, or any other protocol). removes some urls that we do not want, such as "mailto"
            newLink = cleanupUpUrl(newLink); // cleanup: sharp, casing, encoding

            try {
                URL page = new URL(newLink);
                synchronized (crawlPermissions) {
                    if (crawlPermissions.get(page.getHost()) == null)
                        getRobotPermission(page.getHost());
                }
                if (canCrawl(page.getHost(), page.getPath())) {
                    synchronized (seenPages) {
                        if (!seenPages.contains(newLink)) {
                            System.out.println(newLink); // temporary print statement
                            seenPages.add(newLink);
                            linkUrls.add(newLink);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        synchronized(urlsToCrawl) {
            urlsToCrawl.addAll(linkUrls);
        }
    }


    private String cleanupUpUrl(String oldUrl) {
        String newUrl = removeSharpFromUrl(oldUrl) // avoid dups: strip off sharps
                .toLowerCase(); // convert to lowercase to avoid dups from casing

        // encode url spaces and other characters: https://stackoverflow.com/a/25735202
        try {
            URL url = new URL(newUrl);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            newUrl = uri.toASCIIString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newUrl;
    }

    private String removeSharpFromUrl(String url) {
	    int sharpPosition = url.lastIndexOf("#");
	    if (sharpPosition < 0) {
	        return url; // no sharp in the url
        }

	    // deals with .../#!/... which we do not want to remove
        int sharpExclamationPosition = url.lastIndexOf("#!");
        if (sharpPosition != sharpExclamationPosition) { // # that we found is not part of a /#!/ sequence
            return removeSharpFromUrl(url.substring(0, sharpPosition));
        } else {
            return url;
        }
	 }
	
	private Boolean canCrawl(String root, String path) {
		ArrayList<String> disallowed = this.crawlPermissions.get(root);
		if(disallowed != null) {
			for(int i = 0; i < disallowed.size(); ++i) {
				if(path.startsWith(disallowed.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

}
