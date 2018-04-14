package com.web.crawler;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CrawlerMain {
    public static void main(String args[]){
    	int cores = Runtime.getRuntime().availableProcessors();
    	ArrayList<CrawlerThread> threads = new ArrayList<CrawlerThread>();
    	for(int i = 0; i < cores; ++i) {
    		threads.add(new CrawlerThread(i));
    		threads.get((threads.size() - 1)).start();
    	}
    }
}
