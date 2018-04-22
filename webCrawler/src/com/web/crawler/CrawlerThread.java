package com.web.crawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
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
import org.jsoup.nodes.Element;


//core of the crawler containing functions to call to run the crawler
public class CrawlerThread extends Thread {
	
	private static class UrlNode{
		public String url;
		public int hops;
		public boolean allowAddLinks;
		public boolean allowIndex;
		public UrlNode(String url, int hops) {
			this.url = url;
			this.hops = hops;
			allowAddLinks = true;
			allowIndex = true;
		}
	}
	public static BufferedWriter index = CrawlerMain.index;
	private static String pageCountSync = "";
	private robotTextReader robotText = new robotTextReader();
	private int threadNumber;
	private int maxHops = CrawlerMain.hopsAway;
	private static int maxPages = CrawlerMain.numPages;
	private static int pageCount = 0;
	private static boolean init = false;
    private static Map<String, ArrayList<String>> crawlPermissions = new HashMap<String, ArrayList<String>>();

    // all urls in this queue are unique and should be processed
    final private static Queue<UrlNode> urlsToCrawl = initUrlsToCrawl();
    // keeps track of all pages that are in urlsToCrawl (about to be processed) or pages already processed
    final private static Set<String> seenPages = new HashSet<>(CrawlerMain.rootPages);

	
	public CrawlerThread(int number) {
		threadNumber = number;
	}
	
	public void run() {
	    crawl();
    }

    private void crawl() {
	    for (UrlNode urlNode = getNextUrl(); urlNode != null; urlNode = getNextUrl()) {
            try {
            	synchronized(pageCountSync)
            	{
            		if(pageCount > maxPages)
                    {
            			break;
                    }
            	}
            	System.out.println("Url: " + urlNode.url + " Hop count: " + urlNode.hops + " Page count: " + pageCount);//wanted to check the hop count and the url
                Document document = Jsoup.connect(urlNode.url).get();
                // document successfully retrieved
                
                //check to see if doc can be indexed and if new links can be added from it
                checkDoc(document,urlNode);
                
                // save page
                if(urlNode.allowIndex)
                {
                	savePage(document, urlNode.url);
                }
                else
                {
                	System.out.println("NO INDEXING!!!!!!!!!!!! WEBSITE: " + urlNode.url);
                }

                // add new links in the document to crawl
                //check its under the hop limit
                //addNewLinksFromDocument(document);
                if(urlNode.hops < this.maxHops)
                {
                	if(urlNode.allowAddLinks)
                	{
                		addNewLinksFromDocument(document, urlNode.hops);
                	}
                	else
                	{
                		System.out.println("NO FOLLOWING!!!!!!!!!!!! WEBSITE: " + urlNode.url);
                	}
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
//                e.printStackTrace();
                /* This catches "...Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml..." trace in output, which
                 * happens when we try to parse unsupported content type, such as PDF. This helps us satisfy the requirement that PDF should not be
                 * parsed. Need to confirm that this exception appears when downloading an image file, and we should be set in this department.
                 */
            }
        }
    }

    private UrlNode getNextUrl() {
	    synchronized(urlsToCrawl) {
            if(!urlsToCrawl.isEmpty()) {
                return urlsToCrawl.remove();
            } else {
                return null;
            }
	    }
	}
    
    private static void checkDoc(Document document, UrlNode uNode)
    {
    	// TODO come up with a better file naming convention that will actually work. this does not work all the time
		// it was just a test to see if it would work. My thought was we might need to backtrack to the url the html
		//comes from in the second stage to get a more complete page
		Elements metas = document.getElementsByTag("meta");
		for (Element metaTag : metas)
		{
			String content = metaTag.attr("content");
			String name = metaTag.attr("name");
			
			if(name.equals("robots"))
			{
				if(content.toLowerCase().contains("nofollow"))
				{
					uNode.allowAddLinks = false;
				}
				if(content.toLowerCase().contains("noindex"))
				{
					uNode.allowIndex = false;
				}
			}
		}
    }

	private static void savePage(Document doc, String pageUrl) {
	    try {
            // TODO come up with a better file naming convention that will actually work. this does not work all the time
            // it was just a test to see if it would work. My thought was we might need to backtrack to the url the html
            //comes from in the second stage to get a more complete page
	    	String fileName;
	    	String pageNum;
	    	synchronized(pageCountSync) {
	    		fileName = CrawlerMain.outputDirectory + "\\" + String.valueOf(pageCount) + ".html";
	    		pageNum = String.valueOf(pageCount);
	    		++pageCount;
	    	}
	    	if(pageCount > maxPages)
	    		return;
	    	BufferedWriter writer =
              new BufferedWriter(new FileWriter(fileName));
	    	synchronized(index) {
	    		index.write(pageNum + ""
	    				+ "|" + pageUrl.toString() + "\n");
	    		index.flush();
	    	}

            writer.write(doc.html());
            writer.close();
	    }
	    // Exceptions
        catch (MalformedURLException mue) {
	        System.out.println("Malformed URL Exception raised");
	    }
	    catch (IOException ie) {
	        System.out.println("IOException raised");
	        System.out.println(ie);
	    }
	}
	

	private void addNewLinksFromDocument(Document document, int hopNumber) {
		ArrayList<UrlNode> linkUrls = new ArrayList<>();
        Elements links = document.select("a[href]");
        for (int i = 0; i < links.size(); i++) {
            String newLink = links.get(i).attr("abs:href"); // get absolute path when possible
            if (!newLink.startsWith("http://")) continue; // parse only http links (avoid ftp, https, or any other protocol). removes some urls that we do not want, such as "mailto"
            newLink = cleanupUpUrl(newLink); // cleanup: sharp, casing, encoding
            if(!newLink.contains(".edu"))
            	continue;
            if (robotText.robotsShouldFollow(newLink)) {
                synchronized (seenPages) {
                    if (!seenPages.contains(newLink)) {
                        // System.out.println(newLink); // temporary print statement
                        seenPages.add(newLink);
                        linkUrls.add(new UrlNode(newLink, hopNumber + 1));
                    }
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

    private static LinkedList<UrlNode> initUrlsToCrawl() {
        LinkedList<UrlNode> list = new LinkedList<>();

        for (String s : CrawlerMain.rootPages) {
            list.add(new UrlNode(s, 0));
        }
        return list;
    }

}
