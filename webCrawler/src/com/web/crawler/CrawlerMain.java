package com.web.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    

    public static void main(String args[]){
    	//for eventual use in reading in the command line arguments for the files.
    	//command line argument: rootpages 10000 6 C:\\Users\\Asus\\Downloads\\downloadscrawler\\
    	seedFile = args[0];
    	numPages = Integer.parseInt(args[1]);
    	hopsAway = Integer.parseInt(args[2]);
    	outputDirectory = args[3];
    	rootPages = getRootPages();
    	
    	//checks the number of threads that your processor can run at once.
    	final int cores = Runtime.getRuntime().availableProcessors();
    	ArrayList<CrawlerThread> threads = new ArrayList<>();
    	//spawns all the crawler threads
    	for(int i = 0; i < cores; ++i) {
    		threads.add(new CrawlerThread(i));
    		threads.get(i).start();
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
                rootPages.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootPages;
    }
}
