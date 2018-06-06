package com.web.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//class that will take inputs and initialize the crawler and all of its threads.
// TODO make the crawler take a seed file for the start pages.
public class CrawlerMain {
    public static List<String> rootPages;
    public static int numPages;
    public static int hopsAway;
    public static String seedFile;
    public static String outputDirectory;
    public static BufferedWriter index; 
    

    public static void main(String args[]){
    	//command line argument: rootpages 10000 6 C:\\Users\\Asus\\Downloads\\downloadscrawler\\
    	//change outputDirectory for yourself
    	seedFile = args[0];
    	numPages = Integer.parseInt(args[1]);
    	hopsAway = Integer.parseInt(args[2]);
    	outputDirectory = args[3];
    	rootPages = getRootPages();
    	try {
    		index = new BufferedWriter(new FileWriter(CrawlerMain.outputDirectory + File.separator + "index.txt"));
    	} catch (IOException e) {
    		
    	}
    	
    	//checks the number of threads that your processor can run at once.
    	final int cores = Runtime.getRuntime().availableProcessors();
    	ArrayList<CrawlerThread> threads = new ArrayList<>();
    	//spawns all the crawler threads
    	for(int i = 0; i < cores * 40; ++i) {
    		threads.add(new CrawlerThread(i));
    		threads.get(i).start();
    	}
    	
    	while(CrawlerThread.pageCount < numPages - 1) {
			for(int i = java.lang.Thread.activeCount(); i < cores * 40 + 1; ++i) {
	    		threads.add(new CrawlerThread(i));
	    		threads.get(threads.size() - 1).start();
			}
    		try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    private static List<String> getRootPages() {
        List<String> rootPages = new ArrayList<>();

        //rootpages is the resource
        URL path = CrawlerMain.class.getResource(seedFile);
        File file = new File(path.getFile().replaceAll("%20", " "));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
            	line = line.replace("http://", "");
                rootPages.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootPages;
    }
}
