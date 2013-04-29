package com.kiddobloom.bucketlist;

import com.google.gson.annotations.Expose;

public class BucketListTable {
	@Expose
	public int id;
	
	@Expose
	public String facebook_id;
	
	@Expose
	public String date;

	@Expose
	public String item;

	@Expose
	public String done;
	
	@Expose
	public String rating;
	
	@Expose
	public String share;
	
	void setId(int id) {
		this.id = id;
	}

	void setFacebookId(String fbid) {
		this.facebook_id = fbid;
	}

	void setEntry(String entry) {
		this.item = entry;
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
