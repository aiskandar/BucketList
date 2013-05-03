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
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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
		Log.d("tag", "onPerformSync :" + account.name + " " + extras);
        boolean force = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);
        Cursor c = null;
        
		try {
			c = provider.query(MyContentProvider.AUTHORITY_URI, null, null,
					null, null);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        if (force == true) {
        	// this was triggered by a query - the only place I set force to true
        	Log.d("tag", "db query");
        	fetchBucketList(account.name, provider, c);
        	
        } else {

			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				
				if (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_TRANSACTING) {
					
					Log.d("tag", "the following row ID: " + c.getInt(MyContentProvider.COLUMN_INDEX_ID) + " is transacting");
					Log.d("tag", "transaction type: " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
					
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

		try {
			final HttpGet get = new HttpGet(
					"http://andyiskandar.me/bucketdelete.php?serverid=" + c.getInt(MyContentProvider.COLUMN_INDEX_SERVER_ID));
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
			Log.d("tag", "response: " + response);

			boolean error = response.startsWith("error:");
			
			if (error == true) {
				String arr[] = response.split(":");
			
				Log.d("tag", "error response in restDelete");
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
			Log.d("tag", "Server error in fetching remote contacts: "
					+ resp.getStatusLine());
		}		
	}

	private void restUpdate(String id, Cursor c, ContentProviderClient provider) {
		
		
	}
	
	private void restInsert(String id, Cursor c, ContentProviderClient provider) {
		
		Gson mJson = new Gson(); 
		ArrayList<BucketListTable> list = new ArrayList<BucketListTable>();
        HttpEntity entity = null;
        HttpResponse resp = null;
        String response = null;

		BucketListTable data = new BucketListTable();

		data.setFacebookId(id);	
		data.setDate(c.getString(MyContentProvider.COLUMN_INDEX_DATE));
		data.setEntry(c.getString(MyContentProvider.COLUMN_INDEX_ENTRY));
		data.setDone(c.getString(MyContentProvider.COLUMN_INDEX_DONE));
		data.setRating(c.getString(MyContentProvider.COLUMN_INDEX_RATING));
		data.setShare(c.getString(MyContentProvider.COLUMN_INDEX_SHARE));

		list.add(data);
		
		String jsonData = mJson.toJson(list);
		Log.d("tag", jsonData);

		final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("json", jsonData));

		try {
			entity = new UrlEncodedFormEntity(nvp);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// entity = new StringEntity(jsonData);
			final HttpPost post = new HttpPost(
					"http://andyiskandar.me/bucket.php");
			post.addHeader(entity.getContentType());
			post.setEntity(entity);
			// post.setHeader("Accept", "application/json");
			// post.setHeader("Content-type", "application/json");

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
			Log.d("tag", "response: " + response);
			
			boolean error = response.startsWith("error:");
			
			if (error == true) {
				String arr[] = response.split(":");
			
				Log.d("tag", "error response in restDelete");
				for(int i=0; i < arr.length ; i++) {
					Log.d("tag", "arr[i]");
				}
			} else {
				// the response should contain server_id of the last inserted entry
				int serverId = Integer.parseInt(response);
				
				Log.d("tag", "server id: " + serverId);
				
				ContentValues cv = new ContentValues();
				cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
				cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
				cv.put(MyContentProvider.COLUMN_SERVER_ID, serverId);
				
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
			
			if (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_TRANSACTING || 
					c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_RETRY) {
				
				Log.d("tag", "the following row ID: " + c.getInt(MyContentProvider.COLUMN_INDEX_ID) + " is transacting/retry");
				Log.d("tag", "transaction type: " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
				
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

			Uri base = MyContentProvider.CONTENT_URI;
			base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE_NO_NOTIFY);
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
					"http://andyiskandar.me/bucketget.php?id=" + id);
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
			Log.d("tag", "response: " + response);

			Gson mJson = new Gson();

			ArrayList<BucketListTable> blList = mJson.fromJson(response,
					new TypeToken<Collection<BucketListTable>>() {
					}.getType());

			int size = blList.size();
			
			if (size > 0) {
				ContentValues[] cvArray = new ContentValues[size];
	
				for (int i = 0; i < size; i++) {
					BucketListTable blt = blList.get(i);
	
					ContentValues cv = new ContentValues();
	
					Log.d("tag", "server id: " + blt.server_id);
					
					cv.put(MyContentProvider.COLUMN_SERVER_ID, blt.server_id);
					cv.put(MyContentProvider.COLUMN_ENTRY, blt.entry);
					cv.put(MyContentProvider.COLUMN_DATE, blt.date);
					cv.put(MyContentProvider.COLUMN_RATING, blt.rating);
					cv.put(MyContentProvider.COLUMN_SHARE, blt.share);
					cv.put(MyContentProvider.COLUMN_DONE, blt.done);
					cv.put(MyContentProvider.COLUMN_REST_STATUS,
							MyContentProvider.REST_STATUS_SYNCED);
					cv.put(MyContentProvider.COLUMN_REST_STATE,
							MyContentProvider.REST_STATE_NONE);
	
					cvArray[i] = cv;
				}
	
				Uri base = MyContentProvider.CONTENT_URI;
				base = Uri.withAppendedPath(base, "insert");
	
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
