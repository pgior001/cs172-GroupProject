package com.web.crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CrawlerThread extends Thread {
	int threadNumber;
	private static Queue<String> urlsToCrawl = new LinkedList<String>();
	
	public CrawlerThread(int number) {
		threadNumber = number;
	}
	
	public void run() {
	        System.out.println("thread started: " + threadNumber);
	        //use code like this to make sure only one thread manipulates urls at a time.
	        synchronized(urlsToCrawl) {
	        	if(!urlsToCrawl.isEmpty()) {
	        		String url = urlsToCrawl.remove();
	        	}
	        }
    }
	 private static void crawl() {
	    	
    }
	    
    private void retriveDocumentLinks(String URL) {
    	Document doc;
		try {
			doc = Jsoup.connect(URL).get();
			Element link = doc.select("a").first();
	    	String relHref = link.attr("href"); // == "/"
	    	String absHref = link.attr("abs:href"); // "http://jsoup.org/"
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
