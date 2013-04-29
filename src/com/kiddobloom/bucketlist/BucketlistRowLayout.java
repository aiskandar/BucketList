package com.kiddobloom.bucketlist;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

//public class BucketlistRowLayout extends LinearLayout implements Checkable {
public class BucketlistRowLayout extends LinearLayout {

//		public int itemId;
//		private CheckBox cb;
//		private RatingBar rb;
		
		public BucketlistRowLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}

//	    @Override
//	    protected void onFinishInflate() {
//	    	super.onFinishInflate();
//	    	// find checked text view
//			//_checkbox = (CheckBox)findViewById(R.id.ctv1);
//	    	cb = (CheckBox)findViewById(R.id.ctv1);
//	    	rb = (RatingBar)findViewById(R.id.ratingBar1);
//	    	
//	    	this.setOnClickListener(this);
//	    	
//	    	cb.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					Log.d("tag", "checkbox clicked with id:" + itemId);
//
//					Uri base = MyContentProvider.CONTENT_URI;
//					base = Uri.withAppendedPath(base, "edit");
//					base = Uri.withAppendedPath(base, Integer.toString(itemId));
//					
//					ContentValues cv = new ContentValues();
//					boolean checked = cb.isChecked();
//					cv.put(MyContentProvider.COLUMN_DONE, checked);
//					getContext().getContentResolver().update(base, cv, null, null);
//				}
//			});
//			
//			rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
//				
//				@Override
//				public void onRatingChanged(RatingBar ratingBar, float rating,
//						boolean fromUser) {
//					// TODO Auto-generated method stub
//					Log.d("tag", "ratingbar: " + rating + " id:" + itemId);
//					
//					Uri base = MyContentProvider.CONTENT_URI;
//					base = Uri.withAppendedPath(base, "edit");
//					base = Uri.withAppendedPath(base, Integer.toString(itemId));
//					
//					ContentValues cv = new ContentValues();
//					int rt = (int)rating;
//					Log.d("tag", "ratingbar: " + rt);
//					cv.put(MyContentProvider.COLUMN_RATING, rt);
//					getContext().getContentResolver().update(base, cv, null, null);
//					
//				}
//			});
//			
//	    	//this.setOnTouchListener(this);
//	    }
//
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			Log.d("tag", "onclick :" + itemId);
//		}
	    
//	    @Override 
//	    public boolean isChecked() { 
//	        return _checkbox != null ? _checkbox.isChecked() : false; 
//	    }
//	    
//	    @Override 
//	    public void setChecked(boolean checked) {
//	    	Log.d("tag", "setChecked is called : " + checked);
//
//	    	if (_checkbox != null) {
//	    		_checkbox.setChecked(checked);
//	    	}
//	    }
//	    
//	    @Override 
//	    public void toggle() { 
//	    	Log.d("tag", "toggle is called");
//	    	if (_checkbox != null) {
//	    		_checkbox.toggle();
//	    	}
//	    } 

}
