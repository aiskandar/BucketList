package com.kiddobloom.bucketlist;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.facebook.widget.FriendPickerFragment;

public class CommunityOfflineFragment extends SherlockFragment {

	SharedPreferences sp;
	private MyApplication myApp;
	
//	FriendPickerFragment friendPickerFragment;

	public CommunityOfflineFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		sp = getSherlockActivity().getSharedPreferences(getString(R.string.pref_name), 0);
		myApp = (MyApplication) getSherlockActivity().getApplication();
		
		boolean skip = sp.getBoolean(getString(R.string.pref_skip_key), false);

		//NavigationTabs.communityTab.setTabListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate (R.layout.facebook_login_2, container, false);
		
		return v;
	}
	    
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
    }


}
