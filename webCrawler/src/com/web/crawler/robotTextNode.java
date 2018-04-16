package com.web.crawler;

import java.util.ArrayList;

//import org.apache.commons.lang3.builder.EqualsBuilder;
//import org.apache.commons.lang3.builder.HashCodeBuilder;

//node to make storing robot.txt easier. also can add helper functions here.
//class no longer in use
public class robotTextNode {
	private ArrayList<String> disallowed;
	private String rootUrl;
	
	public robotTextNode(String root, ArrayList<String> disallowed) {
		this.rootUrl = root;
		this.disallowed = disallowed;
	}
	
	public robotTextNode(String root) {
		this.rootUrl = root;
		this.disallowed = new ArrayList<>();
	}
	
	public void addDissallowed(String toAdd) {
		this.disallowed.add(toAdd);
	}
	
	public ArrayList<String> getDissallowed() {
		return this.disallowed;
	}
	
	public String getRootUrl() {
		return this.rootUrl;
	}
	
//	@Override
//    public int hashCode() {
//        return new HashCodeBuilder(24593, 1572869). // two randomly chosen prime numbers
//            // if deriving: appendSuper(super.hashCode()).
//            append(rootUrl).
//            toHashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//       if (!(obj instanceof robotTextNode))
//            return false;
//        if (obj == this)
//            return true;
//
//        robotTextNode rhs = (robotTextNode) obj;
//        return new EqualsBuilder().
//            // if deriving: appendSuper(super.equals(obj)).
//            append(rootUrl, rhs.rootUrl).
//            isEquals();
//    }
}
