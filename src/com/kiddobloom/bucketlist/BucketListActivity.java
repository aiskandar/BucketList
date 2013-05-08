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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;


public class BucketListActivity extends SherlockFragmentActivity implements TabListener, Request.GraphUserCallback, Request.GraphUserListCallback {

	SharedPreferences sp;
	AccountManager am;
	MyApplication myApp;
	FragmentManager fm;
	Fragment[] fmList = new Fragment[4];
	Handler mHandler;
	Runnable mUpdate;
	
	int currentTab;
	
	private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    
    private static final int MYLIST_FRAGMENT_IDX = 0;
    private static final int COMMUNITY_FRAGMENT_IDX = 1;
    private static final int COMMUNITY_OFFLINE_FRAGMENT_IDX = 2;
    private static final int ABOUT_FRAGMENT_IDX = 3;
    
	private static final int MYLIST_NAV_TAB_IDX = 0;
	private static final int COMMUNITY_NAV_TAB_IDX = 1;
	private static final int ABOUT_NAV_TAB_IDX = 2;
	
	private static final int TIMER_INTERVAL = 10000; // in miliseconds
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	
    	Log.d("tag", "bucketlist facebook onseesionstatechange:" + session + " state:" + state);

    	if (session != null) {
    		if (session.isOpened() == true) {
				saveState(StateMachine.FB_OPENED_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);
				saveSkip(false);
				// disable timer
				mHandler.removeCallbacks(mUpdate);
				getFacebookInfo();
    		} else if (session.isClosed() == true){
				saveState(StateMachine.FB_CLOSED_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);
    			goToAuthenticatorActivity();
    		}
    	}
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
	    currentTab = intent.getIntExtra("current_tab", 0);
	    
		if (savedInstanceState != null) {
			currentTab = savedInstanceState.getInt("currentTab");
		} 
		
		setContentView(R.layout.main_layout);
		
		fm = getSupportFragmentManager();

		fmList[MYLIST_FRAGMENT_IDX] = fm.findFragmentById(R.id.mylistFragment);
		fmList[COMMUNITY_FRAGMENT_IDX] = fm.findFragmentById(R.id.commFragment);
		fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX] = fm.findFragmentById(R.id.commOfflineFragment);
		fmList[ABOUT_FRAGMENT_IDX] = fm.findFragmentById(R.id.aboutFragment);

		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fmList.length; i++) {
			transaction.hide(fmList[i]);
		}
		transaction.commit();
			
		uiHelper = new UiLifecycleHelper(this, callback);		
		uiHelper.onCreate(savedInstanceState);

		Log.d("tag", "bucketlist activity created - currentTab: " + currentTab);

		sp = getSharedPreferences(getString(R.string.pref_name), 0);

		am = AccountManager.get(this);
		myApp = (MyApplication) getApplication();
		
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        NavigationTabs.myListTab = getSupportActionBar().newTab();
        NavigationTabs.myListTab.setText("My List");
        NavigationTabs.myListTab.setTabListener(this);
        getSupportActionBar().addTab(NavigationTabs.myListTab, false);
 
        NavigationTabs.communityTab = getSupportActionBar().newTab();
        NavigationTabs.communityTab.setText("Community");
        NavigationTabs.communityTab.setTabListener(this);   
        getSupportActionBar().addTab(NavigationTabs.communityTab, false);
        
        NavigationTabs.aboutTab = getSupportActionBar().newTab();
        NavigationTabs.aboutTab.setText("About");
        NavigationTabs.aboutTab.setTabListener(this);
        getSupportActionBar().addTab(NavigationTabs.aboutTab, false);        
 
		getSupportActionBar().setTitle("Bucket List");
		getSupportActionBar().setSubtitle("by kiddoBLOOM");
		
		mHandler = new Handler();
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		Log.d("tag", "menu item selected :" + item.getItemId());
		
		int id = item.getItemId();
		
		if (id == R.id.menu_logout) {

			Session session = Session.getActiveSession();
			if (session != null && session.isOpened() == true) {
				session.closeAndClearTokenInformation();
				myApp.friendsList.clear();	
			} else {
				// toast
				Log.d("tag", "toast: you are not logged in to facebook - ");
				Toast.makeText(this,
		                "You are not currently Logged in to Facebook",
		                Toast.LENGTH_SHORT).show();
			}

		} else if (id == R.id.menu_update) {
			MyListFragment mf = (MyListFragment) fmList[MYLIST_FRAGMENT_IDX];
			mf.sync();
		} else if (id == R.id.menu_preferences) {
			Intent i = new Intent(this, PreferencesActivity.class);
			startActivityForResult(i, 0);
		}
		
		return true;
	}	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        uiHelper.onResume();
        isResumed = true;
        Log.d("tag", "bucketlist activity is resumed - currentTab: " + currentTab);
        
		switch(currentTab) {
		case MYLIST_NAV_TAB_IDX:
			NavigationTabs.myListTab.select();
			break;
		case COMMUNITY_NAV_TAB_IDX:
			NavigationTabs.communityTab.select();
			break;
		case ABOUT_NAV_TAB_IDX:
			NavigationTabs.aboutTab.select();
			break;
		default:
			//throw exception here
		}	
		
		// set a timer for 60 seconds and check how we get here 
        mUpdate = new Runnable() {
        	
        	public void run() {
        		
        		// are we in online, offline, or skip state?
        		if (getState() == StateMachine.OFFLINE_STATE) {
        			Log.d("tag", "timer event offline");
        			// most likely there is an error
        			if (getStatus() == StateMachine.ERROR_STATUS) {

        				if (!isNetworkAvailable()) {
        					Log.d("tag", "network is not available");
        				} else {
	        				
	        				switch (getError()) {
	        					case StateMachine.NETWORK_DISCONNECT_ERROR:
	        						Log.d("tag", "retry - network disconnect error");
	        						getFacebookInfo();
	        						break;
								case StateMachine.FB_GET_ME_FAILED_ERROR:
									Log.d("tag", "retry - fb get me failed error");
									getFacebookInfo();
									break;
								case StateMachine.FB_GET_FRIENDS_FAILED_ERROR:
									Log.d("tag", "retry - fb get friends failed error");
									getFacebookFriends();
									break;
								case StateMachine.FBID_SERVER_REGISTER_ERROR:
									Log.d("tag", "retry - server register error");
									registerUserId();
									break;
								default:
		    				}
        				}
        				
        				// restart timer
        				mHandler.removeCallbacks(this);
        		        mHandler.postDelayed(this, TIMER_INTERVAL); 
	            	}
        		} else if (getState() == StateMachine.ONLINE_STATE) {
        			// it seems our timer callback has successfully delivered us to ONLINE mode
        			// re-select the tab and DO not restart timer
        			Log.d("tag", "timer event online");
        			
        			switch(currentTab) {
        			case MYLIST_NAV_TAB_IDX:
        				NavigationTabs.myListTab.select();
        				break;
        			case COMMUNITY_NAV_TAB_IDX:
        				NavigationTabs.communityTab.select();
        				break;
        			case ABOUT_NAV_TAB_IDX:
        				NavigationTabs.aboutTab.select();
        				break;
        			default:
        				//throw exception here
        			}	
    			}  
			} 
		};
		
		if (getState() == StateMachine.OFFLINE_STATE) {
			// most likely there is an error
			if (getStatus() == StateMachine.ERROR_STATUS) {
				Log.d("tag", "trigger a timer to retry");
				mHandler.removeCallbacks(mUpdate);
				mHandler.postDelayed(mUpdate, TIMER_INTERVAL);
			}
		}
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	    uiHelper.onPause();
	    isResumed = false;
	        
	    // remove timer callback if the activity is paused
	    mHandler.removeCallbacks(mUpdate);
		Log.d("tag", "bucketlist activity is paused");
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
       
       saveInitialSynced(false);
       
       Log.d("tag", "bucketlist activity ondestroy");
    }

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("tag", "bucketlist activity is stopped");


	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("tag", "bucketlist activity is started");

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		Log.d("tag", "bucketlist activity onsaveinstancestate");
		

	 	   // save the currently selected fragment
	 	   outState.putInt("currentTab", currentTab);
	 	   
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		Log.d("tag", "bucketlist activity onrestoreinstancestate");

		super.onRestoreInstanceState(state);
	}

	public void handleVisit(View v) {
		Log.d("tag", "handle visit");
		
	    TextView t2 = (TextView) v;
	    t2.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	public void handleFbLike(View v) {
		Log.d("tag", "handle FB like");
		
		String kiddobloom_fb_id = "208086305947271";
		String uri = "fb://page/" + kiddobloom_fb_id;
		Log.d("tag", "uri: " +uri);
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));		
		startActivity(intent);
		
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

	    Session session = Session.getActiveSession();
	       
		if (tab.getText().equals("My List")) {
			Log.d("tag", "my list selected");
			
			ft.show(fmList[MYLIST_FRAGMENT_IDX]);	
			MyListFragment mf = (MyListFragment) fmList[MYLIST_FRAGMENT_IDX];
			mf.refreshList();
			
			ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
			ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			ft.hide(fmList[ABOUT_FRAGMENT_IDX]);
			
			currentTab = MYLIST_NAV_TAB_IDX;
			
		} else if (tab.getText().equals("Community")) {
			Log.d("tag", "community selected");

			ft.hide(fmList[MYLIST_FRAGMENT_IDX]);	
			
			if(getState() == StateMachine.ONLINE_STATE) {
				ft.show(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
				CommunityFragment cf = (CommunityFragment) fmList[COMMUNITY_FRAGMENT_IDX];
				cf.dataChange();
			} else {
				ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.show(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			}
			
			ft.hide(fmList[ABOUT_FRAGMENT_IDX]);
			
			currentTab = COMMUNITY_NAV_TAB_IDX;

		} else {
			Log.d("tag", "about selected");
			
			ft.hide(fmList[MYLIST_FRAGMENT_IDX]);			
			ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
			ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			ft.show(fmList[ABOUT_FRAGMENT_IDX]);
			
			currentTab = ABOUT_NAV_TAB_IDX;
		}
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
		if (tab.getText().equals("Community")) {
			Log.d("tag", "community re-selected");
			
			Session session = Session.getActiveSession();
			if(getState() == StateMachine.ONLINE_STATE) {
				ft.show(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
				CommunityFragment cf = (CommunityFragment) fmList[COMMUNITY_FRAGMENT_IDX];
				cf.dataChange();
			} else {
				ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.show(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			}
		}
	
	}
	
	public void getFacebookInfo() {
		
		Session session = Session.getActiveSession();
		
		if (session != null && session.isOpened()) {		
			saveState(StateMachine.FB_GET_ME_STATE);
			saveStatus(StateMachine.TRANSACTING_STATUS);
			saveError(StateMachine.NO_ERROR);
			Request.executeMeRequestAsync(session, this);
		}
	}
	
	public void getFacebookFriends() {
		
		// this is called from error state
		// if the app error state is failed to get facebook friends
		// clear facebook friends first before 
		myApp.friendsList.clear();
		
		Session session = Session.getActiveSession();
		
		if (session != null && session.isOpened()) {		
		
			saveState(StateMachine.FB_GET_FRIENDS_STATE);
			saveStatus(StateMachine.TRANSACTING_STATUS);
			saveError(StateMachine.NO_ERROR);
			Request.executeMyFriendsRequestAsync(session, this);
		}		
	}
	
	public void registerUserId() {
		
		saveState(StateMachine.FBID_REGISTER_STATE);
		saveStatus(StateMachine.TRANSACTING_STATUS);
		saveError(StateMachine.NO_ERROR);
		new RegisterTask().execute(getFbUserId());
		
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

			if (resp != null) {
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (response != null) {
						Log.d("tag", "server response:" + response);
					}
				} else {
					Log.d("tag", "server error " + resp.getStatusLine());
					response = "error:" + resp.getStatusLine();
				}
			}
			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
			Log.d("tag", "progress: " + progress[0]);
		}

		protected void onPostExecute(String result) {

			if (result != null) {
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
			                "Failed to register userid on the server - OFFLINE mode",
			                Toast.LENGTH_LONG).show();
					
					saveState(StateMachine.OFFLINE_STATE);
					saveStatus(StateMachine.ERROR_STATUS);
					saveError(StateMachine.FBID_SERVER_REGISTER_ERROR);
					//goToBucketListActivity();
	
				} else {
					Log.d("tag", "facebook id is registered");
					
					// save the registered flag to true in preferences db
					saveUserIdRegistered(true);
					
					saveState(StateMachine.ONLINE_STATE);
					saveStatus(StateMachine.OK_STATUS);
					saveError(StateMachine.NO_ERROR);
					
	    			switch(currentTab) {
	    			case MYLIST_NAV_TAB_IDX:
	    				NavigationTabs.myListTab.select();
	    				break;
	    			case COMMUNITY_NAV_TAB_IDX:
	    				NavigationTabs.communityTab.select();
	    				break;
	    			case ABOUT_NAV_TAB_IDX:
	    				NavigationTabs.aboutTab.select();
	    				break;
	    			default:
	    				//throw exception here
	    			}	
					//goToBucketListActivity();
				}
			} else {
				// no response from the server
				saveState(StateMachine.OFFLINE_STATE);
				saveStatus(StateMachine.ERROR_STATUS);
				saveError(StateMachine.FBID_SERVER_REGISTER_ERROR);
				//goToBucketListActivity();
			}
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
		                "Failed to retrieve friends list from Facebook - OFFLINE mode",
		                Toast.LENGTH_SHORT).show();
				
				// I may need to store this permanently in the preference list
				saveState(StateMachine.OFFLINE_STATE);
				saveStatus(StateMachine.ERROR_STATUS);
				saveError(StateMachine.FB_GET_FRIENDS_FAILED_ERROR);
			//	goToBucketListActivity();
				return;
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
				
    			switch(currentTab) {
    			case MYLIST_NAV_TAB_IDX:
    				NavigationTabs.myListTab.select();
    				break;
    			case COMMUNITY_NAV_TAB_IDX:
    				NavigationTabs.communityTab.select();
    				break;
    			case ABOUT_NAV_TAB_IDX:
    				NavigationTabs.aboutTab.select();
    				break;
    			default:
    				//throw exception here
    			}	
				
				// HACK xxxxxxxxxxxxxxxxxxxxxxxxxx
//				saveState(StateMachine.OFFLINE_STATE);
//				saveStatus(StateMachine.ERROR_STATUS);
//				saveError(StateMachine.FB_GET_ME_FAILED_ERROR);
				//goToBucketListActivity();
				//goToBucketListActivity();
			}
			
		} else {
			
			// failed to get user info from facebook - TOAST
			Log.d("tag", "failed to get friends list from facebook");
			
			Toast.makeText(getApplicationContext(),
	                "Failed to retrieve friends list from Facebook - OFFLINE mode",
	                Toast.LENGTH_SHORT).show();

			saveState(StateMachine.OFFLINE_STATE);
			saveStatus(StateMachine.ERROR_STATUS);
			saveError(StateMachine.FB_GET_FRIENDS_FAILED_ERROR);
			//goToBucketListActivity();
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
		                "Failed to retrieve information from Facebook - OFFLINE mode",
		                Toast.LENGTH_SHORT).show();
				
				saveState(StateMachine.OFFLINE_STATE);
				saveStatus(StateMachine.ERROR_STATUS);
				saveError(StateMachine.FB_GET_ME_FAILED_ERROR);
				//goToBucketListActivity();
				return;
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
			
			Account[] accounts = am.getAccountsByType("com.kiddobloom");

			if (accounts.length <= 0) {
				Log.d("tag", "no account exists");
				// create a new account
				account = new Account(user.getId(), Constants.ACCOUNT_TYPE);
				am.addAccountExplicitly(account, null, null);
				ContentResolver.setSyncAutomatically(account, MyContentProvider.AUTHORITY, true);
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
					ContentResolver.setSyncAutomatically(account, MyContentProvider.AUTHORITY, true);
					
					// update the registered flag so facebook ID gets re-registered
					registered = false;
				}
			}
			
			// at this point, an account should already be created
			// store the userid and registered boolean in preferences db
			saveFbUserId(user.getId());
			saveUserIdRegistered(registered);
				
			// clear facebook friends first before conducting another get request
			myApp.friendsList.clear();
			
			saveState(StateMachine.FB_GET_FRIENDS_STATE);
			saveStatus(StateMachine.TRANSACTING_STATUS);
			saveError(StateMachine.NO_ERROR);
			
			// request facebook friends list
			Request.executeMyFriendsRequestAsync(response.getRequest().getSession(), this);

		} else {
			// throw an exception here - facebook does not indicate error but user is null 
			Log.d("tag", "failed to get user info from facebook - OFFLINE mode");
			
			Toast.makeText(getApplicationContext(),
	                "Failed to retrieve information from Facebook",
	                Toast.LENGTH_SHORT).show();
			
			saveState(StateMachine.OFFLINE_STATE);
			saveStatus(StateMachine.ERROR_STATUS);
			saveError(StateMachine.FB_GET_ME_FAILED_ERROR);
			//goToBucketListActivity();
		}	
	}	
	
	public void goToAuthenticatorActivity() {
		
		Intent launch = new Intent(this, AuthenticatorActivity.class);
		launch.putExtra("com.kiddobloom.bucketlist.current_tab", currentTab);
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
		Log.d("tag", "state change " + StateMachine.stateStr[state]);
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