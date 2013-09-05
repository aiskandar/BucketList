package com.kiddobloom.bucketlist;

import android.graphics.drawable.Drawable;

public class FriendData {
	Drawable photo;
	String name;
	String hometown;
	String birthday;
	String facebookId;
	Boolean registered;
	String[] bucketList;
	Boolean notified;
	
	public boolean isFacebookId(String facebook_id) {
		return this.facebookId.equals(facebook_id);
	}
}
