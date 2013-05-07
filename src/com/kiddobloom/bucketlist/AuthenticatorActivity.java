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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;


/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends SherlockActivity implements Request.GraphUserCallback, Request.GraphUserListCallback {
//public class AuthenticatorActivity extends Activity {

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
    	
    	Log.d("tag", "authenticator facebook onseesionstatechange:" + session + " state:" + state);    	
    	if (session != null) {
    		if (session.isOpened() == true) {
    			processStateMachine(StateMachine.LOGIN_SUCCESS_EVENT);
    		} else if (session.isClosed() == true){
    			processStateMachine(StateMachine.LOGOUT_SUCCESS_EVENT);
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
	    
		Log.d("tag", "authenticator activity oncreate - bucketListTab: " + bucketListTab);
		
		super.onCreate(icicle);
		uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(icicle);

		getSupportActionBar().setTitle("Bucket List");
		getSupportActionBar().setSubtitle("by kiddoBLOOM");

		sp = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		am = AccountManager.get(this);
		myApp = (MyApplication) getApplication();
		
		// only show this screen once when the user installs the app
		// check to see if skip is already saved in preferences
		skip = sp.getBoolean(getString(R.string.pref_skip_key), false);
		Log.d("tag", "skip: " + skip);
		
		if (skip == false) {
			Log.d("tag", "inflating facebook_login");
			// inflate the login screen
			setContentView(R.layout.facebook_login);
			
			// kickstart the state machine
			Session session = Session.getActiveSession();

			if(session != null && session.isOpened() == true) {
				processStateMachine(StateMachine.LOGIN_SUCCESS_EVENT);
			} else {
				processStateMachine(StateMachine.LOGOUT_SUCCESS_EVENT);
			}
			
		} else {
			
			// kickstart the state machine
			processStateMachine(StateMachine.SKIP_EVENT);
		}
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
        Log.d("tag", "authenticator activity onresume");

	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
       uiHelper.onPause();
        isResumed = false;
        Log.d("tag", "authenticator activity onpause");
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
       Log.d("tag", "authenticator activity ondestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       uiHelper.onSaveInstanceState(outState);
    }
    
    // event handler for skip button
    public void handleSkip(View v) {
    	Log.d("tag", "handleSkip");
		processStateMachine(StateMachine.SKIP_EVENT);
    }
	
	public void getFacebookInfo(Session session) {
		// request user information
		Request.executeMeRequestAsync(session, this);
	}
	
	private class RegisterTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {

			Log.d("tag", "facebook id: " + arg0[0].toString());

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
						"http://andyiskandar.me/register.php");
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

			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (response != null) {
					Log.d("tag", "server response:" + response);
				}
			} else {
				Log.d("tag", "Server error " + resp.getStatusLine());
				response = "error:" + resp.getStatusLine();
			}

			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
			Log.d("tag", "progress: " + progress[0]);
		}

		protected void onPostExecute(String result) {

			boolean error = result.startsWith("error:");
			
			if (error == true) {
				String arr[] = result.split(":");
				
				if (arr.length == 3) {
					Log.d("tag", arr[0] + " " + arr[1] + " " + arr[2]);
				} else if (arr.length == 2) {
					Log.d("tag", arr[0] + " " + arr[1]);
				} else if (arr.length == 1) {
					Log.d("tag", arr[0]);
				}

				Toast.makeText(getApplicationContext(),
		                "Failed to register userid on the server",
		                Toast.LENGTH_LONG).show();
				// I may need to store this permanently in the preference list
				myApp.lastKnownError = StateMachine.SERVER_REGISTER_ERROR;
				processStateMachine(StateMachine.SKIP_EVENT);
			} else {
				Log.d("tag", "facebook id is registered");
				
				// save the registered flag to true in preferences db
				saveUserId("FB", true);
				
				goToBucketListActivity();
			}
		}
	}
	
	private void saveSkip(boolean skip) {

		Log.d("tag", "authenticator saveskip: " + skip);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_skip_key), skip);
		editor.commit();

	}

	private void saveUserId(String userId, boolean registered) {

		Log.d("tag", "authenticator saveUserId: " + userId + " registered: " + registered);
		
		SharedPreferences.Editor editor = sp.edit();
		if (!userId.equals("FB")) {
			editor.putString(getString(R.string.pref_userid_key), userId);
		}
		editor.putBoolean(getString(R.string.pref_userid_registered_key), registered);
		editor.commit();
		
	}

	public void processStateMachine(int event) {
		
		Log.d("tag", "processing state machine authenticator activity");

		switch (myApp.currentState) {
		
		case StateMachine.INIT_STATE:			
			processStateMachineInitState(event);
			break;
	
		case StateMachine.SKIPPED_STATE:
			processStateMachineSkippedState(event);
			break;
			
		case StateMachine.OPENED_STATE:
			processStateMachineOpenedState(event);
			break;
			
		case StateMachine.CLOSED_STATE:
			processStateMachineClosedState(event);
			break;
			
		default:
			Log.d("tag", "unknown state - throw exception");
			break;
		}

		// get the next state
		myApp.currentState = myApp.nextState[event][myApp.currentState];
		Log.d("tag", "new state: " + StateMachine.stateStr[myApp.currentState]);
		
	}

	private void processStateMachineInitState(int event) {

		Log.d("tag", "state machine INIT state - event: " + StateMachine.eventStr[event]);
		Session session = Session.getActiveSession();
		
		switch (event) {
		
		case StateMachine.LOGIN_SUCCESS_EVENT:
			// we need to save in pref that the user reverses their decision to skip 
			// the facebook login because he is now logged in to facebook
			saveSkip(false);
			
			// get all the data from facebook
			getFacebookInfo(session);				
			break;
		case StateMachine.LOGOUT_SUCCESS_EVENT:
			break;
		case StateMachine.SKIP_EVENT:
			saveSkip(true);
			goToBucketListActivity();
			break;
		default:
			Log.d("tag", "unknown event - throw exception");
			break;
		}
				
	}

	private void processStateMachineOpenedState(int event) {
		Log.d("tag", "state machine OPENED state - event: " + StateMachine.eventStr[event]);
		
		Intent launch;
		Session session = Session.getActiveSession();
		
		switch (event) {
		case StateMachine.LOGIN_SUCCESS_EVENT:
			// start over
			myApp.friendsList.clear();
			getFacebookInfo(session);
			break;
		case StateMachine.LOGOUT_SUCCESS_EVENT:
			break;
		case StateMachine.SKIP_EVENT:
			saveSkip(true);
			goToBucketListActivity();
			break;
		default:
			Log.d("tag", "unknown event - throw exception");
			break;
		}
		
	}

	private void processStateMachineClosedState(int event) {

		Log.d("tag", "state machine CLOSED state - event: " + StateMachine.eventStr[event]);		
		Session session = Session.getActiveSession();
		
		switch (event) {
		case StateMachine.LOGIN_SUCCESS_EVENT:
			saveSkip(false);
			// get all the data from facebook
			getFacebookInfo(session);	
			break;
		case StateMachine.LOGOUT_SUCCESS_EVENT:
			break;
		case StateMachine.SKIP_EVENT:
			saveSkip(true);
			goToBucketListActivity();
			break;
		default:
			Log.d("tag", "unknown event - throw exception");
			break;
		}

	}

	private void processStateMachineSkippedState(int event) {
		Log.d("tag", "state machine SKIPPED state - event: " + StateMachine.eventStr[event]);
		
		switch (event) {
		case StateMachine.LOGIN_SUCCESS_EVENT:
			break;
		case StateMachine.LOGOUT_SUCCESS_EVENT:
			break;
		case StateMachine.SKIP_EVENT:
			saveSkip(true);
			goToBucketListActivity();
			break;
		default:
			Log.d("tag", "unknown event - throw exception");
			break;
		}
		
	}

	@Override
	public void onCompleted(List<GraphUser> users, Response response) {
		
		Log.d("tag", "oncompleted friends req");
		
		if (response != null) {
			
			FacebookRequestError error = response.getError();
			if (error != null) {
				// failed to get user info from facebook - TOAST
				Log.d("tag", "failed to get friendlist from facebook: " + error);
				
				Toast.makeText(getApplicationContext(),
		                "Failed to retrieve friends list from Facebook",
		                Toast.LENGTH_SHORT).show();
				// I may need to store this permanently in the preference list
				myApp.lastKnownError = StateMachine.FB_GET_FRIENDS_FAILED_ERROR;
				processStateMachine(StateMachine.SKIP_EVENT);
			}
		}
		
		if (users != null) {
			
			for (int i=0 ; i < users.size() ; i++) {
				//Log.d("tag", "name:" + users.get(i).getName());
				//Log.d("tag", "userid:" + users.get(i).getId());
				
				FriendData fd = new FriendData();
				fd.name = users.get(i).getName();
				fd.userId = users.get(i).getId();
				
				if(myApp.friendsList != null) {
					myApp.friendsList.add(fd);
				}
			}
			
			// retrieve the facebook userid from preferences db
			String userId = sp.getString(getString(R.string.pref_userid_key), "");
			boolean reg = sp.getBoolean(getString(R.string.pref_userid_registered_key), false);
			
			if (reg == false) {
				// register the userid on the server
				new RegisterTask().execute(userId);
			} else {
				goToBucketListActivity();
			}
			

			
		} else {
			
			// failed to get user info from facebook - TOAST
			Log.d("tag", "failed to get friends list from facebook");
			
			Toast.makeText(getApplicationContext(),
	                "Failed to retrieve friends list from Facebook",
	                Toast.LENGTH_SHORT).show();
			myApp.lastKnownError = StateMachine.FB_GET_FRIENDS_FAILED_ERROR;
			processStateMachine(StateMachine.SKIP_EVENT);

		}
		
	}

	@Override
	public void onCompleted(GraphUser user, Response response) {
		// TODO Auto-generated method stub
		Log.d("tag", "oncomplete me request");
		
		if (response != null) {
			
			FacebookRequestError error = response.getError();
			if (error != null) {
				// failed to get user info from facebook - TOAST
				Log.d("tag", "failed to get user info from facebook: " + error);
				
				Toast.makeText(getApplicationContext(),
		                "Failed to retrieve information from Facebook",
		                Toast.LENGTH_SHORT).show();
				myApp.lastKnownError = StateMachine.FB_GET_ME_FAILED_ERROR;
				processStateMachine(StateMachine.SKIP_EVENT);
			}
		}
		
		if (user != null) {
			
			boolean registered = false;
			final Account account;
			
			// if we get to this point, we know that the network is OK
			// we can continue server registration
			
			Log.d("tag", "me: " + user);
			// check whether (com.kidobloom) type account has been created for the fb-userid
			// if account db is empty - create a new account using the fb-userid
			// else if an account already exists, check to see if it matches the current fb-userid
			// if account exists and matches the fb-userid, do nothing
			// otherwise replace the account with the new fb-userid
			AccountManager accountManager = AccountManager.get(getApplicationContext());
			Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");

			if (accounts.length <= 0) {
				Log.d("tag", "no account exists");
				// create a new account
				account = new Account(user.getId(), Constants.ACCOUNT_TYPE);
				am.addAccountExplicitly(account, null, null);
				//ContentResolver.setSyncAutomatically(account, MyContentProvider.AUTHORITY, true);
				registered = false;
			} else {
				// get the first account
				Log.d("tag", "account: " + accounts[0].name);

				if (accounts[0].name.equals(user.getId())) {
					registered = true;
					Log.d("tag", "account for usedid: " + user.getId() + " already created");
				} else {
					// remove the account first
					am.removeAccount(accounts[0], null, null);

					// add new account with the new facebook userid
					account = new Account(user.getId(), Constants.ACCOUNT_TYPE);
					am.addAccountExplicitly(account, null, null);
					//ContentResolver.setSyncAutomatically(account, MyContentProvider.AUTHORITY, true);
					registered = false;
				}
			}
			
			// at this point, an account should already be created
			// store the userid and registered boolean in preferences db
			saveUserId(user.getId(), registered);
						
			// request facebook friends list
			Request.executeMyFriendsRequestAsync(response.getRequest().getSession(), this);

		} else {
			// throw an exception here - facebook does not indicate error but user is null 
			Log.d("tag", "failed to get user info from facebook - user is null");
			
			Toast.makeText(getApplicationContext(),
	                "Failed to retrieve information from Facebook",
	                Toast.LENGTH_SHORT).show();
			myApp.lastKnownError = StateMachine.FB_GET_ME_FAILED_ERROR;
			processStateMachine(StateMachine.SKIP_EVENT);
			
		}	
	}	
	
	public void goToBucketListActivity() {
		
		Intent launch = new Intent(this, BucketListActivity.class);
		launch.putExtra("current_tab", bucketListTab);
		startActivity(launch);
		finish();		
	}
}