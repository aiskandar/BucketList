package com.kiddobloom.bucketlist;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.util.Log;


public class MyApplication extends Application {

	public ArrayList<FriendData> friendsList;
	
    // next state table based on [EVENT][INITIAL_STATE] described in States class 
    // eg: this is how to get next state when current state is OPENED and LOGIN SUCCESS event happens
    // int i = nextState[States.LOGIN_SUCCESS_EVENT][States.OPENED_STATE];
    int [][] nextState = { 
    		{StateMachine.OPENED_STATE, StateMachine.OPENED_STATE, StateMachine.OPENED_STATE, StateMachine.OPENED_STATE},
    		{StateMachine.CLOSED_STATE, StateMachine.CLOSED_STATE, StateMachine.CLOSED_STATE, StateMachine.CLOSED_STATE},
    		{StateMachine.SKIPPED_STATE, StateMachine.SKIPPED_STATE, StateMachine.SKIPPED_STATE, StateMachine.SKIPPED_STATE},
    };
    
    // initial state 
    int currentState = StateMachine.INIT_STATE;
    int lastKnownError = StateMachine.NONE_ERROR;
	
	public MyApplication() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.d("tag", "MyApplication oncreate");
		
		friendsList = new ArrayList<FriendData>();
			
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");	
		ContentResolver.setMasterSyncAutomatically(true);
		
		for (int i=0 ; i < accounts.length ; i++) {
			Log.d("tag", "account: " + accounts[i].name);			
			//boolean var = ContentResolver.getSyncAutomatically(accounts[i], MyContentProvider.AUTHORITY);
		}
		
	}


}
