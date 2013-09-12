package com.kiddobloom.bucketlist;

import java.io.File;
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

import com.facebook.Response;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.model.GraphUser;

//public class BucketListActivity extends Activity implements TabListener, Request.GraphUserCallback, Request.GraphUserListCallback {
public class BucketListActivity extends Activity implements TabListener {

	SharedPreferences sp;
	AccountManager am;
	MyApplication myApp;
	FragmentManager fm;
	Fragment[] fmList = new Fragment[4];
	Handler mHandler;
	Runnable mUpdate;
//	private MenuItem logout;
	
	public static int currentTab;
	
	private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    
    private static final int MYLIST_FRAGMENT_IDX = 0;
    private static final int COMMUNITY_FRAGMENT_IDX = 1;
    private static final int COMMUNITY_OFFLINE_FRAGMENT_IDX = 2;
    private static final int ABOUT_FRAGMENT_IDX = 3;
    
	private static final int MYLIST_NAV_TAB_IDX = 0;
	private static final int COMMUNITY_NAV_TAB_IDX = 1;
	private static final int ABOUT_NAV_TAB_IDX = 2;
	
	private static final int TIMER_INTERVAL = 40000; // in miliseconds
    
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	
    	//Log.d("tag", "bucketlist onseesionstatechange:" + session + " state:" + state);

    	if (session != null && session.isOpened()) {
 
        	if (getState() != StateMachine.ONLINE_STATE) {
				saveState(StateMachine.FB_OPENED_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);
				saveSkip(false);
				// disable timer
				mHandler.removeCallbacks(mUpdate);

				goToAuthenticatorActivity();
        	} else {
        		//Log.d("tag", "bucketlist onsessionstatechange re-open STATE MACHINE is ONLINE");
        	}
        
    	} else if (session.isClosed() == true){
				saveState(StateMachine.FB_CLOSED_STATE);
				saveStatus(StateMachine.OK_STATUS);
				saveError(StateMachine.NO_ERROR);

    			goToAuthenticatorActivity();
    	}
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent = getIntent();
	    currentTab = intent.getIntExtra("current_tab", 0);
	    
		if (savedInstanceState != null) {
			currentTab = savedInstanceState.getInt("currentTab");
		} 
		
		setContentView(R.layout.main_layout);
		
		fm = getFragmentManager();

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

		//Log.d("tag", "bucketlist activity created - currentTab: " + currentTab);

		sp = getSharedPreferences(getString(R.string.pref_name), 0);

		am = AccountManager.get(this);
		myApp = (MyApplication) getApplication();
		
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
        
        NavigationTabs.myListTab = getActionBar().newTab();
        NavigationTabs.myListTab.setText("My List");
        NavigationTabs.myListTab.setTabListener(this);
        getActionBar().addTab(NavigationTabs.myListTab, false);
 
        NavigationTabs.communityTab = getActionBar().newTab();
        NavigationTabs.communityTab.setText("My Friends' List");
        NavigationTabs.communityTab.setTabListener(this);   
        getActionBar().addTab(NavigationTabs.communityTab, false);
        
        NavigationTabs.aboutTab = getActionBar().newTab();
        NavigationTabs.aboutTab.setText("About");
        NavigationTabs.aboutTab.setTabListener(this);
        getActionBar().addTab(NavigationTabs.aboutTab, false);        
 
		getActionBar().setTitle("Bucket List");
	    getActionBar().setSubtitle("by kiddoBLOOM");
	    
		mHandler = new Handler();
		
		// timer callback handler - timer is set when state is OFFLINE
        mUpdate = new Runnable() {
        	
        	public void run() {
        		
        		// are we in online, offline, or skip state?
        		if (getState() == StateMachine.OFFLINE_STATE) {
        			//Log.d("tag", "timer event offline");
        			// most likely there is an error
        			if (getStatus() == StateMachine.ERROR_STATUS) {

        				if (!isNetworkAvailable()) {
        					//Log.d("tag", "network is not available");
        				} else {
	        				
	        				switch (getError()) {
	        					case StateMachine.NETWORK_DISCONNECT_ERROR:
	        						//Log.d("tag", "retry - network disconnect error");
	        						//getFacebookInfo();
	        						break;
								case StateMachine.FB_GET_ME_FAILED_ERROR:
									//Log.d("tag", "retry - fb get me failed error");
									//getFacebookInfo();
									break;
								case StateMachine.FB_GET_FRIENDS_FAILED_ERROR:
									//Log.d("tag", "retry - fb get friends failed error");
									//getFacebookFriends();
									break;
								case StateMachine.FBID_SERVER_REGISTER_ERROR:
									//Log.d("tag", "retry - server register error");
									//registerUserId();
									break;
								default:
		    				}
	        				
	        				goToAuthenticatorActivity();
        				}
        				
        				// restart timer
        				mHandler.removeCallbacks(this);
        		        mHandler.postDelayed(this, TIMER_INTERVAL); 
	            	}
        		} else if (getState() == StateMachine.ONLINE_STATE) {
        			// it seems our timer callback has successfully delivered us to ONLINE mode
        			// re-select the tab and DO not restart timer
        			//Log.d("tag", "timer event online");
        			
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
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		//Log.d("tag", "menu item selected :" + item.getItemId());
		
		int id = item.getItemId();
		
		if (id == R.id.menu_logout) {

			Session session = Session.getActiveSession();
			if (session != null && session.isOpened() == true) {
				session.closeAndClearTokenInformation();
				myApp.friendsList.clear();	
			} else {
				// toast
				//Log.d("tag", "toast: you are not logged in to facebook - ");
				Toast.makeText(this,
		                "You are not currently Logged in to Facebook",
		                Toast.LENGTH_SHORT).show();
			}

//		} else if (id == R.id.menu_update) {
//			MyListFragment mf = (MyListFragment) fmList[MYLIST_FRAGMENT_IDX];
//			mf.sync();
//		} else if (id == R.id.menu_preferences) {
//			Intent i = new Intent(this, PreferencesActivity.class);
//			startActivityForResult(i, 0);
		}
		
		return true;
	}	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        uiHelper.onResume();
        isResumed = true;
        //Log.d("tag", "bucketlist activity is resumed - currentTab: " + currentTab);
        //Log.d("tag", "bucketlist resuming from " + StateMachine.stateStr[getState()]);
        
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
		
		if (getState() == StateMachine.OFFLINE_STATE) {
			// most likely there is an error
			if (getStatus() == StateMachine.ERROR_STATUS) {
				//Log.d("tag", "trigger a timer to retry");
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
		//Log.d("tag", "bucketlist activity is paused");
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
  
       // clear facebook friends when activity is destroyed
	   myApp.friendsList.clear();
       saveInitialSynced(false);
       //Log.d("tag", "bucketlist activity ondestroy");
    }

	@Override
	protected void onStop() {
		super.onStop();
		//Log.d("tag", "bucketlist activity is stopped");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//Log.d("tag", "bucketlist activity is started");

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//Log.d("tag", "bucketlist activity onsaveinstancestate");
		

	 	// save the currently selected fragment
	 	outState.putInt("currentTab", currentTab);
	 	   
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		//Log.d("tag", "bucketlist activity onrestoreinstancestate");

		super.onRestoreInstanceState(state);
	}

	public void handleVisit(View v) {
		//Log.d("tag", "handle visit");
		
		Intent intent = new Intent(Intent.ACTION_VIEW); 
		intent.setData(Uri.parse("http://www.kiddobloom.com"));		
		
		startActivity(intent);
	}
	
	public void handleTwitter(View v) {
		//Log.d("tag", "handle Twitter follow");
		
		Intent intent = new Intent(Intent.ACTION_VIEW); 
		intent.setData(Uri.parse("http://twitter.com/kiddobloom"));		
		
		startActivity(intent);
	}
	
	public void handleFbLike(View v) {
		//Log.d("tag", "handle FB like");
		
		Intent intent;
		String kiddobloom_fb_id = "208086305947271";
		String uri = "fb://page/" + kiddobloom_fb_id;
		String url = "https://www.facebook.com/kiddobloom";
			
		try {
			//Log.d("tag", "uri: " + uri);
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();//Log.d("tag", "url: " + url);
		    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		    startActivity(intent);
		}
		
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

	    Session session = Session.getActiveSession();
	       
		if (tab.getText().equals("My List")) {
			//Log.d("tag", "My List selected");
			
			ft.show(fmList[MYLIST_FRAGMENT_IDX]);	
			MyListFragment mf = (MyListFragment) fmList[MYLIST_FRAGMENT_IDX];
			//mf.refreshList();
			
			ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
			ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			ft.hide(fmList[ABOUT_FRAGMENT_IDX]);
			
			currentTab = MYLIST_NAV_TAB_IDX;
			
		} else if (tab.getText().equals("My Friends' List")) {
			//Log.d("tag", "community selected");

			ft.hide(fmList[MYLIST_FRAGMENT_IDX]);	
			
			if(getState() == StateMachine.ONLINE_STATE) {
				ft.show(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
				CommunityFragment cf = (CommunityFragment) fmList[COMMUNITY_FRAGMENT_IDX];
				//cf.dataChange();
			} else {
				ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.show(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			}
			
			ft.hide(fmList[ABOUT_FRAGMENT_IDX]);
			
			currentTab = COMMUNITY_NAV_TAB_IDX;

		} else {
			//Log.d("tag", "about selected");
			
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
		
		if (tab.getText().equals("My Friends' List")) {
			//Log.d("tag", "community re-selected");
			
			Session session = Session.getActiveSession();
			if(getState() == StateMachine.ONLINE_STATE) {
				ft.show(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
				CommunityFragment cf = (CommunityFragment) fmList[COMMUNITY_FRAGMENT_IDX];
				//cf.dataChange();
			} else {
				ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.show(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			}
		} else if (tab.getText().equals("My List")){
			//Log.d("tag", "My List re-selected");
			
		}
	
	}
	
	public void goToAuthenticatorActivity() {
		
		Intent launch = new Intent(this, AuthenticatorActivity.class);
		launch.putExtra("com.kiddobloom.bucketlist.current_tab", currentTab);
		startActivity(launch);
		finish();		
	}
	
	public void goToDetailedEntryActivity() {
		
		Intent launch = new Intent(this, DetailedEntryActivity.class);
		launch.putExtra("com.kiddobloom.bucketlist.current_tab", currentTab);
//		launch.putExtra("com.kiddobloom.bucketlist.facebook_id", )
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
		//Log.d("tag", "bucketlist state change " + StateMachine.stateStr[state]);
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
	
	// facebook pending publish
	public void saveFbPendingPublish(boolean value) {
		//Log.d("tag", "set FbPendingPublish: " + value);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_facebook_pending_publish), value);
		editor.commit();
	}

	public boolean getFbPendingPublish() {
		return sp.getBoolean(getString(R.string.pref_facebook_pending_publish), false);
	}

}