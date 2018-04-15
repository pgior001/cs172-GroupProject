package com.web.crawler;

import java.util.ArrayList;

//class that will take inputs and initialize the crawler and all of its threads.
// TODO make the crawler take a seed file for the start pages.
public class CrawlerMain {
    public static void main(String args[]){
    	//checks the number of threads that your processor can run at once.
    	final int cores = Runtime.getRuntime().availableProcessors();
    	ArrayList<CrawlerThread> threads = new ArrayList<>();
    	//spawns all the crawler threads
    	for(int i = 0; i < cores; ++i) {
    		threads.add(new CrawlerThread(i));
    		threads.get(i).start();
    	}
    }
}
