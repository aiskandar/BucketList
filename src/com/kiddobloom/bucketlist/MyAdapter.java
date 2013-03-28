package com.kiddobloom.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class MyAdapter extends SimpleCursorAdapter {

	LayoutInflater mInflater;		
	Context context;

	public class ViewHolder {
		public TextView tv;
		public CheckBox cb;
	}
	
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
	
//	@Override
//	public View getView(int pos, View v, ViewGroup parent) {
//		//Log.d("tag", "getView pos: " + pos + " view: " + v);
//		ViewHolder vh = new ViewHolder();
//		
//		if (v == null) {
//			v = mInflater.inflate(R.layout.list_image_text, parent, false);			
//			
//			vh.tv = (TextView) v.findViewById(R.id.textView1);  	
//        	vh.cb = (CheckBox) v.findViewById(R.id.ctv1);
//        	
//        	v.setTag(vh);
//        	
//		} else {
//			
//			vh = (ViewHolder) v.getTag();
//		}
//		
//		cursor.moveToPosition(pos);
//		
//		vh.tv.setText(cursor.getString(cursor.getColumnIndex("task")));
//		
//		return v;
//	}

}
