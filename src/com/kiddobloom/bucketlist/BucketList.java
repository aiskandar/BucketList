package com.kiddobloom.bucketlist;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BucketList extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final String TEXT_ENTRY_KEY = "TEXT_ENTRY_KEY";
	private static final String ADDING_ITEM_KEY = "ADDING_ITEM_KEY";
	private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";
	
	MyAdapter la;
	ListView lv;
	Cursor myCursor;
	ActionMode mMode;
	LoaderManager lm;
	static boolean updateInstead = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_todolist);
		Log.d("tag", "main is created");
		
		String from[] = {"task", "date"};
		int to[] = {R.id.textView1, R.id.textView2};
		
		//la = new ArrayAdapter<TodolistActivity.TodoItem>(this, R.layout.list_image_text, R.id.textView1, todoList);
		la = new MyAdapter(this, from, to);
		lv = (ListView) findViewById(android.R.id.list);
		
		lv.setOnItemClickListener(this);
				
		SharedPreferences x = getSharedPreferences("todolist", 0);
		String y = x.getString(TEXT_ENTRY_KEY, "none");
		
		//registerForContextMenu(findViewById(android.R.id.list));
		lm = getSupportLoaderManager();
		LoaderCallbacks<Cursor> loaderCallback = this;
		lm.initLoader(0, null, loaderCallback);
		//lm.enableDebugLogging(true);
			
		lv.setAdapter(la);
		
		EditText et = (EditText) findViewById(R.id.editText1);
		
		et.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				Log.d("tag", "editText event id: " + actionId + " view: " + v.getText().toString());

				String x = v.getText().toString();
				v.setText("");		
				
				ContentValues cv = new ContentValues();
				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
				Date date = new Date();
				
				Log.d("tag", "date : " + sdf.format(date));
						
				cv.put(MyContentProvider.COLUMN_TASK, x);
				cv.put(MyContentProvider.COLUMN_DATE, sdf.format(date));
						
				getContentResolver().insert(MyContentProvider.CONTENT_URI, cv);
				
				return true;
			}
		});
		
	}



//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		// TODO Auto-generated method stub
//		View x = findViewById(android.R.id.list);
//		
//		Log.d("tag", "crete context menu");
//		
//		if (x == v) {
//			Log.d("tag", "create context menu for listview :" + x);
//			getMenuInflater().inflate(R.menu.listview_item_menu, menu);
//		}
//		
//	}
	


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("tag", "main is paused");
		
		SharedPreferences x = getSharedPreferences("todolist", Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = x.edit();
		
		EditText t = (EditText) findViewById(R.id.editText1);
		ed.putString(TEXT_ENTRY_KEY, t.getText().toString());
		ed.commit();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("tag", "main is stopped");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("tag", "main is started");

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		Log.d("tag", "onsaveinstancestate");
		super.onSaveInstanceState(outState);
		
		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		Log.d("tag", "onrestoreinstancestate");
		
		super.onRestoreInstanceState(state);
	}
	
//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		// TODO Auto-generated method stub
//		//super.onListItemClick(l, v, position, id);
//
//		Log.d("tag", "list item clicked with position:" + position + " view: " + v);
//		
//		Callback callback = new AnActionModeofEpicProportions(position);
//		mMode = startActionMode(callback);
//		
//		//l.setItemChecked(position, true);
//
//	}
	

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		
//		Log.d("tag", "oncreate options menu");
//		
//		// Inflate the menu; this adds items to the action bar if it is present.
//		//getSupportMenuInflater().inflate(R.menu.activity_todolist, menu);
//		
//        menu.add("Save")
//        .setIcon(R.drawable.ic_compose_inverse)
//        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//        //menu.add("Search")
//       // .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//
//        menu.add("Refresh")
//        .setIcon(R.drawable.ic_refresh_inverse)
//        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);		
//		return true;
//	}

	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//
//		int id = item.getItemId();
//		

//	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		Log.d("tag", "list item clicked with position:" + position + " and id: " + id);	
		//Callback callback = new AnActionModeofEpicProportions(id, this);
		Callback callback = new AnActionModeofEpicProportions(id);
		mMode = startActionMode(callback);	
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		Log.d("tag", "loader manager : oncreateloader");
		Loader<Cursor> loader = new MyLoader(this, MyContentProvider.CONTENT_URI, null, null, null, null);
		return loader;
	}



	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("tag", "loader manager : onloadfinished cursor= " + cursor);
		//save the cursor
		myCursor = cursor;
		la.swapCursor(cursor);
		
		int position = lv.getCheckedItemPosition();
		lv.setItemChecked(position, false);

	}



	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("tag", "loader manager : onloaderreset");
		la.swapCursor(null);
	}
	
	public class AnActionModeofEpicProportions implements ActionMode.Callback {

		long _id = -1; 
		
		public AnActionModeofEpicProportions(long id) {		
			Log.d("tag", "new actionmode bar requested for id: " + id);		
			_id = id;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			
			Log.d("tag", "actionmode oncreate: " + mode);
			
				mode.setTitle("Bucket List");
				
		       menu.add("Edit")
		        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		       menu.add("Delete")
		        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

				return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			Uri base = MyContentProvider.CONTENT_URI;
			int position = lv.getCheckedItemPosition();
	    	long id[] = lv.getCheckedItemIds();
	    	
			Uri url = Uri.withAppendedPath(base, Integer.toString((int)id[0]));
			        
	        if (item.toString().equals("Delete")) {
	        	Log.d("tag", "actionmode delete");		
	        	position = getContentResolver().delete(url, null, null);
	        	
	        } else if (item.toString().equals("Edit")) {
	        	EditText et = (EditText)findViewById(R.id.editText1);
	        	LinearLayout tv = (LinearLayout) lv.getChildAt(position);
	        	
	        	if (lv != null) {
	        		TextView view = (TextView) tv.findViewById(R.id.textView1);
	        		
		        	if (view != null) {
		        		et.setText(view.getText().toString());
		        	}

//		    		ContentValues cv = new ContentValues();
//		    		
//		    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		    		Date date = new Date();
//		    		
//		    		Log.d("tag", "date : " + sdf.format(date));
//		    				
//		    		cv.put(MyContentProvider.COLUMN_TASK, et.getText().toString());
//		    		cv.put(MyContentProvider.COLUMN_DATE, sdf.format(date));		        	
//		        	
//		        	getContentResolver().update(url, cv, null, null);
	        	}
        }
	        
	        //mode.finish();
	        return true;

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			Log.d("tag", "actionmode ondestroy: " + mode);
			
		}
		

	}

	
}
