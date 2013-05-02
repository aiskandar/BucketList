package com.kiddobloom.bucketlist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
	
	String KEY_USERNAME = "username";
	String KEY_DATE = "date";
	String KEY_ITEM = "item";
	String KEY_DONE = "done";
	String KEY_RATING = "rating";

	public MySyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		// TODO Auto-generated constructor stub
	}

//	public MySyncAdapter(Context context, boolean autoInitialize,
//			boolean allowParallelSyncs) {
//		super(context, autoInitialize, allowParallelSyncs);
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.d("tag", "Sync :" + account.name + " " + extras);
		Cursor c = null;
        boolean force = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);
        
        if (force == true) {
        	// this was triggered by a query - the only place I set force to true
        	Log.d("tag", "db query");
        	fetchBucketList(account.name, provider);
        	
        } else {
			try {
				c = provider.query(MyContentProvider.AUTHORITY_URI, null, null,
						null, null);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				
				if (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATUS) == MyContentProvider.REST_STATUS_TRANSACTING) {
					
					Log.d("tag", "the following row ID: " + c.getInt(MyContentProvider.COLUMN_INDEX_ID) + " is transacting");
					Log.d("tag", "transaction type: " + MyContentProvider.restStateStr[c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)]);
					
					switch (c.getInt(MyContentProvider.COLUMN_INDEX_REST_STATE)) {
						case MyContentProvider.REST_STATE_INSERT:
							restInsert(account, c, provider);
							break;
						case MyContentProvider.REST_STATE_UPDATE:
							break;
						case MyContentProvider.REST_STATE_DELETE:
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

	private void restInsert(Account account, Cursor c, ContentProviderClient provider) {
		
		Gson mJson = new Gson(); 
		ArrayList<BucketListTableServer> list = new ArrayList<BucketListTableServer>();
        HttpEntity entity = null;
        HttpResponse resp = null;
        String response = null;

		BucketListTableServer data = new BucketListTableServer();

		data.setDate(c.getString(MyContentProvider.COLUMN_INDEX_DATE));
		data.setEntry(c.getString(MyContentProvider.COLUMN_INDEX_ENTRY));
		data.setId(c.getInt(MyContentProvider.COLUMN_INDEX_ID));
		data.setDone(c.getString(MyContentProvider.COLUMN_INDEX_DONE));
		data.setRating(c
				.getString(MyContentProvider.COLUMN_INDEX_RATING));
		data.setUsername(account.name);	
		
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
			
			ContentValues cv = new ContentValues();
			cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
			cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
			
			Uri base = Uri.withAppendedPath(MyContentProvider.CONTENT_URI, "edit_status");
			Uri uri = Uri.withAppendedPath(base, Integer.toString(c.getInt(MyContentProvider.COLUMN_INDEX_ID)));
			
			try {
				provider.update(uri, cv, null, null);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			Log.d("tag", "Server error in fetching remote contacts: "
					+ resp.getStatusLine());
		}
		
	}
	
	
	private void fetchBucketList(String id, ContentProviderClient provider) {

		HttpEntity entity = null;
        HttpResponse resp = null;
        String response = null;
               
		try {
			final HttpGet get = new HttpGet("http://andyiskandar.me/bucketget.php?id=" + id);
			
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
    	
    	ArrayList<BucketListTable> blList = mJson.fromJson(response, new TypeToken<Collection<BucketListTable>>() {}.getType());
    	
    	int size = blList.size();
    	
    	for (int i=0; i < size; i++) {
        	BucketListTable blt = blList.get(i);

        	ContentValues cv = new ContentValues();
		
			cv.put(MyContentProvider.COLUMN_ENTRY, blt.item);
			cv.put(MyContentProvider.COLUMN_DATE, blt.date);
			cv.put(MyContentProvider.COLUMN_RATING, blt.rating);
			cv.put(MyContentProvider.COLUMN_SHARE, blt.share);
			cv.put(MyContentProvider.COLUMN_DONE, blt.done);
			cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
			cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
			
//			try {
//				provider.insert(MyContentProvider.CONTENT_URI, cv);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}

    	}
 
    	
    } else {
            Log.d("tag", "Server error in fetching bucket list: "
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
