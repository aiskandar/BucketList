package com.kiddobloom.bucketlist;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BucketList extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final String SHARED_PREF_NAME = "BUCKET_LIST_PREF";
	private static final String TEXT_ENTRY_KEY = "TEXT_ENTRY_KEY";
	private static final String ADDING_ITEM_KEY = "ADDING_ITEM_KEY";
	private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";
	
	MyAdapter la;
	ListView lv;
	Cursor myCursor;
	ActionMode mMode;
	LoaderManager lm;
	static boolean updateInstead = false;
	static Uri lastUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_todolist);
		Log.d("tag", "main is created");
		
		String from[] = {MyContentProvider.COLUMN_TASK, MyContentProvider.COLUMN_DATE};
		int to[] = {R.id.textView1, R.id.textView2};
		
		getSupportActionBar().setTitle("The Bucket List");
		getSupportActionBar().setSubtitle("by kiddoBLOOM");
		
		la = new MyAdapter(this, from, to);
		lv = (ListView) findViewById(android.R.id.list);
		
		lv.setOnItemClickListener(this);
				
		SharedPreferences x = getSharedPreferences(SHARED_PREF_NAME, 0);
		
		//registerForContextMenu(findViewById(android.R.id.list));
		lm = getSupportLoaderManager();
		LoaderCallbacks<Cursor> loaderCallback = this;
		lm.initLoader(0, null, loaderCallback);
		//lm.enableDebugLogging(true);
			
		lv.setAdapter(la);
		
		ImageButton cab = (ImageButton) findViewById(R.id.imageButton2);
		
		cab.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("tag", "cancel button is clicked");
				
				EditText et = (EditText) findViewById(R.id.editText1);
				et.setText("");
				
				updateInstead = false;
				lastUri = null;
				
			}
		});
		
		EditText et = (EditText) findViewById(R.id.editText1);
		
		et.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
				Log.d("tag", "editText event: " + event + " actionID: " + actionId + " view: " + tv.getText().toString());

				// when Enter key is pressed on soft keyboard - the code below only catch the key up event
				if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					
					String text = tv.getText().toString();
					tv.setText("");		
					
					ContentValues cv = new ContentValues();
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
					Date date = new Date();
					
					Log.d("tag", "date : " + sdf.format(date));
							
					cv.put(MyContentProvider.COLUMN_TASK, text);

							
					if (updateInstead == false) {
						cv.put(MyContentProvider.COLUMN_DATE, sdf.format(date));
						cv.put(MyContentProvider.COLUMN_DONE, "false");
						getContentResolver().insert(MyContentProvider.CONTENT_URI, cv);
						
					} else {
						getContentResolver().update(lastUri, cv, null, null);
						signalUpdate(false, null);
					}
				}
				
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
		
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		int count = 0;
		Callback callback;

   		SparseBooleanArray sba = lv.getCheckedItemPositions();		
		//Log.d("tag", "sba size: " + sba.size());
		
    	for (int i = 0; i < sba.size() ; i++) {
    		int key = sba.keyAt(i);
    		//Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
    		if (sba.get(key) == true) {
				//Log.d("tag", "checked item position: " + key);
				count++;
			}
		}		
    	
    	Log.d("tag", "onItemClick position: " + position + " checked count = " + count);
		
		if (count == 0) {
			/* no items are checked.  let's disable CAB in case the CAB is already initialized */
			if (mMode != null) {
				mMode.finish();
			}
			return;
		}

		if (mMode == null) {
			callback = new AnActionModeofEpicProportions();		
			mMode = startActionMode(callback);			
		}
		
		if (count > 1) {
			//Log.d("tag", "count > 1");
			mMode.getMenu().removeItem(AnActionModeofEpicProportions.MENU_ID_EDIT);
		} else if (count == 1) {
			if (mMode.getMenu().findItem(AnActionModeofEpicProportions.MENU_ID_EDIT) == null) {
				//Log.d("tag", "adding edit menu item");
				mMode.getMenu()
						.add(AnActionModeofEpicProportions.MENU_GROUP_ID_MAIN,
								AnActionModeofEpicProportions.MENU_ID_EDIT, 0,
								"Edit")
						.setIcon(R.drawable.content_edit)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT); 

			} 
		}
		
		/* need to tell the framework explicitly to do getView calls to the adapter -
		 * needed for Android 4.x
		 */
		la.notifyDataSetChanged();

	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		//Log.d("tag", "loader manager : oncreateloader");
		Loader<Cursor> loader = new MyLoader(this, MyContentProvider.CONTENT_URI, null, null, null, null);
		return loader;
	}



	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		//Log.d("tag", "loader manager : onloadfinished cursor= " + cursor);
		//save the cursor
		myCursor = cursor;
		la.swapCursor(cursor);
				
//		for (int i=0; i < cursor.getColumnCount(); i++) {
//			Log.d("tag", "column " + i + " = " + cursor.getColumnName(i));
//		}
	}



	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("tag", "loader manager : onloaderreset");
		la.swapCursor(null);
	}

	public void signalUpdate(boolean b, Uri url) {
		// TODO Auto-generated method stub
		updateInstead = b;
		lastUri = url;
	}

	public class AnActionModeofEpicProportions implements ActionMode.Callback {
		
		public final static int MENU_GROUP_ID_MAIN = 0;
		public final static int MENU_ID_EDIT = 0;
		public final static int MENU_ID_DELETE = 1;
		public final static int MENU_ID_SHARE = 2;
		
		public AnActionModeofEpicProportions() {		
			Log.d("tag", "constructor for actionmode");
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			
			Log.d("tag", "actionmode oncreate: " + mode);

			menu.add(MENU_GROUP_ID_MAIN, MENU_ID_SHARE, 0, "Share")
					.setIcon(R.drawable.content_social)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			menu.add(MENU_GROUP_ID_MAIN, MENU_ID_DELETE, 0, "Delete")
					.setIcon(R.drawable.content_discard)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			menu.add(MENU_GROUP_ID_MAIN, MENU_ID_EDIT, 0, "Edit")
					.setIcon(R.drawable.content_edit)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
				
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			Uri base = MyContentProvider.CONTENT_URI;
			 
			SparseBooleanArray sba = lv.getCheckedItemPositions();
			long[] itemids = lv.getCheckedItemIds(); 
			int position = -1;

			int menuItemId = item.getItemId();
			Log.d("tag", "menu item clicked: " + menuItemId);
			
	        if (menuItemId == MENU_ID_DELETE) {
	        	
				base = Uri.withAppendedPath(base, "delete");

				Log.d("tag", "number of checked item Ids: " + itemids.length);
	        	for (int i = 0 ; i < itemids.length ; i++) {
					Log.d("tag", "checked item id: " + itemids[i]);
					base = Uri.withAppendedPath(base, Integer.toString((int)itemids[i]));
				}
				
				Log.d("tag", "uri: " + base);
	        	getContentResolver().delete(base, null, null);
	        	
	        } else if (menuItemId == MENU_ID_EDIT) {

	        	Log.d("tag", "sba size: " + sba.size());
	        	for (int i = 0; i < sba.size() ; i++) {
	        		int key = sba.keyAt(i);
	        		//Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
	        		if (sba.get(key) == true) {
	    				Log.d("tag", "checked item position: " + key);
	    				position = key;
	    			}
	    		}
	        	
				if (position == -1) {
					Log.d("tag", "nothing to edit");
					mode.finish();
					return false;
				}

				Cursor c = (Cursor) la.getItem(position);
				String text = c.getString(MyContentProvider.COLUMN_INDEX_TASK);
				Log.d("tag", "text to Edit: " + text);
				
				int itemId = (int) la.getItemId(position);
									
				base = Uri.withAppendedPath(base, "edit");
	        	base = Uri.withAppendedPath(base, Integer.toString(itemId));
	        	
				Log.d("tag", "uri: " + base);

		        if (text != null) {
		        	EditText et = (EditText) findViewById(R.id.editText1);
		        	et.setText(text.toString());
		        	et.setSelection(et.getText().length());
		        }
		   
		        signalUpdate(true, base);
		        	        	
	        } else {
	        	Log.d("tag", "menu item share clicked");
	        	
	        	StringBuilder textList = new StringBuilder();
	  	        	
	        	int count = 0;
	        	Log.d("tag", "sba size: " + sba.size());
	        	for (int i = 0; i < sba.size() ; i++) {
	        		
	        		int key = sba.keyAt(i);
	        		Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
	        		if (sba.get(key) == true) {
	    				Log.d("tag", "checked item position: " + key);
	    				Cursor c = (Cursor) la.getItem(key);
	    				String text = c.getString(MyContentProvider.COLUMN_INDEX_TASK);
	    				Log.d("tag", " text to share: " + text);
	    				textList.append(++count);
	    				textList.append(". ");
	    				textList.append(text);
	    				textList.append('\n');

	    				Log.d("tag", "string to append: " + textList);
	    			}
	    		}
	        	
	        	if (textList != null) { 
	        	

	        	Intent share = new Intent(android.content.Intent.ACTION_SEND);
	        	share.setType("text/plain");
	        	share.putExtra(Intent.EXTRA_SUBJECT, "my bucket list");
	        	share.putExtra(Intent.EXTRA_TEXT, textList.toString());
	        	startActivity(Intent.createChooser(share, "Share Your dreams"));
	        	}
	        }
      
	        mode.finish();
	        return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			Log.d("tag", "actionmode ondestroy: " + mode);
			mMode = null;

    		SparseBooleanArray sba = lv.getCheckedItemPositions();		
    		//Log.d("tag", "sba size: " + sba.size());
    		
        	for (int i = 0; i < sba.size() ; i++) {
        		int key = sba.keyAt(i);
        		//Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
        		if (sba.get(key) == true) {
    				//Log.d("tag", "checked item position: " + key);
    				lv.setItemChecked(key, false);
    			}
    		}
					
		}
	}
}