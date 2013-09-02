package com.kiddobloom.bucketlist;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class MyApplication extends Application {

	public ArrayList<FriendData> friendsList;
	private static MyApplication mApp = null;
	
	public MyApplication() {
		// TODO Auto-generated constructor stub
	}
	
    public static Context context()
    {
        return mApp.getApplicationContext();
    }
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.d("tagaa", "MyApplication oncreate");
		
		friendsList = new ArrayList<FriendData>();
			
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");	
		ContentResolver.setMasterSyncAutomatically(true);
		
		SharedPreferences sp;
		sp = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
		
		// check to see if this is the first time the app is ran
		int state = sp.getInt(getString(R.string.pref_state_key), 100);

		SharedPreferences.Editor editor = sp.edit();

		if (state == 100) {
			// first time the app is launched
			// initialize all the preferences string (key = value)
			// state = "init"
			// status = "ok"
			// error = "none"
			// fb_userid = "none"
			// userid_registered = false
			// skip = false
			// initial_sync = false
			// fb_me_retrieved = false
			// fb_friends_retrieved = false
			
			editor.putInt(getString(R.string.pref_state_key), StateMachine.INIT_STATE);
			editor.putInt(getString(R.string.pref_status_key), StateMachine.OK_STATUS);
			editor.putInt(getString(R.string.pref_error_key), StateMachine.NO_ERROR);
			editor.putString(getString(R.string.pref_fb_userid_key), "none");
			editor.putBoolean(getString(R.string.pref_facebook_publish_permission), false);
			
			editor.putBoolean(getString(R.string.pref_userid_registered_key), false);
			editor.putBoolean(getString(R.string.pref_skip_key), false);
			editor.putBoolean(getString(R.string.pref_initial_synced_key), false);
			editor.putBoolean(getString(R.string.pref_first_time_install), true);
		} else {
			editor.putBoolean(getString(R.string.pref_first_time_install), false);
		}

		editor.commit();	
		

		mApp = this;
			
	}

}
