package com.kiddobloom.bucketlist;

import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyContentProvider extends ContentProvider {
    
	// URI format
	// base uri - content://com.kiddobloom.bucketlist/bucket
	// query - content://com.kiddobloom.bucketlist/bucket/query
	// insert - content://com.kiddobloom.bucketlist/bucket/insert
	// insert_no_notify - content://com.kiddobloom.bucketlist/bucket/insert_no_notify
	// update - content://com.kiddobloom.bucketlist/bucket/update/5
	// update_no_notify - content://com.kiddobloom.bucketlist/bucket/update_no_notify/5
	// delete (rest) - content://com.kiddobloom.bucketlist/bucket/delete/5/6/9/13
	// delete_db - content://com.kiddobloom.bucketlist/bucket/delete_db/5/6/9/13
	// delete_no_notify - content://com.kiddobloom.bucketlist/bucket/delete_no_notify/5/6/9/13
	
    public static final String AUTHORITY = "com.kiddobloom.bucketlist";
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY); 
	public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "bucket");
	
	public static final String PATH_QUERY = "query";
	public static final String PATH_INSERT = "insert";
	public static final String PATH_INSERT_NO_NOTIFY = "insert_no_notify";
	public static final String PATH_UPDATE = "update";
	public static final String PATH_UPDATE_DB = "update_db";
	public static final String PATH_UPDATE_NO_NOTIFY = "update_no_notify";
	public static final String PATH_DELETE = "delete";
	public static final String PATH_DELETE_DB = "delete_db";
	public static final String PATH_DELETE_NO_NOTIFY = "delete_no_notify";
	
	private static final String DATABASE_NAME = "bucketList.db";
	private static final int DATABASE_VERSION = 40;
	
	// Database table name
	public static final String DATABASE_TABLE = "bucket";
	public static final int DB_MAX_ENTRIES = 50;
	
	// Database column name
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SERVER_ID = "server_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_ENTRY = "entry";
	public static final String COLUMN_DONE = "done";
	public static final String COLUMN_RATING = "rating";
	public static final String COLUMN_SHARE = "share";
	public static final String COLUMN_REST_STATE = "rest_state";
	public static final String COLUMN_REST_STATUS = "rest_result";
	public static final String COLUMN_IMG_PATH = "img_path";
	public static final String COLUMN_DATE_COMPLETED = "date_completed";
	public static final String COLUMN_IMG_CACHE = "image_cache";
	public static final String COLUMN_FACEBOOK_ID = "facebook_id";
	public static final String COLUMN_IMG = "image";

	// Database column index
	public static final int COLUMN_INDEX_ID = 0;
	public static final int COLUMN_INDEX_SERVER_ID = 1;
	public static final int COLUMN_INDEX_DATE = 2;
	public static final int COLUMN_INDEX_ENTRY = 3;
	public static final int COLUMN_INDEX_DONE = 4;
	public static final int COLUMN_INDEX_RATING = 5;
	public static final int COLUMN_INDEX_SHARE = 6;
	public static final int COLUMN_INDEX_REST_STATE = 7;
	public static final int COLUMN_INDEX_REST_STATUS = 8;
	public static final int COLUMN_INDEX_IMG_PATH = 9;
	public static final int COLUMN_INDEX_DATE_COMPLETED = 10;
	public static final int COLUMN_INDEX_IMG_CACHE = 11;
	public static final int COLUMN_INDEX_FACEBOOK_ID = 12;
	public static final int COLUMN_INDEX_IMG = 13;

	public static final String dbColumnStr[] = {COLUMN_ID, COLUMN_SERVER_ID, COLUMN_DATE, COLUMN_ENTRY, 
										COLUMN_DONE, COLUMN_RATING, COLUMN_SHARE, COLUMN_REST_STATE, COLUMN_REST_STATUS, 
										COLUMN_IMG_PATH, COLUMN_DATE_COMPLETED, COLUMN_IMG_CACHE, COLUMN_FACEBOOK_ID, COLUMN_IMG };
	
	// REST STATUS constants
	public static final int REST_STATE_INSERT = 0;
	public static final int REST_STATE_DELETE = 1;
	public static final int REST_STATE_UPDATE = 2;
	public static final int REST_STATE_QUERY = 3;
	public static final int REST_STATE_NONE = 4;

	public static final String REST_STATE_INSERT_STR = "insert";
	public static final String REST_STATE_DELETE_STR = "delete";
	public static final String REST_STATE_UPDATE_STR = "update";
	public static final String REST_STATE_QUERY_STR = "query";
	public static final String REST_STATE_NONE_STR = "none";
	
    public static final String restStateStr[] = {REST_STATE_INSERT_STR, REST_STATE_DELETE_STR, REST_STATE_UPDATE_STR, REST_STATE_QUERY_STR, REST_STATE_NONE_STR };
	
	// REST RESULTS constants
	public static final int REST_STATUS_TRANSACTING = 0;
	public static final int REST_STATUS_RETRY = 1;
	public static final int REST_STATUS_SYNCED = 2;
	
	public static final String REST_STATUS_TRANSACTING_STR = "transacting";
	public static final String REST_STATUS_RETRY_STR = "retry";
	public static final String REST_STATUS_SYNCED_STR = "synced";
	
    public static final String restStatusStr[] = {REST_STATUS_TRANSACTING_STR, REST_STATUS_RETRY_STR, REST_STATUS_SYNCED_STR};
	
	private SQLiteDatabase bucketDB;
	private BucketDBOpenHelper bucketDBHelper;
	
	public MyContentProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onCreate() {
		
		Log.d("tag", "provider oncreate");
		bucketDBHelper = new BucketDBOpenHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		bucketDB = bucketDBHelper.getWritableDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		Log.d("tag", "MyContentProvider: query uri = " + uri.toString());
		
		List<String> ls = null;
		ls = uri.getPathSegments();
		//Log.d("tag", "query path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			//Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
		}
		
		Cursor cur = bucketDB.query(DATABASE_TABLE, projection, selection, selectionArgs, null, null, COLUMN_DONE + " ASC," + COLUMN_RATING + " DESC");
		cur.setNotificationUri(getContext().getContentResolver(), uri);
		
		SharedPreferences sp = getContext().getSharedPreferences(getContext().getString(R.string.pref_name), 0);
		AccountManager accountManager = AccountManager.get(getContext());

		boolean synced = sp.getBoolean(getContext().getString(R.string.pref_initial_synced_key), false);
		int state = sp.getInt(getContext().getString(R.string.pref_state_key), 100);
		
		if (synced == false && state == StateMachine.ONLINE_STATE) {
			Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");		
			for (int i=0 ; i < accounts.length ; i++) {
				Log.d("tag", "MyContentProvider: requesting query sync for account = " + accounts[i].name);
			
				Bundle extras = new Bundle();
				extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				ContentResolver.requestSync(accounts[i], MyContentProvider.AUTHORITY, extras);
			}
			
			SharedPreferences.Editor editor = sp.edit();		
			editor.putBoolean(getContext().getString(R.string.pref_initial_synced_key), true);
			editor.commit();
			
		} else {
			Log.d("tag", "MyContentProvider: no request sync - synced = " + sp.getBoolean(getContext().getString(R.string.pref_initial_synced_key), false));
		}
		return cur;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		Log.d("tag", "MyContentProvider: bulk insert uri = " + uri);
		
		// set the maximum entries for bucketlist
		long count = DatabaseUtils.queryNumEntries(bucketDB, DATABASE_TABLE);
		
		//Log.d("tag", "size of db = " + count);
		if (count >= DB_MAX_ENTRIES) {
            Toast.makeText(getContext(), 
                    "Failed to add entry - Maximum allowable entries are " + DB_MAX_ENTRIES, 
                    Toast.LENGTH_LONG).show();
			return 0;	
		}
		
		List<String> ls = null;
		ls = uri.getPathSegments();
		//Log.d("tag", "bulk insert path segments: " + ls);
		
		for (int i = 0 ; i < ls.size() ; i++) {
			//Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
		}
		
		int size = values.length;
		for (int i=0; i<size; i++) {
			bucketDB.insert(DATABASE_TABLE, null, values[i]);
		}
		
		// bulk insert is only called after performing initial sync
		// we get here from onPerformSync after the device has received
		// JSON response from the server.  we want to update the listview
		// but we do not want to trigger another network update
		// set the syncToNetwork flag to false
		getContext().getContentResolver().notifyChange(uri, null, false);
		
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		Log.d("tag", "getType " + uri);
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		Log.d("tag", "MyContentProvider: insert uri = " + uri);
		
		List<String> ls = null;
		ls = uri.getPathSegments();
		String command = null;
		// set the maximum entries for bucketlist
		long count = DatabaseUtils.queryNumEntries(bucketDB, DATABASE_TABLE);
		
		//Log.d("tag", "size of db = " + count);
		if (count >= DB_MAX_ENTRIES) {
            Toast.makeText(getContext(), 
                    "Failed to add entry - Maximum allowable entries are " + DB_MAX_ENTRIES, 
                    Toast.LENGTH_LONG).show();
			return null;	
		}
		
		//Log.d("tag", "insert path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			//Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
			if (i == 1) {
				command = ls.get(i).toString();
			} 
		}
		
		// insert into our local DB first and sync later
		// at this stage, the entry's REST_STATE should be inserting and REST_STATUS is transacting
		// sync adapter will check the local database and process this entry because the REST_STATUS is transacting
		bucketDB.insert(DATABASE_TABLE, null, values);
		
		if (command.equals(PATH_INSERT)) {
			getContext().getContentResolver().notifyChange(uri, null, true);
		} else if (command.equals(PATH_INSERT_NO_NOTIFY)) {
			// do nothing - local DB already updated above
		} else {
			// throw exception here
		}

		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		Log.d("tag", "MyContentProvider delete uri = " + uri);

		List<String> ls = null;
		ls = uri.getPathSegments();
		String command = null;
		
		//Log.d("tag", "path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			//Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
			if (i == 1) {
				command = ls.get(i).toString();
			} 
		}
		
		if (command.equals(PATH_DELETE)) {
			
			for (int i = 2 ; i < ls.size() ; i++) {
				Log.d("tag", "Delete rest - mark for deletion rowID: " + ls.get(i).toString());
				
				ContentValues cv = new ContentValues();	
				cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_DELETE);
				cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
				bucketDB.update(DATABASE_TABLE, cv, COLUMN_ID + "=" + ls.get(i).toString(), null);
			}

			getContext().getContentResolver().notifyChange(uri, null, true);
			
		} else if (command.equals(PATH_DELETE_DB)) {
			
			for (int i = 2 ; i < ls.size() ; i++) {
				Log.d("tag", "Delete DB - rowID to REALLY delete: " + ls.get(i).toString());
				bucketDB.delete(DATABASE_TABLE, COLUMN_ID + "=" + ls.get(i).toString(), null);
				
				// PATH_DELETE_DB is only called from onPerformSync after the device
				// has completed delete transaction with the server.  We want to update 
				// the listview but we do not want to trigger another network update
				// set the syncToNetwork flag to false				
				getContext().getContentResolver().notifyChange(uri, null, false);
			} 
		} else if (command.equals(PATH_DELETE_NO_NOTIFY)) {
			
			for (int i = 2 ; i < ls.size() ; i++) {
				Log.d("tag", "Delete no notify - rowID to REALLY delete: " + ls.get(i).toString());
				bucketDB.delete(DATABASE_TABLE, COLUMN_ID + "=" + ls.get(i).toString(), null);
			}
		} else {
			// throw exception here
		}
		
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		Log.d("tag", "MyContentProvider: update uri = " + uri);

		List<String> ls = null;
		ls = uri.getPathSegments();
		String rowId = null;
		String command = null;
		
		//Log.d("tag", "update path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			//Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
			
			if (i == 1) {
				command = ls.get(i).toString();
			} else if (i == 2) {
				rowId = ls.get(i).toString();
			}
		}
		
		if (command.equals(PATH_UPDATE)) {
			
			Cursor myCursor = bucketDB.query(DATABASE_TABLE, null, COLUMN_ID + "=" + rowId, null, null, null, COLUMN_RATING + " DESC");
			
			//Log.d("tag", "cursor count: " + myCursor.getCount());	
			if (myCursor.getCount() == 0) {
				return 0;
			}
			
			myCursor.moveToFirst();
			
			Log.d("tag", "db update: rest state " + restStateStr[myCursor.getInt(COLUMN_INDEX_REST_STATE)] );
				
			// do not store the updated values if the user is in process of deleting the row
			if (myCursor.getInt(COLUMN_INDEX_REST_STATE) == REST_STATE_DELETE) {
				// do nothing to the row - this is the case where we do async task to retrieve image
				// when the image came back the row is already deleted by the user.
				Log.d("tag", "db update - row is in process of being deleted");
			} else if (myCursor.getInt(COLUMN_INDEX_REST_STATE) == REST_STATE_NONE) {
				values.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_UPDATE);
				values.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
				bucketDB.update(DATABASE_TABLE, values, COLUMN_ID + "=" + rowId, null);
				getContext().getContentResolver().notifyChange(uri, null, true);
			} else if (myCursor.getInt(COLUMN_INDEX_REST_STATE) == REST_STATE_INSERT) {
				// the row is in process of being inserted and the user has already changed the entries
				// we insert a new row that includes the latest changes and keep the state as REST_STATE_INSERT
				bucketDB.update(DATABASE_TABLE, values, COLUMN_ID + "=" + rowId, null);
				getContext().getContentResolver().notifyChange(uri, null, false);
			} else if (myCursor.getInt(COLUMN_INDEX_REST_STATE) == REST_STATE_UPDATE){
				bucketDB.update(DATABASE_TABLE, values, COLUMN_ID + "=" + rowId, null);
				getContext().getContentResolver().notifyChange(uri, null, true);
			} else {
				Log.d("tag", "error - db update - but row is in state " + restStateStr[myCursor.getInt(COLUMN_INDEX_REST_STATE)]);
			}
			
		} else if (command.equals(PATH_UPDATE_DB)) {
			bucketDB.update(DATABASE_TABLE, values, COLUMN_ID + "=" + rowId, null);
			getContext().getContentResolver().notifyChange(uri, null, false);
		} else if (command.equals(PATH_UPDATE_NO_NOTIFY)) {
			//getContext().getContentResolver().notifyChange(uri, null, false);
			bucketDB.update(DATABASE_TABLE, values, COLUMN_ID + "=" + rowId, null);
		} else {
			// throw exception here
		}
		
		
		return Integer.parseInt(rowId);
	}
	
	private static class BucketDBOpenHelper extends SQLiteOpenHelper {

		public BucketDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			Log.d("tag", "sqllite helper called");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {	
			// sqlite command: create table DATABASE_TABLE(KEY_ID INTEGER PRIMARY KEY, COLUMN_ENTRY TEXT, COLUMN_DATE TEXT, COLUMN_RATING TEXT, COLUMN_DONE TEXT, COLUMN_REST_STATUS TEXT, COLUMN_REST_RESULT TEXT);
			String sqlCreateTable = "create table " + DATABASE_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY, " + COLUMN_SERVER_ID + " INTEGER, " + COLUMN_DATE + " TEXT, " + COLUMN_ENTRY + " TEXT, " + COLUMN_DONE + " TEXT, " + COLUMN_RATING + " TEXT, " +  COLUMN_SHARE + " TEXT, " + COLUMN_REST_STATE + " INTEGER, " + COLUMN_REST_STATUS + " INTEGER, " + COLUMN_IMG_PATH + " TEXT, " + COLUMN_DATE_COMPLETED + " TEXT, " + COLUMN_IMG_CACHE + " TEXT, " + COLUMN_FACEBOOK_ID + " TEXT, " + COLUMN_IMG + " BLOB);" ;
			db.execSQL(sqlCreateTable);
			Log.d("tag", "sqllite new database generated");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("tag", "upgrading database");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		    onCreate(db);
		}
	}

}