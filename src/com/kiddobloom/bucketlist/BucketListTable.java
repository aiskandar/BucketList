package com.kiddobloom.bucketlist;

import com.google.gson.annotations.Expose;

public class BucketListTable {

	@Expose
	public int server_id;

	@Expose
	public String facebook_id;
	
	@Expose
	public String facebook_pending_announce;
	
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
	
	@Expose
	public String imagepath;
	
	@Expose
	public String date_completed;
	
	@Expose
	public String imagecache;
	
	@Expose
	public byte[] image;

	void setServerId(int server_id) {
		this.server_id = server_id;
	}

	void setFacebookId(String fbid) {
		this.facebook_id = fbid;
	}
	
	void setFacebookPendingAnnounce(String pending) {
		this.facebook_pending_announce = pending;
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
	
	void setImagecache(String ic) {
		this.imagecache = ic;
	}
	
	void setDone(String done) {
		this.done = done;
	}
	
	void setShare(String share) {
		this.share = share;
	}
	
	void setImagepath(String imagepath) {
		this.imagepath = imagepath;
	}
	
	void setDatecompleted(String datecompleted) {
		this.date_completed = datecompleted;
	}
	
	void setImage(byte[] image) {
		this.image = image.clone();
	}
	
	
}
