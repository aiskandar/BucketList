package com.kiddobloom.bucketlist;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;


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
		// TODO Auto-generated method stub
		Log.d("tag", "perform item click position: " + position);
		
		boolean checked = false;
		
		if (isItemChecked(position)) {
			checked = true; 
		}
		
		super.performItemClick(view, position, id);
		
		// allow the user to toggle the checkbox
		if (checked == true) {
			setItemChecked(position, false);
		}
		
		
		return false;
	}
	
	@Override
	public void setItemChecked(int position, boolean value) {
		// TODO Auto-generated method stub
		//Log.d("tag", "set item checked : " + position);
		super.setItemChecked(position, value);
	}

	@Override
	public int getCheckedItemPosition() {
		// TODO Auto-generated method stub
		Log.d("tag", "getcheckeditemposition");
		return super.getCheckedItemPosition();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		
		//Log.d("tag", "on touch event: " + ev);
		return super.onTouchEvent(ev);
	}
}
