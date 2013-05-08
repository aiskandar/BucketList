package com.kiddobloom.bucketlist;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract.Columns;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

//import com.kiddobloom.bucketlist.MyAdapter.OnItemClickListener;

public class MyListFragment extends SherlockFragment implements 
						OnItemClickListener, LoaderCallbacks<Cursor> {

	public MyAdapter la;
	public SelectListView lv;
	public EditText et;
	public Cursor myCursor;
	public ActionMode mMode;
	public LoaderManager lm;
	public SharedPreferences sp;
	static boolean updateInstead = false;
	static int rowToUpdate = 0;
	static int positionToUpdate = 0;
	
	public MyListFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		String from[] = { MyContentProvider.COLUMN_ENTRY, MyContentProvider.COLUMN_DATE };
		int to[] = { R.id.blogHeader, R.id.textView2 };

		la = new MyAdapter(getSherlockActivity(), from, to);

		sp = getSherlockActivity().getSharedPreferences(getString(R.string.pref_name), 0);
		
		boolean skip = sp.getBoolean(getString(R.string.pref_skip_key), false);

		// registerForContextMenu(findViewById(android.R.id.list));
		lm = getSherlockActivity().getSupportLoaderManager();
		LoaderCallbacks<Cursor> loaderCallback = this;
		lm.initLoader(0, null, loaderCallback);
		// lm.enableDebugLogging(true);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.mylist_fragment, container, false);	
		
		lv = (SelectListView) v.findViewById(android.R.id.list);
		et = (EditText) v.findViewById(R.id.editText1);
		
		getSherlockActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 

		if (lv==null || et==null) {
			return null;
		}
		
		
		// register on item click listener on listview 
		lv.setOnItemClickListener(this);
		lv.setAdapter(la);
		//lv.setOnClickListenerCallback(this);
			
		// register editor listener for keyboard presses
		et.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
				
				//Log.d("tag", "editText event: " + event + " actionID: "
					//	+ actionId + " view: " + tv.getText().toString());

				// when Enter key is pressed on soft keyboard - the code below
				// only catch the key up event
				if (event.getAction() == KeyEvent.ACTION_UP
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

					String text = tv.getText().toString();
					tv.setText("");

					ContentValues cv = new ContentValues();
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
					Date date = new Date();
					Uri base = MyContentProvider.CONTENT_URI;
					
					//Log.d("tag", "date : " + sdf.format(date));

					cv.put(MyContentProvider.COLUMN_ENTRY, text);

					if (updateInstead == false) {
						cv.put(MyContentProvider.COLUMN_DATE, sdf.format(date));
						cv.put(MyContentProvider.COLUMN_RATING, "false");
						cv.put(MyContentProvider.COLUMN_SHARE, "true");
						cv.put(MyContentProvider.COLUMN_DONE, "false");
						cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_INSERT);
						cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
							
						base = Uri.withAppendedPath(base, MyContentProvider.PATH_INSERT);
						
						getSherlockActivity().getContentResolver().insert(base , cv);
						
					} else {
											
						base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
						base = Uri.withAppendedPath(base, Integer.toString(rowToUpdate));
							
						getSherlockActivity().getContentResolver().update(base, cv, null, null);

						signalUpdate(false, 0, 0);						
						
					}
				}
				
				return true;
			}
		});

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		//Log.d("tag", "loader manager : oncreateloader");
		
		// filter the entries that are marked for deletion (using the selection field) from displaying in listview
		// sync adapter will kick in later to send the rest delete command to server
		// once we get confirmation from the server that deletion succeeds, the entry in the db will be deleted
		Loader<Cursor> loader = new MyLoader(getSherlockActivity(),
				MyContentProvider.CONTENT_URI, null, MyContentProvider.COLUMN_REST_STATE + "<>" + MyContentProvider.REST_STATE_DELETE, null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		//Log.d("tag", "loader manager : onloadfinished cursor= " + cursor);
		// save the cursor
		myCursor = cursor;
		la.swapCursor(cursor);
		
		// dump the db
//		cursor.moveToFirst();
//		for (int i=0; i < cursor.getCount(); i++) {
//			Log.d("tag", "row " + i + " :" + cursor.getInt(MyContentProvider.COLUMN_INDEX_ID) + " " + cursor.getString(MyContentProvider.COLUMN_INDEX_ENTRY) + " " 
//					+ cursor.getString(MyContentProvider.COLUMN_INDEX_DONE) + " " + MyContentProvider.restStateStr[cursor.getInt(7)]);
//			cursor.moveToNext();
//		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//Log.d("tag", "loader manager : onloaderreset");
		la.swapCursor(null);
	}

	public void signalUpdate(boolean b, int row, int position) {
		// TODO Auto-generated method stub
		updateInstead = b;
		rowToUpdate = row;
		positionToUpdate = position;
	}
	
	public void sync() {
		Log.d("tag", "sync");
		getSherlockActivity().getContentResolver().query(MyContentProvider.CONTENT_URI, null, MyContentProvider.COLUMN_REST_STATE + "<>" + MyContentProvider.REST_STATE_DELETE, null, null);
	}
	
	public class MyListActionMode implements ActionMode.Callback {

		public final static int MENU_GROUP_ID_MAIN = 0;
		public final static int MENU_ID_EDIT = 0;
		public final static int MENU_ID_DELETE = 1;
		public final static int MENU_ID_SHARE = 2;

		public MyListActionMode() {
			Log.d("tag", "constructor for actionmode");
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			//Log.d("tag", "actionmode oncreate: " + mode);

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
			//Log.d("tag", "menu item clicked: " + menuItemId);

			if (menuItemId == MENU_ID_DELETE) {

				base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE);

				//Log.d("tag", "number of checked item Ids: " + itemids.length);
				for (int i = 0; i < itemids.length; i++) {
					//Log.d("tag", "checked item id: " + itemids[i]);
					base = Uri.withAppendedPath(base,
							Integer.toString((int) itemids[i]));
				}

				//Log.d("tag", "uri: " + base);
				
				getSherlockActivity().getContentResolver().delete(base, null, null);

			} else if (menuItemId == MENU_ID_EDIT) {

				//Log.d("tag", "sba size: " + sba.size());
				for (int i = 0; i < sba.size(); i++) {
					int key = sba.keyAt(i);
					// Log.d("tag", "value at key:" + key + " is "+
					// sba.get(key));
					if (sba.get(key) == true) {
						//Log.d("tag", "checked item position: " + key);
						position = key;
					}
				}

				if (position == -1) {
					//Log.d("tag", "nothing to edit");
					mode.finish();
					return false;
				}

				Cursor c = (Cursor) la.getItem(position);
				String text = c.getString(MyContentProvider.COLUMN_INDEX_ENTRY);
				//Log.d("tag", "text to Edit: " + text);

				int itemId = (int) la.getItemId(position);

				base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
				base = Uri.withAppendedPath(base, Integer.toString(itemId));

				//Log.d("tag", "uri: " + base);

				if (text != null) {
					
					et.setText(text.toString());
					et.setSelection(et.getText().length());
				}

				signalUpdate(true, itemId, position);

			} else {
				//Log.d("tag", "menu item share clicked");

				StringBuilder textList = new StringBuilder();

				int count = 0;
				//Log.d("tag", "sba size: " + sba.size());
				for (int i = 0; i < sba.size(); i++) {

					int key = sba.keyAt(i);
					//Log.d("tag", "value at key:" + key + " is " + sba.get(key));
					if (sba.get(key) == true) {
						//Log.d("tag", "checked item position: " + key);
						Cursor c = (Cursor) la.getItem(key);
						String text = c
								.getString(MyContentProvider.COLUMN_INDEX_ENTRY);
						//Log.d("tag", " text to share: " + text);
						textList.append(++count);
						textList.append(". ");
						textList.append(text);
						textList.append('\n');

						//Log.d("tag", "string to append: " + textList);
					}
				}

				if (textList != null) {

					Intent share = new Intent(
							android.content.Intent.ACTION_SEND);
					share.setType("text/plain");
					share.putExtra(Intent.EXTRA_SUBJECT, "my bucket list");
					share.putExtra(Intent.EXTRA_TEXT, textList.toString());
					startActivity(Intent.createChooser(share,
							"Share Your dreams"));
				}
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			//Log.d("tag", "actionmode ondestroy: " + mode);
			mMode = null;

			SparseBooleanArray sba = lv.getCheckedItemPositions();
			// Log.d("tag", "sba size: " + sba.size());

			for (int i = 0; i < sba.size(); i++) {
				int key = sba.keyAt(i);
				// Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
				if (sba.get(key) == true) {
					// Log.d("tag", "checked item position: " + key);
					lv.setItemChecked(key, false);
				}
			}

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		int count = 0;
		Callback callback;

		SparseBooleanArray sba = lv.getCheckedItemPositions();
		// Log.d("tag", "sba size: " + sba.size());

		for (int i = 0; i < sba.size(); i++) {
			int key = sba.keyAt(i);
			// Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
			if (sba.get(key) == true) {
				// Log.d("tag", "checked item position: " + key);
				count++;
			}
		}

		Log.d("tag", "onItemClick position: " + position + " checked count = "
				+ count);

		if (count == 0) {
			/*
			 * no items are checked. let's disable CAB in case the CAB is
			 * already initialized
			 */
			if (mMode != null) {
				mMode.finish();
			}
			return;
		}

		if (mMode == null) {
			callback = new MyListActionMode();
			mMode = getSherlockActivity().startActionMode(callback);
		}

		if (count > 1) {
			// Log.d("tag", "count > 1");
			mMode.getMenu().removeItem(
					MyListActionMode.MENU_ID_EDIT);
		} else if (count == 1) {
			if (mMode.getMenu().findItem(
					MyListActionMode.MENU_ID_EDIT) == null) {
				// Log.d("tag", "adding edit menu item");
				mMode.getMenu()
						.add(MyListActionMode.MENU_GROUP_ID_MAIN,
								MyListActionMode.MENU_ID_EDIT, 0,
								"Edit")
						.setIcon(R.drawable.content_edit)
						.setShowAsAction(
								MenuItem.SHOW_AS_ACTION_IF_ROOM
										| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			}
		}

		/*
		 * need to tell the framework explicitly to do getView calls to the
		 * adapter - needed for Android 4.x
		 */
		la.notifyDataSetChanged();
		
	}
	
	public void refreshList() {
		lm.restartLoader(0, null, this);
	}

}
