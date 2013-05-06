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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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
	public static final String PATH_UPDATE_NO_NOTIFY = "update_no_notify";
	public static final String PATH_DELETE = "delete";
	public static final String PATH_DELETE_DB = "delete_db";
	public static final String PATH_DELETE_NO_NOTIFY = "delete_no_notify";
	
	private static final String DATABASE_NAME = "bucketList.db";
	private static final int DATABASE_VERSION = 15;
	
	// Database table name
	public static final String DATABASE_TABLE = "bucket";
	
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
	
	public static final String dbColumnStr[] = {COLUMN_ID, COLUMN_SERVER_ID, COLUMN_DATE, COLUMN_ENTRY, 
										COLUMN_DONE, COLUMN_RATING, COLUMN_SHARE, COLUMN_REST_STATE, COLUMN_REST_STATUS };
	
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
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		Log.d("tag", "bulk insert Uri " + uri);
		
		List<String> ls = null;
		ls = uri.getPathSegments();
		Log.d("tag", "path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
		}
		
		int size = values.length;
		for (int i=0; i<size; i++) {
			bucketDB.insert(DATABASE_TABLE, null, values[i]);
		}
		
		getContext().getContentResolver().notifyChange(uri, null, true);
		
		return 0;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		Log.d("tag", "query Uri " + uri.toString());
		
		List<String> ls = null;
		ls = uri.getPathSegments();
		Log.d("tag", "path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
		}
		
		Cursor cur = bucketDB.query(DATABASE_TABLE, projection, selection, selectionArgs, null, null, COLUMN_RATING + " DESC");
		cur.setNotificationUri(getContext().getContentResolver(), uri);
		
		SharedPreferences sp = getContext().getSharedPreferences(getContext().getString(R.string.pref_name), 0);
		AccountManager accountManager = AccountManager.get(getContext());

		boolean synced = sp.getBoolean(getContext().getString(R.string.pref_initial_sync_key), false);
		Log.d("tag", "sync: " + synced);
		
		if (synced == false) {
			Account[] accounts = accountManager.getAccountsByType("com.kiddobloom");		
			for (int i=0 ; i < accounts.length ; i++) {
				Log.d("tag", "requesting query sync for account: " + accounts[i].name);
			
				Bundle extras = new Bundle();
				extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				ContentResolver.requestSync(accounts[i], MyContentProvider.AUTHORITY, extras);
//			
//			boolean var = ContentResolver.isSyncActive(accounts[i], MyContentProvider.AUTHORITY);
//			boolean var2 = ContentResolver.isSyncPending(accounts[i], MyContentProvider.AUTHORITY);
//			int var3 = ContentResolver.getIsSyncable(accounts[i], MyContentProvider.AUTHORITY);
//			
//			Log.d("tag", " sync active: " + var);
//			Log.d("tag", " sync pending: " + var2);
//			Log.d("tag", " syncable: " + var3);
			}
			
			SharedPreferences.Editor editor = sp.edit();			
			editor.putBoolean(getContext().getString(R.string.pref_initial_sync_key), true);
			editor.commit();
			
		}
		return cur;
	}

	@Override
	public String getType(Uri uri) {
		Log.d("tag", "getType " + uri);
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		Log.d("tag", "insert Uri " + uri);
		
		List<String> ls = null;
		ls = uri.getPathSegments();
		String command = null;
		
		Log.d("tag", "path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
			if (i == 1) {
				command = ls.get(i).toString();
			} 
		}
		
		bucketDB.insert(DATABASE_TABLE, null, values);
		
		if (command.equals(PATH_INSERT)) {
			getContext().getContentResolver().notifyChange(uri, null, true);
		} else if (command.equals(PATH_INSERT_NO_NOTIFY)) {
			//getContext().getContentResolver().notifyChange(uri, null, false);
		} else {
			// throw exception here
		}

		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		Log.d("tag", "delete Uri " + uri);

		List<String> ls = null;
		ls = uri.getPathSegments();
		String command = null;
		
		Log.d("tag", "path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
			if (i == 1) {
				command = ls.get(i).toString();
			} 
		}
		
		if (command.equals(PATH_DELETE)) {
			
			for (int i = 2 ; i < ls.size() ; i++) {
				Log.d("tag", "rowID to delete: " + ls.get(i).toString());
				
				ContentValues cv = new ContentValues();	
				cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_DELETE);
				cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
				bucketDB.update(DATABASE_TABLE, cv, COLUMN_ID + "=" + ls.get(i).toString(), null);
							
				//bucketDB.delete(DATABASE_TABLE, COLUMN_ID + "=" + ls.get(i).toString(), null);
			}

			getContext().getContentResolver().notifyChange(uri, null, true);
			
		} else if (command.equals(PATH_DELETE_DB)) {
			
			for (int i = 2 ; i < ls.size() ; i++) {
				Log.d("tag", "rowID to REALLY delete: " + ls.get(i).toString());
				bucketDB.delete(DATABASE_TABLE, COLUMN_ID + "=" + ls.get(i).toString(), null);
				getContext().getContentResolver().notifyChange(uri, null, true);
			} 
		} else if (command.equals(PATH_DELETE_NO_NOTIFY)) {
			for (int i = 2 ; i < ls.size() ; i++) {
				Log.d("tag", "rowID to REALLY delete: " + ls.get(i).toString());
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

		Log.d("tag", "update Uri " + uri);

		List<String> ls = null;
		ls = uri.getPathSegments();
		String rowId = null;
		String command = null;
		
		Log.d("tag", "path segments: " + ls + " with size: " + ls.size());
		
		for (int i = 0 ; i < ls.size() ; i++) {
			Log.d("tag", "uri path " + i + " is " + ls.get(i).toString());
			
			if (i == 1) {
				command = ls.get(i).toString();
			} else if (i == 2) {
				rowId = ls.get(i).toString();
			}
		}

		if (rowId != null) {	
			bucketDB.update(DATABASE_TABLE, values, COLUMN_ID + "=" + rowId, null);
		}
		
		if (command.equals(PATH_UPDATE)) {
			getContext().getContentResolver().notifyChange(uri, null, true);
		} else if (command.equals(PATH_UPDATE_NO_NOTIFY)) {
			//getContext().getContentResolver().notifyChange(uri, null, false);
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
			String sqlCreateTable = "create table " + DATABASE_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY, " + COLUMN_SERVER_ID + " INTEGER, " + COLUMN_DATE + " TEXT, " + COLUMN_ENTRY + " TEXT, " + COLUMN_DONE + " TEXT, " + COLUMN_RATING + " TEXT, " +  COLUMN_SHARE + " TEXT, " + COLUMN_REST_STATE + " INTEGER, " + COLUMN_REST_STATUS + " INTEGER);" ;
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