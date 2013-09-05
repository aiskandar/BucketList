/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kiddobloom.bucketlist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;




/**
 * Activity which displays login screen to the user.
 */
public class DetailedEntryActivity extends Activity  {

	private MyApplication myApp;
	private int bucketListTab;
	private String facebook_id;
	ListView lv;
	ArrayAdapter<String> ad;
	String[] list;
	
	SharedPreferences sp;
    
    /**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle icicle) {

		Intent intent = getIntent();
	    bucketListTab = intent.getIntExtra("com.kiddobloom.bucketlist.current_tab", 0);
	    facebook_id = intent.getStringExtra("com.kiddobloom.bucketlist.facebook_id");
	    
		Log.d("tagaa", "DetailedEntry activity oncreate - bucketListTab: " + bucketListTab + " facebook: " + facebook_id);
		
		super.onCreate(icicle);

		getActionBar().setTitle("Bucket List");
		getActionBar().setSubtitle("by kiddoBLOOM");
		// getActionBar().setDisplayHomeAsUpEnabled(true);

		sp = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		myApp = (MyApplication) getApplication();
		list = new String[0];
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setContentView(R.layout.facebook_friend_detailed);
        Log.d("tagaa", "DetailedEntry activity onresume");
        
        TextView tv = (TextView) findViewById(R.id.fbprofile2);
        
		for (int i=0; i<myApp.friendsList.size() ; i++) {
			//Log.d("tagcf", "add fbid: " + myApp.friendsList.get(i).facebookId);
			if (myApp.friendsList.get(i).facebookId.equals(facebook_id)) {
				String text = myApp.friendsList.get(i).name + "'s" + "\n" + "Bucket List";
				tv.setText(text);
				break;
			}
		}
		
		lv = (ListView) findViewById(R.id.fbFriendDetailListView);
		ad = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1);
		lv.setAdapter(ad);
		
		new GetFriendListTask().execute(facebook_id);
		
	}

	/*
	 * This is the callback function for RegisterTask to bucketlist server
	 */	
	private class GetFriendListTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {

			Log.d("tagaa", "facebook id: " + arg0[0].toString());

			final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("fbid", arg0[0].toString()));

			HttpEntity entity = null;
			HttpResponse resp = null;
			String response = null;

			try {
				entity = new UrlEncodedFormEntity(nvp);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				final HttpPost post = new HttpPost(
						"http://bucketlist.kiddobloom.com/get_friend_list.php");
				post.addHeader(entity.getContentType());
				post.setEntity(entity);

				HttpClient mHttpClient = new DefaultHttpClient();
				resp = mHttpClient.execute(post);
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

			if (resp != null) {
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (response != null) {
						Log.d("tagaa", "server response:" + response);
						
						// decode JSON
						Gson m2Json = new Gson();

						Type type = new TypeToken<String[]>(){}.getType();
						list = m2Json.fromJson(response, type);
						

 
					}
				} else {
					Log.d("tagaa", "server error " + resp.getStatusLine());
					response = "error:" + resp.getStatusLine();
				}
			}
			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
			Log.d("tagaa", "progress: " + progress[0]);
		}

		protected void onPostExecute(String result) {

			if (result != null) {
				boolean error = result.startsWith("error:");
				
				if (error == true) {
					String arr[] = result.split(":");
					
					if (arr.length == 3) {
						Log.d("tagaa", arr[0] + " " + arr[1] + " " + arr[2]);
					} else if (arr.length == 2) {
						Log.d("tagaa", arr[0] + " " + arr[1]);
					} else if (arr.length == 1) {
						Log.d("tagaa", arr[0]);
					}
	
					Toast.makeText(getApplicationContext(),
			                "Failed to get friend's list on the server",
			                Toast.LENGTH_LONG).show();
				} else {
					Log.d("tag", "notify dataset changed");
					
					for(int i=0; i<list.length ; i++) {
						Log.d("tag", "list: " + i + " :" + list[i]);
						ad.add(list[i]);
					}
					ad.notifyDataSetChanged();
				}
			} else {
				// no response from the server
				
				Toast.makeText(getApplicationContext(),
		                "No response from server. Pls check network connection",
		                Toast.LENGTH_LONG).show();

			}
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

        Log.d("tagaa", "DetailedEntry activity onpause");
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

       Log.d("tagaa", "DetailedEntry activity ondestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }
	
}