package com.kiddobloom.bucketlist;

import com.google.gson.annotations.Expose;

public class BucketListPreviewTable {

	@Expose
	public String facebook_id;

	@Expose
	public String exists;
	
	@Expose
	public String entry0;
	
	@Expose
	public String entry1;	

	
	void setFacebookId(String facebook_id) {
		this.facebook_id = facebook_id;
	}

	void setEntry0(String entry0) {
		this.entry0 = entry0;
	}

	void setEntry1(String entry1) {
		this.entry1 = entry1;
	}

	void setExists(String exists) {
		this.exists = exists;
	}
}
