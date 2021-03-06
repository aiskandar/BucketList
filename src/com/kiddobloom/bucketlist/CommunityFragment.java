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
import android.app.Fragment;

public class CommunityFragment extends Fragment {

	SharedPreferences sp;
	private MyApplication myApp;
	ListView lv;
	ArrayAdapter ad;
	
//	FriendPickerFragment friendPickerFragment;

	public CommunityFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		sp = getActivity().getSharedPreferences(getString(R.string.pref_name), 0);
		myApp = (MyApplication) getActivity().getApplication();
		
		boolean skip = sp.getBoolean(getString(R.string.pref_skip_key), false);

		//NavigationTabs.communityTab.setTabListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate (R.layout.community_fragment, container, false);
		
		ad = new FacebookFriendsAdapter (getActivity(), R.layout.facebook_friends_item_layout, myApp.friendsList);		
		lv = (ListView) v.findViewById(R.id.fbfriendslist);
	
		if (lv != null) {
			lv.setAdapter(ad);
			//ad.notifyDataSetChanged();
		}
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

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		

	}
	
	public void dataChange() {
		if (ad != null) {
			ad.notifyDataSetChanged();
			//Log.d("tag", "dataChange");
		}
		
		
	}

}
