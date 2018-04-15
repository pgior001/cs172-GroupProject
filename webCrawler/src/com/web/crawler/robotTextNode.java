package com.web.crawler;

import java.util.ArrayList;

//node to make storing robot.txt easier. also can add helper functions here.
public class robotTextNode {
	ArrayList<String> disallowed;
	String rootUrl;
	
	public robotTextNode(String root, ArrayList<String> disallowed) {
		this.rootUrl = root;
		this.disallowed = disallowed;
	}
}
