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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;


public class BucketListActivity extends SherlockFragmentActivity implements TabListener {

	SharedPreferences sp;
	AccountManager accountManager;
	MyApplication myApp;
	FragmentManager fm;
	Fragment[] fmList = new Fragment[4];
	boolean skip;
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
	    		myApp.currentState = StateMachine.OPENED_STATE;
	    		goToAuthenticatorActivity();
    		} else if (session.isClosed() == true){
    			myApp.currentState = StateMachine.CLOSED_STATE;
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
		fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX] = fm
				.findFragmentById(R.id.commOfflineFragment);
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

		accountManager = AccountManager.get(this);
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

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	       uiHelper.onPause();
	        isResumed = false;
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
		
		FriendData fd = myApp.friendsList.get(0); 
		
		String kiddobloom_fb_id = "208086305947271";
		String uri = "fb://page/" + kiddobloom_fb_id;
		Log.d("tag", "uri: " +uri);
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));		
		startActivity(intent);
		
	}

	private void saveSkip(boolean skip) {

		Log.d("tag", "bucketlist saveskip: " + skip);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_skip_key), skip);
		editor.commit();
	}

	private void saveInitialSynced(boolean synced) {

		Log.d("tag", "bucketlist saveInitialSynced: " + synced);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_initial_sync_key), synced);
		editor.commit();
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

	    Session session = Session.getActiveSession();
	       
		if (tab.getText().equals("My List")) {
			Log.d("tag", "my list selected");
			
			ft.show(fmList[MYLIST_FRAGMENT_IDX]);			
			ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
			ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			ft.hide(fmList[ABOUT_FRAGMENT_IDX]);
			
			currentTab = MYLIST_NAV_TAB_IDX;
			
		} else if (tab.getText().equals("Community")) {
			Log.d("tag", "community selected");

			ft.hide(fmList[MYLIST_FRAGMENT_IDX]);	
			
			if(skip == false && session != null && session.isOpened() == true) {
				ft.show(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
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
			if(skip == false && session != null && session.isOpened() == true) {
				ft.show(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.hide(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			} else {
				ft.hide(fmList[COMMUNITY_FRAGMENT_IDX]);
				ft.show(fmList[COMMUNITY_OFFLINE_FRAGMENT_IDX]);
			}
		}
	
	}

	public void handleJoinEvent(View v) {
		Log.d("tag", "handle join event");	
		saveSkip(false);	
		goToAuthenticatorActivity();
	}
	
	public void goToAuthenticatorActivity() {
		
		Intent launch = new Intent(this, AuthenticatorActivity.class);
		launch.putExtra("com.kiddobloom.bucketlist.current_tab", currentTab);
		//launch.putExtra("com.kiddobloom.bucketlist.fb_state", fbState);
		startActivity(launch);
		finish();		
	}
}