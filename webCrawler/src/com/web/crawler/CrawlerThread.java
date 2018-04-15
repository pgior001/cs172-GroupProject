package com.web.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CrawlerThread extends Thread {
	int threadNumber;
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
	        //use code like this to make sure only one thread manipulates urls at a time.
	        String url = getNextUrl();
	        while(url != null) {
	        	ArrayList<String> found = this.retriveDocumentLinks(url);
	        	for(int i = 0; i < found.size(); ++i) {
	        		urlsToCrawl.add(found.get(i));
	        	}
	        	url = getNextUrl();
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
	    
    private ArrayList<String> retriveDocumentLinks(String URL) {
    	Document doc;
		ArrayList<String> linkUrls = new ArrayList<String>();
		try {
			doc = Jsoup.connect(URL).get();
			Elements links = doc.select("a[href]");
			for(int i = 0; i < links.size(); ++i) {
				linkUrls.add(links.get(i).attr("abs:href"));
			}
//	    	String relHref = link.attr("href"); // == "/"
//	    	String absHref = link.attr("abs:href"); // "http://jsoup.org/"
	    	System.out.println(threadNumber + ": " + linkUrls);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return linkUrls;
    }
}
