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
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends Activity implements Request.GraphUserCallback, Request.GraphUserListCallback {

	private AccountManager am;
	private TextView tv;
	public  boolean skip = false;
	private String email;
	private EditText et;
	private MyApplication myApp;
	private int bucketListTab;
	
	SharedPreferences sp;
    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	
    	//Log.d("tagaa", "authenticator facebook onseesionstatechange:" + session + " state:" + state);    	
    	if (session != null) {
    		if (session.isOpened() == true) {
    			saveSkip(false);
    			int st = getState();
    			if (st == StateMachine.FB_CLOSED_STATE || st == StateMachine.INIT_STATE) {
    				saveState(StateMachine.FB_OPENED_STATE);
    				saveStatus(StateMachine.OK_STATUS);
    				saveError(StateMachine.NO_ERROR);

    				getFacebookInfo(session);
    			} else {
    				//Log.d("tagaa", "Ignore the FB session open because prev state: " + StateMachine.stateStr[getState()]);
    			}
    			
    		} else if (session.isClosed() == true){
    			// nothing to do here
    		}
    		
    	}
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle icicle) {

		Intent intent = getIntent();
	    bucketListTab = intent.getIntExtra("com.kiddobloom.bucketlist.current_tab", 0);
	    
		//Log.d("tagaa", "authenticator activity oncreate - bucketListTab: " + bucketListTab);
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		super.onCreate(icicle);
		setProgressBarIndeterminateVisibility(false);
		
		uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(icicle);

		//getActionBar().setTitle(getResources().getString(R.string.app_name));
		getActionBar().setTitle("Bucket List");
	    getActionBar().setSubtitle("by kiddoBLOOM");

		sp = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		am = AccountManager.get(this);
		myApp = (MyApplication) getApplication();
		
		// reset the sync flag to trigger re-sync
		saveInitialSynced(false);
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        uiHelper.onResume();
        isResumed = true;
        //Log.d("tagaa", "authenticator activity onresume");
        

		// only show this screen once when the user installs the app
		// check to see if skip is already saved in preferences
		skip = getSkip();
		//Log.d("tagaa", "skip: " + skip);
		
		if (skip == false) {
			//Log.d("tagaa", "inflating facebook_login");
			// inflate the login screen
			setContentView(R.layout.facebook_login);
			
			// kickstart the state machine
			Session session = Session.getActiveSession();
			if(session != null && session.isOpened() == true) {
				saveState(StateMachine.FB_OPENED_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);

				getFacebookInfo(session);
			} else {
				saveState(StateMachine.FB_CLOSED_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);
			}
		} else {
			saveSkip(true);
			saveState(StateMachine.SKIPPED_STATE);
			saveStatus(StateMachine.OK_STATUS);
			saveError(StateMachine.NO_ERROR);
			goToBucketListActivity();
		}

	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
       uiHelper.onPause();
        isResumed = false;
        //Log.d("tagaa", "authenticator activity onpause");
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       uiHelper.onDestroy();
       //Log.d("tagaa", "authenticator activity ondestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       uiHelper.onSaveInstanceState(outState);
    }
	
    // event handler for skip button
    public void handleSkip(View v) {
    	//Log.d("tagaa", "handleSkip");
    	saveSkip(true);
    	saveState(StateMachine.SKIPPED_STATE);
		saveStatus(StateMachine.OK_STATUS);
		saveError(StateMachine.NO_ERROR);
		goToBucketListActivity();
   }
	
	public void getFacebookInfo(Session session) {
		
		if (!isNetworkAvailable()) {
			//Log.d("tagaa", "network is not available");
			Toast.makeText(getApplicationContext(),
	                "network connection is not available - OFFLINE mode",
	                Toast.LENGTH_LONG).show();
			
			saveState(StateMachine.OFFLINE_STATE);
			saveStatus(StateMachine.ERROR_STATUS);
			saveError(StateMachine.NETWORK_DISCONNECT_ERROR);
			goToBucketListActivity();
		} else {
			// always request facebook user information 
			// in case the user switches the facebook account
			setProgressBarIndeterminateVisibility(true);
			
			saveState(StateMachine.FB_GET_ME_STATE);
			saveStatus(StateMachine.TRANSACTING_STATUS);
			saveError(StateMachine.NO_ERROR);
			Request.executeMeRequestAsync(session, this);
		}		
	}
	
	/*
	 * This is the callback function for RegisterTask to bucketlist server
	 */	
	private class RegisterTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {

			//Log.d("tagaa", "RegisterTask: facebook id = " + arg0[0].toString());

			final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("fbid", arg0[0].toString()));
			//nvp.add(new BasicNameValuePair("fbid", "100001573160170"));

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
						"http://bucketlist.kiddobloom.com/register.php");
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
						//Log.d("tagaa", "RegisterTask: server response = " + response);
					}
				} else {
					//Log.d("tagaa", "RegisterTask: server error = " + resp.getStatusLine());
					response = "error:" + resp.getStatusLine();
				}
			}
			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
			//Log.d("tagaa", "RegisterTask: progress = " + progress[0]);
		}

		protected void onPostExecute(String result) {

			if (result != null) {
				boolean error = result.startsWith("error:");
				
				if (error == true) {
					String arr[] = result.split(":");
					
					if (arr.length == 3) {
						//Log.d("tagaa", arr[0] + " " + arr[1] + " " + arr[2]);
					} else if (arr.length == 2) {
						//Log.d("tagaa", arr[0] + " " + arr[1]);
					} else if (arr.length == 1) {
						//Log.d("tagaa", arr[0]);
					}
	
					Toast.makeText(getApplicationContext(),
			                "Failed to register userid on the server - OFFLINE mode",
			                Toast.LENGTH_LONG).show();
					
					saveState(StateMachine.OFFLINE_STATE);
					saveStatus(StateMachine.ERROR_STATUS);
					saveError(StateMachine.FBID_SERVER_REGISTER_ERROR);
					goToBucketListActivity();
	
				} else {
					//Log.d("tagaa", "RegisterTask: completed facebook id registration");
					
					// save the registered flag to true in preferences db
					saveUserIdRegistered(true);
					
					saveState(StateMachine.ONLINE_STATE);
					saveStatus(StateMachine.OK_STATUS);
					saveError(StateMachine.NO_ERROR);
					goToBucketListActivity();
				}
			} else {
				// no response from the server
				
				Toast.makeText(getApplicationContext(),
		                "No response from server. Pls check network connection - OFFLINE mode",
		                Toast.LENGTH_LONG).show();
				
				saveState(StateMachine.OFFLINE_STATE);
				saveStatus(StateMachine.ERROR_STATUS);
				saveError(StateMachine.FBID_SERVER_REGISTER_ERROR);
				goToBucketListActivity();
			}
		}
	}
	
	/*
	 * This is the callback function for FB_GET_FRIENDS request
	 */	
	@Override
	public void onCompleted(List<GraphUser> users, Response response) {
		
		//Log.d("tagaa", "FacebookGetFriends: oncompleted friends req");
		
		if (response != null) {
			
			FacebookRequestError error = response.getError();
			if (error != null) {
				// failed to get user info from facebook - TOAST
				//Log.d("tagaa", "FacebookGetFriends: failed to get friendlist from facebook: " + error);
				
				Toast.makeText(getApplicationContext(),
		                "Failed to retrieve friends list from Facebook - OFFLINE mode",
		                Toast.LENGTH_SHORT).show();
				
				// I may need to store this permanently in the preference list
				saveState(StateMachine.OFFLINE_STATE);
				saveStatus(StateMachine.ERROR_STATUS);
				saveError(StateMachine.FB_GET_FRIENDS_FAILED_ERROR);
				goToBucketListActivity();
				return;
			}
		}
		if (users != null) {
			
			myApp.friendsList.clear();
			
			for (int i=0 ; i < users.size() ; i++) {
				////Log.d("tagaa", "name:" + users.get(i).getName());
				//Log.d("tagaa", "userid:" + users.get(i).getId());
				
				FriendData fd = new FriendData();
				fd.name = users.get(i).getName();
				fd.facebookId = users.get(i).getId();
				fd.bucketList = new String[50];
				fd.notified = false;
				fd.allPrivate = false;
				
				if(myApp.friendsList != null) {
					myApp.friendsList.add(fd);
				}
			}
			
			// skip registration if facebook ID is already registered	
			if (getUserIdRegistered() == false) {
				// register the userid on the server
				saveState(StateMachine.FBID_REGISTER_STATE);
				saveStatus(StateMachine.TRANSACTING_STATUS);
				saveError(StateMachine.NO_ERROR);
				new RegisterTask().execute(getFbUserId());
			} else {
				
				saveState(StateMachine.ONLINE_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);
				goToBucketListActivity();
			}
			
		} else {
			
			// failed to get user info from facebook - TOAST
			//Log.d("tagaa", "FacebookGetFriends: failed to get friends list from facebook");
			
			Toast.makeText(getApplicationContext(),
	                "Failed to retrieve friends list from Facebook - OFFLINE mode",
	                Toast.LENGTH_SHORT).show();

			saveState(StateMachine.OFFLINE_STATE);
			saveStatus(StateMachine.ERROR_STATUS);
			saveError(StateMachine.FB_GET_FRIENDS_FAILED_ERROR);
			goToBucketListActivity();
		}
		
	}
	
	/*
	 * This is the callback function for FB_GET_ME request
	 */
	@Override
	public void onCompleted(GraphUser user, Response response) {
		// TODO Auto-generated method stub
		//Log.d("tagaa", "FacebookGetMe: oncomplete me request");
		
		if (response != null) {
			
			FacebookRequestError error = response.getError();
			if (error != null) {
				// failed to get user info from facebook - TOAST
				//Log.d("tagaa", "FacebookGetMe: failed to get user info from facebook: " + error);
				
				Toast.makeText(getApplicationContext(),
		                "Failed to retrieve information from Facebook - OFFLINE mode",
		                Toast.LENGTH_SHORT).show();
				
				saveState(StateMachine.OFFLINE_STATE);
				saveStatus(StateMachine.ERROR_STATUS);
				saveError(StateMachine.FB_GET_ME_FAILED_ERROR);
				goToBucketListActivity();
				return;
			}
		}
		
		if (user != null) {
			
			boolean registered = false;
			final Account account;
			
			// if we get to this point, we know that the network is OK
			// we can continue server registration
			
			//Log.d("tagaa", "FacebookGetMe: me = " + user);
			
			// check whether (com.kidobloom) type account has been created for the fb-userid
			// if account db is empty - create a new account using the fb-userid
			// else if an account already exists, check to see if it matches the current fb-userid
			// if account exists and matches the fb-userid, do nothing
			// otherwise replace the account with the new fb-userid
			AccountManager accountManager = AccountManager.get(getApplicationContext());
			Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");

			if (accounts.length <= 0) {
				//Log.d("tagaa", "FacebookGetMe: no account exists");
				// create a new account
				account = new Account(user.getId(), Constants.ACCOUNT_TYPE);
				am.addAccountExplicitly(account, null, null);
				ContentResolver.setSyncAutomatically(account, MyContentProvider.AUTHORITY, true);
				registered = false;
			} else {
				// get the first account
				//Log.d("tagaa", "FacebookGetMe: account = " + accounts[0].name);

				if (accounts[0].name.equals(user.getId())) {
					registered = true;
					//Log.d("tagaa", "FacebookGetMe: account for facebookId = " + user.getId() + " already created");
				} else {
					//Log.d("tag", "FacebookGetMe: user switched account");
					// remove the account first
					am.removeAccount(accounts[0], null, null);

					// add new account with the new facebook userid
					account = new Account(user.getId(), Constants.ACCOUNT_TYPE);
					am.addAccountExplicitly(account, null, null);
					ContentResolver.setSyncAutomatically(account, MyContentProvider.AUTHORITY, true);
					
					// update the registered flag so facebook ID gets re-registered
					registered = false;
				}
			}
			
			// at this point, an account should already be created
			// store the userid and registered boolean in preferences db
			saveFbUserId(user.getId());
			saveUserIdRegistered(registered);
			
			saveState(StateMachine.FB_GET_FRIENDS_STATE);
			saveStatus(StateMachine.TRANSACTING_STATUS);
			saveError(StateMachine.NO_ERROR);
			
			// request facebook friends list
			Request.executeMyFriendsRequestAsync(response.getRequest().getSession(), this);

		} else {
			// throw an exception here - facebook does not indicate error but user is null 
			//Log.d("tagaa", "FacebookGetMe: failed to get user info from facebook - OFFLINE mode");
			
			Toast.makeText(getApplicationContext(),
	                "Failed to retrieve information from Facebook",
	                Toast.LENGTH_SHORT).show();
			
			saveState(StateMachine.OFFLINE_STATE);
			saveStatus(StateMachine.ERROR_STATUS);
			saveError(StateMachine.FB_GET_ME_FAILED_ERROR);
			goToBucketListActivity();
		}	
	}	
	
	public void goToBucketListActivity() {
		
		setProgressBarIndeterminateVisibility(false);
		
		Intent launch = new Intent(this, BucketListActivity.class);
		launch.putExtra("current_tab", bucketListTab);
		startActivity(launch);
		finish();		
	}
	
	/* 
	 * Preferences settings GET/SET methods
	 * Need to find a way so these GET/SET methods can be used across activities
	 */
	
	// skip
	public void saveSkip(boolean skip) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_skip_key), skip);
		editor.commit();
	}
	
	public Boolean getSkip() {
		return sp.getBoolean(getString(R.string.pref_skip_key), false);
	}
	
	// state
	public void saveState(int state) {
		//Log.d("tagaa", "authenticator state change " + StateMachine.stateStr[state]);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(getString(R.string.pref_state_key), state);
		editor.commit();
	}
	
	public int getState() {
		return sp.getInt(getString(R.string.pref_state_key), 100);
	}

	// status
	public void saveStatus(int status) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(getString(R.string.pref_status_key), status);
		editor.commit();
	}
	
	public int getStatus() {
		return sp.getInt(getString(R.string.pref_status_key), 100);	
	}

	// error
	public void saveError(int error) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(getString(R.string.pref_error_key), error);
		editor.commit();
	}

	public int getError() {
		return sp.getInt(getString(R.string.pref_error_key), 100);
	}
	
	// fb_userid
	public void saveFbUserId(String fbUserid) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(getString(R.string.pref_fb_userid_key), fbUserid);
		editor.commit();
	}

	public String getFbUserId() {
		return sp.getString(getString(R.string.pref_fb_userid_key), "invalid");
	}

	// fb_userCity
	public void saveFbUserCity(String city) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(getString(R.string.pref_fb_user_city_key), city);
		editor.commit();
	}

	public String getFbUserCity(String State) {
		return sp.getString(getString(R.string.pref_fb_user_city_key), "invalid");
	}
	
	// fb_userstate
	public void saveFbUserState(String state) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(getString(R.string.pref_fb_user_state_key), state);
		editor.commit();
	}

	public String getFbUserState(String State) {
		return sp.getString(getString(R.string.pref_fb_user_state_key), "invalid");
	}
	
	// fb_usercountry
	public void saveFbUserCountry(String country) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(getString(R.string.pref_fb_user_country_key), country);
		editor.commit();
	}

	public String getFbUserCountry(String State) {
		return sp.getString(getString(R.string.pref_fb_user_country_key), "invalid");
	}

	// userid_registered
	public void saveUserIdRegistered(boolean value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_userid_registered_key), value);
		editor.commit();
	}

	public boolean getUserIdRegistered() {
		return sp.getBoolean(getString(R.string.pref_userid_registered_key), false);
	}	
	
	// initial_sync
	public void saveInitialSynced(boolean value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_initial_synced_key), value);
		editor.commit();
	}

	public boolean getInitialSynced() {
		return sp.getBoolean(getString(R.string.pref_initial_synced_key), false);
	}		

	
}