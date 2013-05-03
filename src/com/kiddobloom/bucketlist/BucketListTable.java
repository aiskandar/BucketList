package com.kiddobloom.bucketlist;

import com.google.gson.annotations.Expose;

public class BucketListTable {

	@Expose
	public int server_id;

	@Expose
	public String facebook_id;
	
	@Expose
	public String date;

	@Expose
	public String entry;

	@Expose
	public String done;
	
	@Expose
	public String rating;
	
	@Expose
	public String share;
	
	void setServerId(int server_id) {
		this.server_id = server_id;
	}

	void setFacebookId(String fbid) {
		this.facebook_id = fbid;
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
	
	void setShare(String share) {
		this.share = share;
	}
	
}
