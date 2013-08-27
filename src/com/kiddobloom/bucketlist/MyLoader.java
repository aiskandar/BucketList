package com.kiddobloom.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.content.CursorLoader;
import android.util.Log;

public class MyLoader extends CursorLoader {

	Cursor cursor;	

	public MyLoader(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Cursor loadInBackground() {
		// TODO Auto-generated method stub
		Cursor c = super.loadInBackground();
		Log.d("tag", "MyLoader: loadInBackground");
		return c;
	}
	
	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		Log.d("tag", "MyLoader: onStartLoading " + this + " cursor = " + cursor);
		
	}
	
	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		Log.d("tag", "MyLoader: onStopLoading ");
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		Log.d("tag", "MyLoader: onReset " + this + " cursor = " + cursor);
		
		if (isStarted()) {
			Log.d("tag", "MyLoader started state " + this);
		}
		
		if (isReset()) {
			Log.d("tag", "MyLoader reset state " + this);
		}
	}
	
	@Override
	protected void onAbandon() {
		super.onAbandon();
		Log.d("tag", "MyLoader: onAbandon " + this  + " cursor = " + cursor);
		
		if (isStarted()) {
			Log.d("tag", "loader started state " + this);
		}
		
		if (isReset()) {
			Log.d("tag", "loader reset state " + this);
		}
	}
	
	@Override
	public void deliverResult(Cursor data) {
		super.deliverResult(data);
		Log.d("tag", "myLoader: deliverResult cursor = " + data);
		
		if (isStarted()) {
			//Log.d("tag", "loader started state " + this);
		}
		
		if (isReset()) {
			//Log.d("tag", "loader reset state " + this);
		}
		
	}
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		Log.d("tag", "MyLoader: onContentChanged");
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
		
		Log.d("tag", "MyLoader: reset");
		
		if (isStarted()) {
			Log.d("tag", "loader started state " + this);
		}
		
		if (isReset()) {
			Log.d("tag", "loader reset state " + this);
		}		
		
	}
}
