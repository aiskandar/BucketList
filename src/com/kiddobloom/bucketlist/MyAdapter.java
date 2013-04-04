package com.kiddobloom.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
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
		
		super(c, R.layout.item_layout, null, from, to, 0);
		
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
		
		View v = mInflater.inflate(R.layout.item_layout, parent, false);
	
		lv = (ListView) parent;
		
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		// TODO Auto-generated method stub
		// super.bindView(arg0, arg1, arg2);
		
		//Log.d("tag2", "bindview - position: " + c.getPosition());
		
		TextView tv = (TextView) v.findViewById(R.id.textView1);
		tv.setText(c.getString(MyContentProvider.COLUMN_INDEX_ENTRY));

		TextView tv2 = (TextView) v.findViewById(R.id.textView2);
		tv2.setText(c.getString(MyContentProvider.COLUMN_INDEX_DATE));
		
//		SparseBooleanArray sba = lv.getCheckedItemPositions();
//		
//		Log.d("tag3", "sba size: " + sba.size());
//		
//    	for (int i = 0; i < sba.size() ; i++) {
//    		int key = sba.keyAt(i);
//    		Log.d("tag3", "value at key:" + key + " is "+ sba.get(key));
//    		if (sba.get(key) == true) {
//				Log.d("tag3", "checked item position: " + key);
//			}
//		}
		
		if (lv.isItemChecked(c.getPosition())) {
			//Log.d("tag2", "yellow");
			v.setBackgroundResource(R.color.paper);
		} else {
			//Log.d("tag2", "white");
			v.setBackgroundResource(android.R.color.white);
		}
		
		String checked_str = c.getString(MyContentProvider.COLUMN_INDEX_DONE);
		boolean checked = Boolean.parseBoolean(checked_str);
		
		//Log.d("tag", "pos = " + c.getPosition() + " checked = " + checked);

		CheckBox cb = (CheckBox) v.findViewById(R.id.ctv1);
		
		if (cb != null) {
			cb.setChecked(checked);

			if (cb.isChecked()) {
				tv.setPaintFlags(tv.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				tv.setPaintFlags(tv.getPaintFlags()
						& ~Paint.STRIKE_THRU_TEXT_FLAG);
			}
		}

		
	}
	
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		//Log.d("tag", "getview :" + arg0);
		return super.getView(arg0, arg1, arg2);
	}
	
}
