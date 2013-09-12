package com.kiddobloom.bucketlist;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DetailedFriendAdapter extends ArrayAdapter<BucketListTable> {

	LayoutInflater mInflater;

	public DetailedFriendAdapter(Context context, int resource, int textViewResourceId) {
		super (context, resource, textViewResourceId);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View baseview, ViewGroup vg) {
		
		//Log.d("tag", "getview for pos: " + position);
		
		ListView lv = (ListView) vg;
		int itemId = (int) getItemId(position);
		BucketListTable blt = getItem(position);
		
		if (baseview != null) {

		} else {
			baseview = mInflater.inflate(R.layout.friend_detailed_item_layout, vg, false);
		}
		
		TextView check = (TextView) baseview.findViewById(R.id.friend_checkmark);
		TextView text = (TextView) baseview.findViewById(R.id.simple_textview);
		
		if (check != null) {
			
			if (blt.done.equals("false")) {
				check.setVisibility(View.INVISIBLE);
			} else {
				check.setVisibility(View.VISIBLE);
			}
		}
		
		if (text != null) {

			text.setText(blt.entry);
		}
		
		return baseview;
	}
}