package com.kiddobloom.bucketlist;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class SelectListView extends ListView {
	
	public SelectListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChoiceMode(CHOICE_MODE_MULTIPLE);
	}


	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		Log.d("tag","perform click");
		return super.performClick();
	}
	
	@Override
	public boolean performItemClick(View view, int position, long id) {
		
		Log.d("tag", "perform item click position: " + position);

		if (BucketList.updateInstead == true) {
			Log.d("tag", "in middle of update");
			Toast.makeText(getContext(),
	                "Press X button to cancel edit mode or press Enter to save change",
	                Toast.LENGTH_SHORT).show();
			return false;
		}
			
		boolean checked = false;
		
		if (isItemChecked(position)) {
			checked = true; 
		}
		
		// this is the function in AbsListView that maintains the state of checked items in listView
		super.performItemClick(view, position, id);
		
		// Allow the user to toggle the selection
		if (checked == true) {
			setItemChecked(position, false);
		}

		return false;
	}
	
	@Override
	public void setItemChecked(int position, boolean value) {
		// TODO Auto-generated method stub
		//Log.d("tag2", "set item checked : " + position);
		super.setItemChecked(position, value);
	}

	@Override
	public int getCheckedItemPosition() {
		// TODO Auto-generated method stub
		Log.d("tag", "get checked item position");
		return super.getCheckedItemPosition();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		
		Uri base = MyContentProvider.CONTENT_URI;
		
		//Log.d("tag", "on touch event action=" + ev.getAction() + " x=" + ev.getX() + " y=" + ev.getY());
		
		float x = ev.getX();
		float y= ev.getY();
		int action = ev.getAction();
		
		if (x < getWidth() / 7 && action == MotionEvent.ACTION_DOWN) {
			
			int position = pointToPosition((int)x, (int)y);
			Log.d("tag", "checkbox area is clicked for pos: " + position);
			
			int id = (int) getAdapter().getItemId(position);
			Log.d("tag", "checkbox area is clicked for item id: " + id);
			
			Cursor c = (Cursor) getAdapter().getItem(position);
			String checked = c.getString(MyContentProvider.COLUMN_INDEX_DONE);
						
			Log.d("tag", "checkbox is checked = " + checked);
			
			ContentValues cv = new ContentValues();
			
			if (checked.equals("true")) {
				cv.put(MyContentProvider.COLUMN_DONE, "false");
			} else {
				cv.put(MyContentProvider.COLUMN_DONE, "true");
			}
			
			base = Uri.withAppendedPath(base, "edit");
			base = Uri.withAppendedPath(base, Integer.toString(id));
			
			Log.d("tag", "uri: " + base);
			
			getContext().getContentResolver().update(base, cv, null, null);
			
			return false;
			
		} else {
			return super.onTouchEvent(ev);
		}
	}
}
