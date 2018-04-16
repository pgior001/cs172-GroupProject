package com.web.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

//core of the crawler containing functions to call to run the crawler
public class CrawlerThread extends Thread {
	private int threadNumber;
    //just an idea. this will use more memory but will decrease the number of times we need to request the robots.txt by storing the results.
    private static ArrayList<robotTextNode> crawlPermissions = new ArrayList<>();

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
	    System.out.println("thread started: " + threadNumber);
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
		try {
			url = new URL(rootUrl);
			Scanner s = new Scanner(url.openStream());
			//TODO use the scanner to read this file and store disallowed
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
//            System.out.println("Successfully Downloaded.");
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

            synchronized (seenPages) {
                if (!seenPages.contains(newLink)) {
                    System.out.println(newLink); // temporary print statement
                    seenPages.add(newLink);
                    linkUrls.add(newLink);
                }
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
}
