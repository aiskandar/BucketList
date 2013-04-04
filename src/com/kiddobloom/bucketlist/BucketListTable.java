package com.kiddobloom.bucketlist;

import com.google.gson.annotations.Expose;

public class BucketListTable {
	@Expose
	public int id;
	
	@Expose
	public String entry;
	
	@Expose
	public String date;
	
	@Expose
	public String done;
	
	void setId(int id) {
		this.id = id;
	}
	
	void setEntry(String entry) {
		this.entry = entry;
	}
	
	void setDate(String date) {
		this.date = date;
	}
	
	void setDone(String done) {
		this.done = done;
	}
}
