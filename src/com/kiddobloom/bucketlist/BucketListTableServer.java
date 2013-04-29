package com.kiddobloom.bucketlist;

import com.google.gson.annotations.Expose;

public class BucketListTableServer {
	@Expose
	public int id;
	
	@Expose
	public String entry;
	
	@Expose
	public String date;
	
	@Expose
	public String rating;
	
	@Expose
	public String done;
	
	@Expose
	public String username;
	
	void setId(int id) {
		this.id = id;
	}
	
	void setEntry(String entry) {
		this.entry = entry;
	}
	
	void setDate(String date) {
		this.date = date;
	}
	
	void setRating(String rating) {
		this.rating = rating;
	}
	
	void setDone(String done) {
		this.done = done;
	}
	
	void setUsername(String username) {
		this.username = username;
	}
	
}
