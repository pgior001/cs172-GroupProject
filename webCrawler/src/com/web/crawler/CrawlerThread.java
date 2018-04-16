package com.web.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
	int threadNumber;
	//might need to make a node of this to track the depth along with the url. or possible just a set.
    private static Set<String> pagesVisited = new HashSet<>();
    private static Map<String, ArrayList<String>> crawlPermissions = new HashMap<String, ArrayList<String>>();
    final private static Queue<String> urlsToCrawl = new LinkedList<>(Arrays.asList("http://www.ucr.edu/", "https://www.american.edu/", "https://www.sdsu.edu/"
			, "http://www.rccd.edu", "https://ucsd.edu/", "https://www.berkeley.edu/", "https://uci.edu/", "http://www.ucla.edu/", "https://www.csusm.edu/", 
			"http://www.cpp.edu/", "http://www.redlands.edu/", "https://calbaptist.edu/", "https://www.apu.edu/"));
	
	public CrawlerThread(int number) {
		threadNumber = number;
	}
	
	public void run() {
        this.crawl();
    }
	
//	 private void crawl() {
//	    System.out.println("thread started: " + threadNumber);
//		// TODO add code to look read robots.txt and check permisions from these files
//		// TODO track and use a max depth
//		String url = getNextUrl();
//
//		do {
//			// checks if we have already visited the page so we do not add it again
//			//use code like this to make sure only one thread manipulates urls at a time.
//			synchronized(pagesVisited){
//				//TODO add code to check that the url is not dissallowed in a robots.text
//				while(pagesVisited.contains(url)) {
//					url = getNextUrl();
//				}
//				pagesVisited.add(url);
//		        // System.out.println(pagesVisited.size());
//			}
//			ArrayList<String> found = this.retriveDocumentLinks(url);
//			synchronized(urlsToCrawl) {
//				for(int i = 0; i < found.size(); ++i) {
//					urlsToCrawl.add(found.get(i));
//				}
//			}
//		} while(url != null);
//    }

    private void crawl() {
//	    System.out.println("thread started: " + threadNumber);
	    for (String nextUrl = getNextUrl(); nextUrl != null; nextUrl = getNextUrl()) {
	        // visit the next url
            synchronized (pagesVisited) {
                pagesVisited.add(nextUrl);
            }
            URL page;
			try {
				page = new URL(nextUrl);
				synchronized(this.crawlPermissions) {
					if(this.crawlPermissions.get(page.getHost()) == null)
						getRobotPermission(page.getHost());
				}
				if(canCrawl(page.getHost(),page.getPath())) {
			        ArrayList<String> newLinks = retriveDocumentLinks(nextUrl);
		
			        synchronized(urlsToCrawl) {
			            for (String s : newLinks) {
			                // only put unvisited pages into frontier
			                if (!pagesVisited.contains(s)) {
			                    urlsToCrawl.add(s);
		                    }
		                }
		            }
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	    
    private ArrayList<String> retriveDocumentLinks(String URL) {
    	Document doc;
		ArrayList<String> linkUrls = new ArrayList<>();
		try {
			doc = Jsoup.connect(URL).get();
			Elements links = doc.select("a[href]");
			for(int i = 0; i < links.size(); ++i) {
				linkUrls.add(links.get(i).attr("abs:href"));
			}
			savePage(doc, URL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return linkUrls;
    }
}
