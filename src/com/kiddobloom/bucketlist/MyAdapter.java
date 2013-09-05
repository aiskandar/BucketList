package com.kiddobloom.bucketlist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.RowId;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.model.GraphObject;
import com.kiddobloom.bucketlist.ImageDownloader.FlushedInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyAdapter extends SimpleCursorAdapter {

	LayoutInflater mInflater;		
	Context context;
	ImageDownloader imageDownloader = new ImageDownloader();
	MyListFragment mf;
	boolean first_time = false;
	OnPendingPublishListener callback;
	
	public interface OnPendingPublishListener {
		public void onPendingPublish(String serverId, String rowId);
	};
	
	public MyAdapter(Context c, String[] from, int[] to, MyListFragment lmf) {
		super(c, R.layout.item_layout, null, from, to, 0);
		context = c;
		mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mf = lmf;
		callback = (OnPendingPublishListener) mf;

		Log.d("tag", "MyAdapter constructor: " + callback);
		
		SharedPreferences sp;
		sp = context.getSharedPreferences(context.getString(R.string.pref_name), 0);
		first_time = sp.getBoolean(context.getString(R.string.pref_first_time_install), false);
		Log.d("tag", "first time: " + first_time);
	}
	
	public class ViewHolder {
		ImageView tw;
		TextView ap;
		//View transparency;
		//RelativeLayout tw;
		CheckBox cb;
		ImageButton ib1;
		ImageButton ib2;
		TextView ib2tv;
		TextView tv1;
		TextView tv2;
		int itemId;
		int pos;
	}
	
	public class ImageViewHolder {
		int itemId;
		int pos;
		String rating;
		String share;
		ListView listview;
	}	
	
	public class GetImageResult {
		Bitmap bmp;
		String rowId;
	}
	
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public View getView(int position, View baseview, ViewGroup vg) {

		//Log.d("tag", "getview position:" + position + " v:" + baseview + " vg:" + vg);
		ViewHolder vh = null;
		//ImageViewHolder ivh = null;
		
		Cursor c = getCursor();
		int itemId = (int) getItemId(position);
		ListView lv = (ListView) vg;

		//Log.d("tag", "cursor:" + c.getPosition() + " id:" + itemId);
		if (baseview == null) {

			// inflate the view object for the row - baseview if of type BucketListRowLayout object
			baseview = mInflater.inflate(R.layout.item_layout, vg, false);
			
			// save the child views of the base view 
			// In this case: checkbox, textviews, and ratingbar objects will be saved in ViewHolder
			// Purpose of the ViewHolder is to save the findViewById for these child views
			// The ViewHolder object will be saved in the tag of the baseview by calling setTag
			vh = new ViewHolder();
			vh.tw = (ImageView) baseview.findViewById(R.id.blPhoto);
			vh.ap = (TextView) baseview.findViewById(R.id.ap);
			//vh.transparency = (View) baseview.findViewById(R.id.transparency);
			vh.cb = (CheckBox) baseview.findViewById(R.id.ctv1);
			vh.tv1 = (TextView) baseview.findViewById(R.id.fblistitems);
			vh.tv2 = (TextView) baseview.findViewById(R.id.listItemDate);
			vh.ib1 = (ImageButton) baseview.findViewById(R.id.ib1);	
			vh.ib2 = (ImageButton) baseview.findViewById(R.id.ib2);	
			vh.ib2tv = (TextView) baseview.findViewById(R.id.ib2tv);
			
			//Log.d("tag", "container height: " + vh.tw.getMeasuredHeight() + " width:" + vh.tw.getMeasuredWidth());
			
			// save the ViewHolder
			baseview.setTag(vh);
			
			// setup the click listener for checkbox
			vh.cb.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					ImageViewHolder ivh = (ImageViewHolder) v.getTag();		
					
					Log.d("tag", "checkbox clicked id: " + ivh.itemId + " pos: " + ivh.pos + " rating: " + ivh.rating);
					
					Uri base = MyContentProvider.CONTENT_URI;
					base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
					base = Uri.withAppendedPath(base, Integer.toString(ivh.itemId));
					
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
					Date date = new Date();
					
					ContentValues cv = new ContentValues();
					boolean checked = cb.isChecked();

					cv.put(MyContentProvider.COLUMN_DONE, Boolean.toString(checked));
					cv.put(MyContentProvider.COLUMN_DATE_COMPLETED, sdf.format(date));
					context.getContentResolver().update(base, cv, null, null);
					
//					if (checked) {
//						Cursor c = getCursor();
//						String serverId = c.getString(MyContentProvider.COLUMN_INDEX_SERVER_ID);
//						String rowId = String.valueOf(c.getString(MyContentProvider.COLUMN_INDEX_ID));
//						
//						callback.onPendingPublish(serverId, rowId);
//					}
				}
			});
			
			vh.ib1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					ImageViewHolder ivh = (ImageViewHolder) v.getTag();		
					
					Log.d("tag", "star image clicked id: " + ivh.itemId + " rating: " + ivh.rating);
					
					Uri base = MyContentProvider.CONTENT_URI;
					base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
					base = Uri.withAppendedPath(base, Integer.toString(ivh.itemId));
										
					ContentValues cv = new ContentValues();
					
					if (ivh.rating.equals("false")) {
						ivh.rating = "true";
					} else {
						ivh.rating = "false";
					}
					
					cv.put(MyContentProvider.COLUMN_RATING, ivh.rating);	
					context.getContentResolver().update(base, cv, null, null);				}
			});

			vh.ib2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					ImageViewHolder ivh = (ImageViewHolder) v.getTag();		
					
					Log.d("tag", "share image clicked id: " + ivh.itemId + " share: " + ivh.share);
					
					Uri base = MyContentProvider.CONTENT_URI;
					base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
					base = Uri.withAppendedPath(base, Integer.toString(ivh.itemId));
										
					ContentValues cv = new ContentValues();
					
					if (ivh.share.equals("false")) {
						ivh.share = "true";
					} else {
						ivh.share = "false";
					}
					
					cv.put(MyContentProvider.COLUMN_SHARE, ivh.share);	
					context.getContentResolver().update(base, cv, null, null);				}
			});
			
			vh.tw.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					ImageViewHolder vh = (ImageViewHolder) v.getTag();
					Log.d("tag", "onclick row for id:" + vh.itemId + " position:" + vh.pos);
					
					ListView lv = (ListView) vh.listview;
					lv.performItemClick(v, vh.pos, vh.itemId);
				}
			});
		} else {	
			// the row view has been inflated before - get the saved child views
			vh = (ViewHolder) baseview.getTag();
			
		}
		
		// populate the checkbox and ratingbar for the data in the adapter based on
		// the persistant db storage		
		vh.tv1.setText(c.getString(MyContentProvider.COLUMN_INDEX_ENTRY));
		vh.tv2.setText(c.getString(MyContentProvider.COLUMN_INDEX_DATE));

		String checked_str = c.getString(MyContentProvider.COLUMN_INDEX_DONE);
		boolean checked = Boolean.parseBoolean(checked_str);
		
		if (vh.cb != null) {
			vh.cb.setChecked(checked);

			if (vh.cb.isChecked()) {
				vh.tv1.setPaintFlags(vh.tv1.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				vh.tv1.setPaintFlags(vh.tv1.getPaintFlags()
						& ~Paint.STRIKE_THRU_TEXT_FLAG);
			}	
		}
		
		String rating_str = c.getString(MyContentProvider.COLUMN_INDEX_RATING);
		if (rating_str.equals("false")) {
			vh.ib1.setImageResource(R.drawable.rating_not_important);
		} else {
			vh.ib1.setImageResource(R.drawable.rating_important);
		}
		
		if (lv.isItemChecked(position)) {
			vh.tw.setAlpha((float)0.7);
		} else {
			String resStr = c.getString(MyContentProvider.COLUMN_INDEX_IMG_PATH);
			// Log.d("tag", "img path: " + resStr);
			
			String cache = c.getString(MyContentProvider.COLUMN_INDEX_IMG_CACHE);			
			if (cache.equals("true")) {
				//Log.d("tag", "loading from image cache");
				byte[] byteimage = c.getBlob(MyContentProvider.COLUMN_INDEX_IMG);
				Bitmap bmp = BitmapFactory.decodeByteArray (byteimage, 0, byteimage.length);
				BitmapDrawable bd = new BitmapDrawable(bmp);
				vh.tw.setImageDrawable(bd);
				vh.ap.setVisibility(View.INVISIBLE);
				//vh.ap.setVisibility(View.VISIBLE);
			} else if (resStr.startsWith("/")) {
				//Log.d("tag", "loading image from local filesystem");
				Bitmap selectedImage = BitmapFactory.decodeFile(resStr);
			    
				int width = selectedImage.getWidth();
				int height = selectedImage.getHeight();
				//Log.d ("tag", "orig file x=" + width + " y=" + height);
				
				BitmapDrawable bd = new BitmapDrawable(selectedImage);
				vh.tw.setImageDrawable(bd);
				vh.ap.setVisibility(View.INVISIBLE);
				//vh.ap.setVisibility(View.VISIBLE);
			} else if (resStr.startsWith("http")) {
				//Log.d("tag", "loading image from http");
				//imageDownloader.download(resStr, vh.tw);
				Integer i = new Integer(itemId);
				new GetImageTask().execute(resStr, i.toString());
				
				// temporary placeholder
				vh.tw.setImageResource(R.drawable.placeholder);
				vh.ap.setVisibility(View.INVISIBLE);
				//vh.ap.setVisibility(View.VISIBLE);
			} else {
				//Log.d("tag", "loading image from resource");
				//int resIdx = Integer.parseInt(resStr); 
				int resId = Integer.parseInt(resStr);
				//int resId = Constants.resId[resIdx];
				vh.tw.setImageResource(resId);
				
				if (first_time) { 
					vh.ap.setVisibility(View.VISIBLE);
				} else {
					vh.ap.setVisibility(View.INVISIBLE);
				}
			}
			
			vh.tw.setAlpha((float)1.0);
		}

		String share_str = c.getString(MyContentProvider.COLUMN_INDEX_SHARE);
		if (share_str.equals("false")) {
			vh.ib2.setImageResource(R.drawable.share_no);
			vh.ib2tv.setText("private");
		} else {
			vh.ib2.setImageResource(R.drawable.share);
			vh.ib2tv.setText("public");
		}

		
		// save the itemId of the data in the adapter into the child views
		// purpose is for the checkbox and ratingbar event listeners to understand the
		// current adapter itemId that is using this View
		// Everytime getview is called - we reset itemId in the view object
		
		ImageViewHolder ivh = new ImageViewHolder();
		ivh.itemId = itemId;
		ivh.pos = position;
		ivh.rating = rating_str;
		ivh.share = share_str;
		ivh.listview = lv;
		
		vh.cb.setTag(ivh);
		vh.ib1.setTag(ivh);
		vh.ib2.setTag(ivh);
		vh.tw.setTag(ivh);
		vh.itemId = itemId;
		vh.pos = position;

		return baseview;
	}
	  
	/*
	 * This is the callback function for GetImageTask to bucketlist server
	 */	
	private class GetImageTask extends AsyncTask<String, Integer, GetImageResult> {

		private static final String LOG_TAG = "tag";

		@Override
		protected GetImageResult doInBackground(String... arg0) {

			Log.d("tagaa", "GetImageTask url: " + arg0[0].toString() + " rowid: " + arg0[1].toString());
			String url = arg0[0].toString();
			String rowId = arg0[1].toString();
			
			HttpEntity entity = null;
			HttpResponse resp = null;
			String response = null;

			final HttpClient client = AndroidHttpClient.newInstance("Android");
			final HttpGet getRequest = new HttpGet(url);

			try {
				resp = client.execute(getRequest);
				final int statusCode = resp.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					Log.w("ImageDownloader", "Error " + statusCode
							+ " while retrieving bitmap from " + url);
					return null;
				}

				entity = resp.getEntity();
				if (entity != null) {
					InputStream inputStream = null;
					try {
						inputStream = entity.getContent();
						// return BitmapFactory.decodeStream(inputStream);
						// Bug on slow connections, fixed in future release.
						GetImageResult result = new GetImageResult();
						result.bmp = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
						result.rowId = rowId;
						return result;
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			} catch (IOException e) {
				getRequest.abort();
				Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url,
						e);
			} catch (IllegalStateException e) {
				getRequest.abort();
				Log.w(LOG_TAG, "Incorrect URL: " + url);
			} catch (Exception e) {
				getRequest.abort();
				Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
			} finally {
				if ((client instanceof AndroidHttpClient)) {
					((AndroidHttpClient) client).close();
				}
			}
			return null;
		}

		/*
		 * An InputStream that skips the exact number of bytes provided, unless
		 * it reaches EOF.
		 */
		class FlushedInputStream extends FilterInputStream {
			public FlushedInputStream(InputStream inputStream) {
				super(inputStream);
			}

			@Override
			public long skip(long n) throws IOException {
				long totalBytesSkipped = 0L;
				while (totalBytesSkipped < n) {
					long bytesSkipped = in.skip(n - totalBytesSkipped);
					if (bytesSkipped == 0L) {
						int b = read();
						if (b < 0) {
							break; // we reached EOF
						} else {
							bytesSkipped = 1; // we read one byte
						}
					}
					totalBytesSkipped += bytesSkipped;
				}
				return totalBytesSkipped;
			}
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
			Log.d("tagaa", "progress: " + progress[0]);
		}

		protected void onPostExecute(GetImageResult result) {
			
			if (result != null) {
				Log.d("tag", "GetImageTask result.row: " + result.rowId);
		
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				result.bmp.compress(Bitmap.CompressFormat.JPEG, 75, out);
	
				Uri base = MyContentProvider.CONTENT_URI;
				ContentValues cv = new ContentValues();
				cv.put(MyContentProvider.COLUMN_IMG_CACHE, "true");
				cv.put(MyContentProvider.COLUMN_IMG, out.toByteArray());
				cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_NONE);
				cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_SYNCED);
	
				base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE_DB);
				base = Uri.withAppendedPath(base, result.rowId);
	
				MyApplication.context().getContentResolver().update(base, cv, null, null);		
			}
		}
	}


}
