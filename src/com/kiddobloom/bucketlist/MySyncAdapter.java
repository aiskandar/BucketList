package com.kiddobloom.bucketlist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MySyncAdapter extends AbstractThreadedSyncAdapter {

	public MySyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		
		SharedPreferences sp = getContext().getSharedPreferences(getContext().getString(R.string.pref_name), 0);
		int state = sp.getInt(getContext().getString(R.string.pref_state_key), 100);
		
		if (state != StateMachine.ONLINE_STATE) {
			return;
		}
				
		Log.d("tag", "onPerformSync :" + account.name + " " + extras);
        boolean force = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);
        Cursor c = null;
        
		try {
			c = provider.query(MyContentProvider.CONTENT_URI, null, null, null, null);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        if (force == true) {
        	// this was triggered by a query - the only place I set force to true
        	Log.d("tag", "onPerformSync: db query");
        	fetchBucketList(account.name, provider, c);
        } else {
        	// triggered by insert, update, or delete
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				
				Log.d("tag", "onPerformSync: processing row ID = " + c.getInt(MyContentProvider.COLUMN_INDEX_ID));
				
				if (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_TRANSACTING) {
					
					Log.d("tag", "onPerformSync: the following row ID = " + c.getInt(MyContentProvider.COLUMN_INDEX_ID) + " is transacting");
					Log.d("tag", "onPerformSync: transaction type = " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
					
					switch (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)) {
						case MyContentProvider.REST_STATE_INSERT:
							restInsert(account.name, c, provider);
							break;
						case MyContentProvider.REST_STATE_UPDATE:
							restUpdate(account.name, c, provider);
							break;
						case MyContentProvider.REST_STATE_DELETE:
							restDelete(account.name, c, provider);
							break;
						case MyContentProvider.REST_STATE_NONE:	
						case MyContentProvider.REST_STATE_QUERY:
							// throw exception here - when transacting, it cannot be in these 2 states
							break;
						default:
					}				
				} else {
					
				}
				c.moveToNext();
			}
        }

	}

	private void restDelete(String id, Cursor c, ContentProviderClient provider) {
		
		Gson mJson = new Gson(); 
		ArrayList<BucketListTable> list = new ArrayList<BucketListTable>();
        HttpEntity entity = null;
        HttpResponse resp = null;
        String response = null;

        Log.d("tag", "restDelete : rest state = " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
        
		try {
			final HttpGet get = new HttpGet(
					"http://23.20.35.242/bucketdelete.php?serverid=" + c.getInt(MyContentProvider.COLUMN_INDEX_SERVER_ID));
			HttpClient mHttpClient = new DefaultHttpClient();
			resp = mHttpClient.execute(get);
			response = EntityUtils.toString(resp.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			Log.d("tag", "restDelete: response = " + response);

			boolean error = response.startsWith("error:");
			
			if (error == true) {
				String arr[] = response.split(":");
			
				Log.d("tag", "restDelete: error response in restDelete");
				for(int i=0; i < arr.length ; i++) {
					Log.d("tag", "error: " + arr[i]);
				}
				
			} else {
			
				Uri base = MyContentProvider.CONTENT_URI;
				base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE_DB);
				Uri uri = Uri.withAppendedPath(base, Integer.toString(c.getInt(MyContentProvider.COLUMN_INDEX_ID)));
				
				try {
					provider.delete(uri, null, null);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			Log.d("tag", "restDelete: server error in fetching remote contacts: "
					+ resp.getStatusLine());
		}		
	}

	private void restUpdate(String id, Cursor c, ContentProviderClient provider) {
		Gson mJson = new Gson(); 
		ArrayList<BucketListTable> list = new ArrayList<BucketListTable>();
        HttpEntity entity = null;
        HttpResponse resp = null;
        String response = null;

        Log.d("tag", "restUpdate : rest state = " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
        
		BucketListTable data = new BucketListTable();

		data.setDate(c.getString(MyContentProvider.COLUMN_INDEX_DATE));
		data.setEntry(c.getString(MyContentProvider.COLUMN_INDEX_ENTRY));
		data.setDone(c.getString(MyContentProvider.COLUMN_INDEX_DONE));
		data.setRating(c.getString(MyContentProvider.COLUMN_INDEX_RATING));
		data.setShare(c.getString(MyContentProvider.COLUMN_INDEX_SHARE));
		data.setServerId(c.getInt(MyContentProvider.COLUMN_INDEX_SERVER_ID));
		data.setImagepath(c.getString(MyContentProvider.COLUMN_INDEX_IMG_PATH));
		data.setDatecompleted(c.getString(MyContentProvider.COLUMN_INDEX_DATE_COMPLETED));
		data.setImagecache(c.getString(MyContentProvider.COLUMN_INDEX_IMG_CACHE));
		//data.setImage(c.getBlob(MyContentProvider.COLUMN_INDEX_IMG));
		data.setFacebookId(c.getString(MyContentProvider.COLUMN_INDEX_FACEBOOK_ID));

		String imgpath = c.getString(MyContentProvider.COLUMN_INDEX_IMG_PATH);
		String image64 = null;
		
		Log.d("tag", "restUpdate imgpath: " + imgpath);
		if (imgpath.startsWith("/")) {
			Log.d("tag", "restUpdate starts with /");
			byte[] image = c.getBlob(MyContentProvider.COLUMN_INDEX_IMG);
			image64 = Base64.encodeToString(image, Base64.DEFAULT);
		} else if (imgpath.startsWith("http")) {
			// do nothing - if the imagepath is updated by the user on the app, 
			// the variable will be a filesystem path as opposed http url
			// if it is http url, means there is no update
			Log.d("tag", "restUpdate starts with http");
		} else {
			// do nothing
		}
				
		list.add(data);
		
		String jsonData = mJson.toJson(list);
		Log.d("tag", "restUpdate :" + jsonData);

		final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("json", jsonData));
		if (image64 != null) {
			nvp.add(new BasicNameValuePair("image64", image64));
		}
		
		try {
			entity = new UrlEncodedFormEntity(nvp);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// entity = new StringEntity(jsonData);
			final HttpPost post = new HttpPost(
					"http://23.20.35.242/bucketupdate.php");
			post.addHeader(entity.getContentType());
			post.setEntity(entity);

			HttpClient mHttpClient = new DefaultHttpClient();
			resp = mHttpClient.execute(post);
			response = EntityUtils.toString(resp.getEntity());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			Log.d("tag", "restUpdate: server response: " + response);
			
			boolean error = response.startsWith("error:");
			
			if (error == true) {
				String arr[] = response.split(":");
			
				Log.d("tag", "error response in restUpdate");
				for(int i=0; i < arr.length ; i++) {
					Log.d("tag", "arr[i]");
				}
			} else {
				// the response should contain fileurl of the last updated entry		
				//Log.d("tag", "restUpdate: updated fileurl: " + response);
				
				ContentValues cv = new ContentValues();
				cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
				cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
				cv.put(MyContentProvider.COLUMN_IMG_PATH, response);
				cv.put(MyContentProvider.COLUMN_IMG_CACHE, "false");
				
				Uri base = Uri.withAppendedPath(MyContentProvider.CONTENT_URI, MyContentProvider.PATH_UPDATE_NO_NOTIFY);
				Uri uri = Uri.withAppendedPath(base, Integer.toString(c.getInt(MyContentProvider.COLUMN_INDEX_ID)));
				
				try {
					provider.update(uri, cv, null, null);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} else {
			Log.d("tag", "Server error in fetching remote contacts: "
					+ resp.getStatusLine());
		}		
	}
	
	private void restInsert(String id, Cursor c, ContentProviderClient provider) {
		
		Gson mJson = new Gson(); 
		ArrayList<BucketListTable> list = new ArrayList<BucketListTable>();
        HttpEntity entity = null;
        HttpResponse resp = null;
        String response = null;

        Log.d("tag", "restInsert : rest state = " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
        
		BucketListTable data = new BucketListTable();

		data.setFacebookId(c.getString(MyContentProvider.COLUMN_INDEX_FACEBOOK_ID));
		data.setDate(c.getString(MyContentProvider.COLUMN_INDEX_DATE));
		data.setEntry(c.getString(MyContentProvider.COLUMN_INDEX_ENTRY));
		data.setDone(c.getString(MyContentProvider.COLUMN_INDEX_DONE));
		data.setRating(c.getString(MyContentProvider.COLUMN_INDEX_RATING));
		data.setShare(c.getString(MyContentProvider.COLUMN_INDEX_SHARE));
		data.setImagepath(c.getString(MyContentProvider.COLUMN_INDEX_IMG_PATH));
		data.setDatecompleted(c.getString(MyContentProvider.COLUMN_INDEX_DATE_COMPLETED));
		//data.setImage(c.getBlob(MyContentProvider.COLUMN_INDEX_IMG));
		data.setImagecache(c.getString(MyContentProvider.COLUMN_INDEX_IMG_CACHE));

		String imgpath = c.getString(MyContentProvider.COLUMN_INDEX_IMG_PATH);
		String image64 = null;
		
		Log.d("tag", "restinsert imgpath: " + imgpath);
		if (imgpath.startsWith("/")) {
			Log.d("tag", "restinsert starts with /");
			byte[] image = c.getBlob(MyContentProvider.COLUMN_INDEX_IMG);
			image64 = Base64.encodeToString(image, Base64.DEFAULT);
		} else if (imgpath.startsWith("http")) {
			// do nothing - if the imagepath is updated by the user on the app, 
			// the variable will be a filesystem path as opposed http url
			// if it is http url, means there is no update
			Log.d("tag", "restinsert starts with http");
		} else {
			// do nothing
		}
		
		list.add(data);
		
		String jsonData = mJson.toJson(list);
		Log.d("tag", "restInsert :" + jsonData);

		final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("json", jsonData));
		if (image64 != null) {
			nvp.add(new BasicNameValuePair("image64", image64));
		}
		
		try {
			entity = new UrlEncodedFormEntity(nvp);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// entity = new StringEntity(jsonData);
			final HttpPost post = new HttpPost(
					"http://23.20.35.242/bucket.php");
			post.addHeader(entity.getContentType());
			post.setEntity(entity);

			HttpClient mHttpClient = new DefaultHttpClient();
			resp = mHttpClient.execute(post);
			response = EntityUtils.toString(resp.getEntity());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			//Log.d("tag", "restinsert response: " + response);
			
			boolean error = response.startsWith("error:");
			
			if (error == true) {
				String arr[] = response.split(":");
			
				Log.d("tag", "error response in restinsert");
				for(int i=0; i < arr.length ; i++) {
					Log.d("tag", "arr[i]");
				}
			} else {
				// the response should contain server_id of the last inserted entry
				
				Log.d("tag", "restInsert: server response: " + response);
				
				String arr[] = response.split("\\|");
				
				if (arr.length == 3) {
					Log.d("tag", arr[0] + " " + arr[1] + " " + arr[2]);
				} else if (arr.length == 2) {
					Log.d("tag", arr[0] + " " + arr[1]);
				} else if (arr.length == 1) {
					Log.d("tag", arr[0]);
				} else {
					Log.d("tag", "split response array size: " + arr.length);
				}
				
				String serverId = arr[0];
				String fileurl = arr[1];
				
				Log.d("tag", "restInsert: server response serverid: " + serverId);
				
				ContentValues cv = new ContentValues();
				cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
				cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
				cv.put(MyContentProvider.COLUMN_SERVER_ID, serverId);
				cv.put(MyContentProvider.COLUMN_IMG_PATH, fileurl);
				cv.put(MyContentProvider.COLUMN_IMG_CACHE, "false");
				
				Uri base = Uri.withAppendedPath(MyContentProvider.CONTENT_URI, MyContentProvider.PATH_UPDATE_NO_NOTIFY);
				Uri uri = Uri.withAppendedPath(base, Integer.toString(c.getInt(MyContentProvider.COLUMN_INDEX_ID)));
				
				try {
					provider.update(uri, cv, null, null);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} else {
			Log.d("tag", "Server error in fetching remote contacts: "
					+ resp.getStatusLine());
		}
		
	}
	
	private void fetchBucketList(String id, ContentProviderClient provider, Cursor c) {

		// before we do anything to fetch all the bucket list from the cloud
		// check the rest_status and rest_state of each local db
		// possible that users add to the list in offline mode - the rest_state would be REST_STATUS_RETRY or REST_STATUS_TRANSACTING
		// if all the rest_state in the list are SYNCED, we just overwrite the local db
		
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			
			Log.d("tag", "fetchBucketList: processing row ID = " + c.getInt(MyContentProvider.COLUMN_INDEX_ID));
			
			if (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_TRANSACTING || 
					c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_RETRY) {
				
				Log.d("tag", "fetchBucketList: the following row ID: " + c.getInt(MyContentProvider.COLUMN_INDEX_ID) + " is transacting/retry");
				Log.d("tag", "fetchBucketList: rest state = " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
				
				switch (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)) {
					case MyContentProvider.REST_STATE_INSERT:
						restInsert(id, c, provider);
						break;
					case MyContentProvider.REST_STATE_UPDATE:
						restUpdate(id, c, provider);
						break;
						
					case MyContentProvider.REST_STATE_DELETE:
						restDelete(id, c, provider);
						break;
					case MyContentProvider.REST_STATE_NONE:	
					case MyContentProvider.REST_STATE_QUERY:
						// throw exception here - when transacting, it cannot be in these 2 states
						break;
					default:
				}				
			} 
			
			//Log.d("tag", "fetchBucketList: server has been updated for rowID " + c.getInt(MyContentProvider.COLUMN_INDEX_ID) + "... deleting from local DB");
			
			// delete all rows from local DB and sync to the latest updated sets from the server
			// this is a use-case when a user updates the list on one device and opens the app again on another device
			// do not notify listview that the entry is deleted because we will update with ones from server
			Uri base = MyContentProvider.CONTENT_URI;
			base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE_NO_NOTIFY);
			//base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE_DB);
			Uri uri = Uri.withAppendedPath(base, Integer.toString(c.getInt(MyContentProvider.COLUMN_INDEX_ID)));
			
			try {
				provider.delete(uri, null, null);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			c.moveToNext();
		}
						
		HttpEntity entity = null;
		HttpResponse resp = null;
		String response = null;
		
		try {
			final HttpGet get = new HttpGet(
					"http://23.20.35.242/bucketget.php?id=" + id);
			HttpClient mHttpClient = new DefaultHttpClient();
			resp = mHttpClient.execute(get);
			response = EntityUtils.toString(resp.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			Log.d("tagbg", "fetchBucketList: bucketget.php server response: " + response);

			Gson mJson = new Gson();

			ArrayList<BucketListTable> blList = mJson.fromJson(response,
					new TypeToken<Collection<BucketListTable>>() {
					}.getType());

			if (blList == null)
				return;
			
			int size = blList.size();
			
			if (size > 0) {
				ContentValues[] cvArray = new ContentValues[size];
	
				for (int i = 0; i < size; i++) {
					BucketListTable blt = blList.get(i);
	
					ContentValues cv = new ContentValues();
	
					Log.d("tag", "fetchBucketList response serverId: " + blt.server_id);
					
					cv.put(MyContentProvider.COLUMN_FACEBOOK_ID, blt.facebook_id);
					cv.put(MyContentProvider.COLUMN_SERVER_ID, blt.server_id);
					cv.put(MyContentProvider.COLUMN_ENTRY, blt.entry);
					cv.put(MyContentProvider.COLUMN_DATE, blt.date);
					cv.put(MyContentProvider.COLUMN_RATING, blt.rating);
					cv.put(MyContentProvider.COLUMN_SHARE, blt.share);
					cv.put(MyContentProvider.COLUMN_DONE, blt.done);
					cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
					cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
					cv.put(MyContentProvider.COLUMN_IMG_PATH, blt.imagepath);
					cv.put(MyContentProvider.COLUMN_DATE_COMPLETED, blt.date_completed);
					cv.put(MyContentProvider.COLUMN_IMG_CACHE, blt.imagecache);
					//cv.put(MyContentProvider.COLUMN_IMG, blt.image);
	
					cvArray[i] = cv;
				}
				
				Uri base = MyContentProvider.CONTENT_URI;
				base = Uri.withAppendedPath(base, MyContentProvider.PATH_INSERT);
	
				try {
					provider.bulkInsert(base, cvArray);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.d("tag",
					"Server error in fetching bucket list: "
							+ resp.getStatusLine());
		}
	
	}
	

//	public void requestSync() {
//		
//		AccountManager accountManager = AccountManager.get(getContext());
//
//		Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");
//
//		for (int i = 0; i < accounts.length; i++) {
//			Log.d("tag", "account: " + accounts[i].name);
//			Bundle extra = new Bundle();
//			extra.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//
//			ContentResolver.requestSync(accounts[i],
//					MyContentProvider.AUTHORITY, extra);
//		}
//	}
}
