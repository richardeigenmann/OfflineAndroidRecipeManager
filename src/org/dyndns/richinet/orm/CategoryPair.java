package org.dyndns.richinet.orm;

public class CategoryPair {

	
	private String category = "";
	private String member = "";
	
	public CategoryPair (String category, String member) {
		this.category = category;
		this.member = member;
	}
	
	
	public String getCategory() {
		return category;
	}
	
	public String getMember() {
		return member;
	}
}
