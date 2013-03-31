package com.kiddobloom.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class MyAdapter extends SimpleCursorAdapter {

	LayoutInflater mInflater;		
	ListView lv;
	static int selectedPos = -1;
	static int prevSelectedPos = -1;
	static boolean selectedPosChecked = false;
	
	public MyAdapter(Context c, String[] from, int[] to) {
		
		super(c, R.layout.list_image_text, null, from, to, 0);
		
		// TODO Auto-generated constructor stub
		mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}
		
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		// return super.newView(context, cursor, parent);
		
		//Log.d("tag", "cursor: " + cursor.getPosition());
		
		View v = mInflater.inflate(R.layout.list_image_text, parent, false);
	
		lv = (ListView) parent;
		
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		// TODO Auto-generated method stub
		// super.bindView(arg0, arg1, arg2);
		//Log.d("tag", "bindview: " + v + " cursor: " + c.getPosition());
		Log.d("tag", "bindview - cursor: " + c.getPosition());
		
		TextView tv = (TextView) v.findViewById(R.id.textView1);
		tv.setText(c.getString(1));

		//Log.d("tag", "checked item position: " + lv.getCheckedItemPosition());
		
		CheckBox cr = (CheckBox) v.findViewById(R.id.ctv1);

		cr.setChecked(lv.isItemChecked(c.getPosition()));

	}
	
}
