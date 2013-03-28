package com.kiddobloom.bucketlist;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

public class BucketlistRowLayout extends LinearLayout implements Checkable {

	private CheckBox _checkbox;
	
	public BucketlistRowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	   @Override
	    protected void onFinishInflate() {
	    	super.onFinishInflate();
	    	// find checked text view
			_checkbox = (CheckBox)findViewById(R.id.ctv1);
	    }
	    
	    @Override 
	    public boolean isChecked() { 
	        return _checkbox != null ? _checkbox.isChecked() : false; 
	    }
	    
	    @Override 
	    public void setChecked(boolean checked) {
	    	//Log.d("tag", "setChecked is called : " + checked);

	    	if (_checkbox != null) {
	    		_checkbox.setChecked(checked);
	    	}
	    }
	    
	    @Override 
	    public void toggle() { 
	    	Log.d("tag", "toggle is called");
	    	if (_checkbox != null) {
	    		_checkbox.toggle();
	    	}
	    } 

}
