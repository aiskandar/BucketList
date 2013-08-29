package com.kiddobloom.bucketlist;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.SyncStateContract.Columns;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView.OnEditorActionListener;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

//import com.kiddobloom.bucketlist.MyAdapter.OnItemClickListener;

public class MyListFragment extends Fragment implements 
						OnItemClickListener, LoaderCallbacks<Cursor> {

	public MyAdapter la;
	public SelectListView lv;
	public EditText et;
	public WebView wv;
	public Cursor myCursor;
	public ActionMode mMode;
	public LoaderManager lm;
	public SharedPreferences sp;
	static boolean updateInstead = false;
	static int rowToUpdate = 0;
	static int positionToUpdate = 0;
	static int RESULT_LOAD_IMAGE = 1;
	static int rowItemId = -1;
	
	public MyListFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.d("tag", "MyListFragment: onCreate");
		
		String from[] = { MyContentProvider.COLUMN_ENTRY, MyContentProvider.COLUMN_DATE };
		int to[] = { R.id.fblistitems, R.id.listItemDate };

		la = new MyAdapter(getActivity(), from, to);

		sp = getActivity().getSharedPreferences(getString(R.string.pref_name), 0);
		
		boolean skip = sp.getBoolean(getString(R.string.pref_skip_key), false);

		// registerForContextMenu(findViewById(android.R.id.list));
		lm = getActivity().getLoaderManager();
		LoaderCallbacks<Cursor> loaderCallback = this;
		lm.initLoader(0, null, loaderCallback);
		// lm.enableDebugLogging(true);

	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		
		Log.d("tag", "MyListFragment: onCreateView");
		
		View v = inflater.inflate(R.layout.mylist_fragment, container, false);	
		
		lv = (SelectListView) v.findViewById(android.R.id.list);
		et = (EditText) v.findViewById(R.id.editText1);
		wv = (WebView) v.findViewById(R.id.webView1);

		String id = getFbUserId();
		//wv.loadUrl("http://andyiskandar.me/adget.php?id=" + "100002870863505");
		//wv.loadUrl("http://andyiskandar.me/adget.php?id=" + id);
		
		if (lv==null || et==null) {
			return null;
		}
		
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
		
		// register on item click listener on listview 
		lv.setOnItemClickListener(this);
		lv.setAdapter(la);			
		
		// webview
		wv.setVisibility(View.GONE);
		
		// register editor listener for keyboard presses
		et.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
				
				Log.d("tag", "editText event: " + event + " actionID: "
						+ actionId + " view: " + tv.getText().toString());

					String text = tv.getText().toString();				
					tv.setText("");					
					
					if(text.isEmpty()) {
						return true;
					}

					ContentValues cv = new ContentValues();
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
					Date date = new Date();
					Uri base = MyContentProvider.CONTENT_URI;
					
					//Log.d("tag", "date : " + sdf.format(date));

					cv.put(MyContentProvider.COLUMN_ENTRY, text);

					//Random r = new Random();
					//int rdIdx = r.nextInt(Constants.resId.length);
					//int resId = Constants.resId[rdIdx];
					//int rdIdx = 0;
					int resId = R.drawable.placeholder;
					Log.d("tag", "resid selected: " + resId);
					//byte[] value = {'d','u','m','m','y'}; 
					
					if (updateInstead == false) {
						
						String fbid = getFbUserId();
						Log.d("tag", "inserting fbid: " + fbid);
						
						cv.put(MyContentProvider.COLUMN_DATE, sdf.format(date));
						cv.put(MyContentProvider.COLUMN_RATING, "false");
						cv.put(MyContentProvider.COLUMN_SHARE, "true");
						cv.put(MyContentProvider.COLUMN_DONE, "false");
						
						// if the user skips facebook login, mark the rest_state column of the entry to skipped state
						// if the user decides to login to facebook later, these entries will be local and not synced to server
						if (false == sp.getBoolean(getString(R.string.pref_skip_key), false)) {
							cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_INSERT);
						} else {
							cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_SKIPPED);
						}
						cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
						cv.put(MyContentProvider.COLUMN_IMG_PATH, resId);
						cv.put(MyContentProvider.COLUMN_DATE_COMPLETED, sdf.format(date));
						cv.put(MyContentProvider.COLUMN_IMG_CACHE, "false");
						//cv.put(MyContentProvider.COLUMN_IMG, value);
						cv.put(MyContentProvider.COLUMN_FACEBOOK_ID, fbid);
							
						base = Uri.withAppendedPath(base, MyContentProvider.PATH_INSERT);
						getActivity().getContentResolver().insert(base , cv);
						
					} else {
						
						//cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_UPDATE);
						//cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
						base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
						base = Uri.withAppendedPath(base, Integer.toString(rowToUpdate));
							
						getActivity().getContentResolver().update(base, cv, null, null);
						
						signalUpdate(false, 0, 0);						
						
					}
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);	
				
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
		Log.d("tag", "LoaderManager : oncreateloader");
		
		// filter the entries that are marked for deletion (using the selection field) from displaying in listview
		// sync adapter will kick in later to send the rest delete command to server
		// once we get confirmation from the server that deletion succeeds, the entry in the db will be deleted
		// Loader<Cursor> loader = new MyLoader(getActivity(), MyContentProvider.CONTENT_URI, null, MyContentProvider.COLUMN_REST_STATE + "<>" + MyContentProvider.REST_STATE_DELETE, null, null);
		Loader<Cursor> loader = new MyLoader(getActivity(), MyContentProvider.CONTENT_URI, null, MyContentProvider.COLUMN_FACEBOOK_ID + "=" + getFbUserId() + " AND " + MyContentProvider.COLUMN_REST_STATE + "<>" + MyContentProvider.REST_STATE_DELETE , null, null);
		//Loader<Cursor> loader = new MyLoader(getActivity(), MyContentProvider.CONTENT_URI, null, MyContentProvider.COLUMN_REST_STATE + "<>" + MyContentProvider.REST_STATE_DELETE , null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("tag", "LoaderManager : onloadfinished cursor= " + cursor);
		// save the cursor
		myCursor = cursor;
		la.swapCursor(cursor);
		
		// dump the db
		cursor.moveToFirst();
		for (int i=0; i < cursor.getCount(); i++) {
			Log.d("tag", "row " + i + " :" + cursor.getInt(MyContentProvider.COLUMN_INDEX_ID) + " " + cursor.getString(MyContentProvider.COLUMN_INDEX_ENTRY) + " " 
					+ cursor.getString(MyContentProvider.COLUMN_INDEX_DONE) + " " + MyContentProvider.restStateStr[cursor.getInt(7)] + " " + cursor.getInt(MyContentProvider.COLUMN_INDEX_FACEBOOK_ID));
			cursor.moveToNext();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("tag", "LoaderManager : onloaderreset");
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
		getActivity().getContentResolver().query(MyContentProvider.CONTENT_URI, null, MyContentProvider.COLUMN_REST_STATE + "<>" + MyContentProvider.REST_STATE_DELETE, null, null);
	}
	
	public class MyListActionMode implements ActionMode.Callback {

		public final static int MENU_GROUP_ID_MAIN = 0;
		public final static int MENU_ID_EDIT = 0;
		public final static int MENU_ID_DELETE = 1;
		public final static int MENU_ID_SHARE = 2;
		public final static int MENU_ID_PHOTO = 3;


		public MyListActionMode() {
			Log.d("tag", "constructor for actionmode");
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			//Log.d("tag", "actionmode oncreate: " + mode);

			menu.add(MENU_GROUP_ID_MAIN, MENU_ID_PHOTO, 0, "Change Picture")
			.setIcon(R.drawable.content_picture)
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
			
			menu.add(MENU_GROUP_ID_MAIN, MENU_ID_SHARE, 0, "Share")
			.setIcon(R.drawable.content_social)
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
			int itemId;

			int menuItemId = item.getItemId();
			//Log.d("tag", "menu item clicked: " + menuItemId);

			if (menuItemId == MENU_ID_DELETE) {

				for (int i = 0; i < sba.size(); i++) {
					int key = sba.keyAt(i);
					//Log.d("tag", "value at key:" + key + " is " + sba.get(key));
					if (sba.get(key) == true) {
						Log.d("tag", "MENU_DELETE: checked item position = " + key);
						position = key;
						
						Cursor c = (Cursor) la.getItem(position);
						String state = c.getString(MyContentProvider.COLUMN_INDEX_REST_STATE);

						if (state.equals(MyContentProvider.REST_STATE_SKIPPED)) {
							base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE_DB);
						} else {
							base = Uri.withAppendedPath(base, MyContentProvider.PATH_DELETE);
						}
						
						itemId = (int) la.getItemId(position);
						base = Uri.withAppendedPath(base, Integer.toString(itemId));
						
						getActivity().getContentResolver().delete(base, null, null);
					}
					
				}

			} else if (menuItemId == MENU_ID_EDIT) {

				//Log.d("tag", "sba size: " + sba.size());
				for (int i = 0; i < sba.size(); i++) {
					int key = sba.keyAt(i);
					if (sba.get(key) == true) {
						Log.d("tag", "MENU_EDIT: checked item position = " + key);
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

				itemId = (int) la.getItemId(position);

				if (text != null) {
					
					et.setText(text.toString());
					et.setSelection(et.getText().length());
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
				}	

				signalUpdate(true, itemId, position);

			} else if (menuItemId == MENU_ID_SHARE){
				Log.d("tag", "menu item share clicked");

				StringBuilder textList = new StringBuilder();

				textList.append("I would like to share my bucket list:\n");
				int count = 0;
				//Log.d("tag", "sba size: " + sba.size());
				for (int i = 0; i < sba.size(); i++) {

					int key = sba.keyAt(i);
					//Log.d("tag", "value at key:" + key + " is " + sba.get(key));
					if (sba.get(key) == true) {
						Log.d("tag", "MENU_SHARE: checked item position = " + key);
						Cursor c = (Cursor) la.getItem(key);
						String text = c
								.getString(MyContentProvider.COLUMN_INDEX_ENTRY);
						Log.d("tag", "MENU_SHARE: text to share = " + text);
						textList.append(++count);
						textList.append(". ");
						textList.append(text);
						textList.append('\n');

						Log.d("tag", "MENU_SHARE: string to append = " + textList);
					}
				}

				if (textList != null) {

					Intent share = new Intent(
							android.content.Intent.ACTION_SEND);
					share.setType("text/plain");
					share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					share.putExtra(Intent.EXTRA_SUBJECT, "bucket list");
					share.putExtra(Intent.EXTRA_TEXT, textList.toString());
					startActivity(Intent.createChooser(share,
							"Share Your dreams"));
				}
			} else {
				
				//Log.d("tag", "menu item picture clicked");
				
				for (int i = 0; i < sba.size(); i++) {
					int key = sba.keyAt(i);
					Log.d("tag", "value at key:" + key + " is "+ sba.get(key));
					if (sba.get(key) == true) {
						Log.d("tag", "MENU_PICTURE: checked item position = " + key);
						position = key;
					}
				}

				if (position == -1) {
					Log.d("tag", "nothing to select");
					mode.finish();
					return false;
				}
				
				itemId = (int) la.getItemId(position);				
				//Log.d("tag","item id: " + itemId);
				
				int icounter = getImageCounter();
				
				Intent i = new Intent(Intent.ACTION_PICK ,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
				path += "/picture" + icounter++ + ".jpg";
	            
				saveImageCounter(icounter);
				
				File file = new File(path);
	            Uri outputFileUri = Uri.fromFile( file );
				
	            i.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				i.setType("image/*");
				i.putExtra("crop", "true");
				i.putExtra("aspectX", 16);
	            i.putExtra("aspectY", 9);
	            i.putExtra("outputX", 533);
	            i.putExtra("outputY", 300);
	            i.putExtra("scale", true);
	            i.putExtra("scaleUpIfNeeded", true);
				i.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				
				// ugly - pass in data when callback for activityresult is called
				rowItemId = itemId;
						
				startActivityForResult(i, RESULT_LOAD_IMAGE);
				
			}

			mode.finish();
			return true;
		}

//		private void setRowToSelect(int itemId) {
//			// TODO Auto-generated method stub
//			rowToSelect = itemId;
//		}

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.d("tagab", "onactivity result");

	    if (requestCode == MyListFragment.RESULT_LOAD_IMAGE) {

            if (data != null && resultCode == Activity.RESULT_OK) {  
            	  
            	int icounter = getImageCounter();
            	icounter--;
            	
	            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            	filePath += "/picture" + icounter + ".jpg";

				Log.d("tagab", "image path: " + filePath + " rowid: " + rowItemId);
				Bitmap image = BitmapFactory.decodeFile(filePath);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				image.compress(Bitmap.CompressFormat.JPEG, 75, out);

				Uri base = MyContentProvider.CONTENT_URI;
				ContentValues cv = new ContentValues();
				cv.put(MyContentProvider.COLUMN_IMG_PATH, filePath);
				cv.put(MyContentProvider.COLUMN_IMG, out.toByteArray());
				cv.put(MyContentProvider.COLUMN_IMG_CACHE, "true");
				//cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_UPDATE);
				//cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
				
				base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
				base = Uri.withAppendedPath(base, Integer.toString(rowItemId));

				getActivity().getContentResolver().update(base, cv, null, null);
				
			}
		}
        		
		
//        if (requestCode == MyListFragment.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
// 
//            Uri selectedImage = data.getData();
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//            Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//            Log.d("tag", "image path: " + picturePath + " rowid: " + rowToSelect);
//            
//            Uri base = MyContentProvider.CONTENT_URI;
//            ContentValues cv = new ContentValues();
//			cv.put(MyContentProvider.COLUMN_IMG_PATH, picturePath);
//			
//            base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
//			base = Uri.withAppendedPath(base, Integer.toString(rowToSelect));
//            
//			getActivity().getContentResolver().update(base, cv, null, null);
//			
//            //ImageView imageView = (ImageView) findViewById(R.id.imgView);
//            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
//     }
     
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
			mMode = getActivity().startActionMode(callback);
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
		// only restart loader if the account has been synced
		if (getInitialSynced() == true) {
			Log.d("tag", "LoaderManager: restarting loader");
			lm.restartLoader(0, null, this);
			saveInitialSynced(false);
		}
	}

	public boolean getInitialSynced() {
		return sp.getBoolean(getString(R.string.pref_initial_synced_key), false);
	}
	
	public void saveInitialSynced(boolean value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_initial_synced_key), value);
		editor.commit();
	}

	public void saveImageCounter(int count) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(getString(R.string.pref_image_counter), count);
		editor.commit();
	}
	
	public int getImageCounter() {
		return sp.getInt(getString(R.string.pref_image_counter), 100);	
	}
	
	// fb_userid
	public void saveFbUserId(String fbUserid) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(getString(R.string.pref_fb_userid_key), fbUserid);
		editor.commit();
	}

	public String getFbUserId() {
		String id = sp.getString(getString(R.string.pref_fb_userid_key), "0");
		//Log.d("tag", "fb userid: " + id);
		
		// If fb_userid is not yet saved to preferences db, this will return the string "none".
		// the mysql WHERE clause does not like this.  
		if (id.equals("none")) {
			return "0";
		} 
		return id;
	}
	
	// skip
	public void saveSkip(boolean skip) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(getString(R.string.pref_skip_key), skip);
		editor.commit();
	}
	
	public Boolean getSkip() {
		return sp.getBoolean(getString(R.string.pref_skip_key), false);
	}
}
