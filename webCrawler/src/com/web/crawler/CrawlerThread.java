package com.web.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CrawlerThread extends Thread {
	int threadNumber;
    private static Set<String> pagesVisited = new HashSet<String>();
	private static Queue<String> urlsToCrawl = new LinkedList<String>(Arrays.asList("http://www.ucr.edu/", "https://www.american.edu/", "https://www.sdsu.edu/" 
			, "http://www.rccd.edu", "https://ucsd.edu/", "https://www.berkeley.edu/", "https://uci.edu/", "http://www.ucla.edu/", "https://www.csusm.edu/", 
			"http://www.cpp.edu/", "http://www.redlands.edu/", "https://calbaptist.edu/", "https://www.apu.edu/"));
	
	public CrawlerThread(int number) {
		threadNumber = number;
	}
	
	public void run() {
	        this.crawl();
    }
	
	 private void crawl() {
		 System.out.println("thread started: " + threadNumber);
		 	// TODO add code to look read robots.txt
		 	// TODO track and use a max depth
	        String url = getNextUrl();
	        do {
        		// checks if we have already visited the page so we do not add it again
		        //use code like this to make sure only one thread manipulates urls at a time.
	        	synchronized(pagesVisited){
	        		//TODO add code to check that the url is not dissallowed in a robots.text
		        	while(pagesVisited.contains(url)) {
			        	url = getNextUrl();
		        	}
		        	pagesVisited.add(url);
//		        	System.out.println(pagesVisited.size());
	        	}
	        	ArrayList<String> found = this.retriveDocumentLinks(url);
	        	for(int i = 0; i < found.size(); ++i) {
        			urlsToCrawl.add(found.get(i));
	        	}
	        } while(url != null);
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
			 	//comes from
	            BufferedWriter writer = 
	              new BufferedWriter(new FileWriter(".\\data\\" +pageUrl.replaceAll("[:]", "").replaceAll("[/]", "") + ".html"));
	             
	            writer.write(doc.html());
	 
	            writer.close();
	            System.out.println("Successfully Downloaded.");
	        }
	 
	        // Exceptions
	        catch (MalformedURLException mue) {
	            System.out.println("Malformed URL Exception raised");
	        }
	        catch (IOException ie) {
	            System.out.println("IOException raised");
	        }
	 }
	    
    private ArrayList<String> retriveDocumentLinks(String URL) {
    	Document doc;
		ArrayList<String> linkUrls = new ArrayList<String>();
		try {
			doc = Jsoup.connect(URL).get();
			Elements links = doc.select("a[href]");
			for(int i = 0; i < links.size(); ++i) {
				linkUrls.add(links.get(i).attr("abs:href"));
			}
			savePage(doc, URL);
//	    	String relHref = link.attr("href"); // == "/"
//	    	String absHref = link.attr("abs:href"); // "http://jsoup.org/"
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return linkUrls;
    }
}
