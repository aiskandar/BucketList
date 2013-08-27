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
import java.util.Date;
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

import com.kiddobloom.bucketlist.ImageDownloader.FlushedInputStream;

import android.content.ContentValues;
import android.content.Context;
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
	
	public MyAdapter(Context c, String[] from, int[] to) {
		super(c, R.layout.item_layout, null, from, to, 0);
		context = c;
		mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public class ViewHolder {
		ImageView tw;
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
					Integer id;
					id = (Integer) v.getTag();
					//Log.d("tag", "checkbox clicked for id:" + id + " checked:" + cb.isChecked());
					
					Uri base = MyContentProvider.CONTENT_URI;
					base = Uri.withAppendedPath(base, MyContentProvider.PATH_UPDATE);
					base = Uri.withAppendedPath(base, Integer.toString(id));
					
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
					Date date = new Date();
					
					ContentValues cv = new ContentValues();
					boolean checked = cb.isChecked();
					cv.put(MyContentProvider.COLUMN_DONE, Boolean.toString(checked));
					cv.put(MyContentProvider.COLUMN_DATE_COMPLETED, sdf.format(date));
					//cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_UPDATE);
					//cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
					context.getContentResolver().update(base, cv, null, null);
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
					//cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_UPDATE);
					//cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
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
					//cv.put(MyContentProvider.COLUMN_REST_STATE, MyContentProvider.REST_STATE_UPDATE);
					//cv.put(MyContentProvider.COLUMN_REST_STATUS, MyContentProvider.REST_STATUS_TRANSACTING);
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
			} else if (resStr.startsWith("/")) {
				//Log.d("tag", "loading image from local filesystem");
				Bitmap selectedImage = BitmapFactory.decodeFile(resStr);
			    
				int width = selectedImage.getWidth();
				int height = selectedImage.getHeight();
				//Log.d ("tag", "orig file x=" + width + " y=" + height);
				
				BitmapDrawable bd = new BitmapDrawable(selectedImage);
				vh.tw.setImageDrawable(bd);
			} else if (resStr.startsWith("http")) {
				//Log.d("tag", "loading image from http");
				//imageDownloader.download(resStr, vh.tw);
				Integer i = new Integer(itemId);
				new GetImageTask().execute(resStr, i.toString());
				
				// temporary placeholder
				vh.tw.setImageResource(R.drawable.placeholder);
			} else {
				//Log.d("tag", "loading image from resource");
				int resIdx = Integer.parseInt(resStr); 
				vh.tw.setImageResource(Constants.resId[resIdx]);
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
		
		vh.cb.setTag(itemId);
		vh.ib1.setTag(ivh);
		vh.ib2.setTag(ivh);
		vh.tw.setTag(ivh);
		vh.itemId = itemId;
		vh.pos = position;

		return baseview;
	}
	
//	  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
//		    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//		        bitmap.getHeight(), Config.ARGB_8888);
//		    Canvas canvas = new Canvas(output);
//		 
//		    final int color = 0xff424242;
//		    final Paint paint = new Paint();
//		    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//		    final RectF rectF = new RectF(rect);
//		    final float roundPx = 12;
//		 
//		    paint.setAntiAlias(true);
//		    canvas.drawARGB(0, 0, 0, 0);
//		    paint.setColor(color);
//		    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//		 
//		    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
//		    canvas.drawBitmap(bitmap, rect, rect, paint);
//		 
//		    return output;
//		  }
	  
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
			
			// final ArrayList<NameValuePair> nvp = new
			// ArrayList<NameValuePair>();
			// nvp.add(new BasicNameValuePair("fbid", arg0[0].toString()));
			// nvp.add(new BasicNameValuePair("fbid", "100001573160170"));

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
			// if (resp != null) {
			// if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// if (response != null) {
			// Log.d("tagaa", "server response:" + response);
			// }
			// } else {
			// Log.d("tagaa", "server error " + resp.getStatusLine());
			// response = "error:" + resp.getStatusLine();
			// }
			// }
			// //return response;
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
			
			// if (result != null) {
			// boolean error = result.startsWith("error:");
			//
			// if (error == true) {
			// String arr[] = result.split(":");
			//
			// if (arr.length == 3) {
			// Log.d("tagaa", arr[0] + " " + arr[1] + " " + arr[2]);
			// } else if (arr.length == 2) {
			// Log.d("tagaa", arr[0] + " " + arr[1]);
			// } else if (arr.length == 1) {
			// Log.d("tagaa", arr[0]);
			// }
			//
			// Toast.makeText(getApplicationContext(),
			// "Failed to register userid on the server - OFFLINE mode",
			// Toast.LENGTH_LONG).show();
			//
			// saveState(StateMachine.OFFLINE_STATE);
			// saveStatus(StateMachine.ERROR_STATUS);
			// saveError(StateMachine.FBID_SERVER_REGISTER_ERROR);
			// goToBucketListActivity();

			// } else {
			// Log.d("tagaa", "completed facebook id registration");

			// // save the registered flag to true in preferences db
			// saveUserIdRegistered(true);
			//
			// // check whether facebook friends are already registered at
			// bucketlist server
			// saveState(StateMachine.FBFRIENDS_CHECK_STATE);
			// saveStatus(StateMachine.TRANSACTING_STATUS);
			// saveError(StateMachine.NO_ERROR);
			// new CheckFriendsTask().execute();
			// }
			// } else {
			// no response from the server

			// Toast.makeText(getApplicationContext(),
			// "No response from server. Pls check network connection - OFFLINE mode",
			// Toast.LENGTH_LONG).show();
			//
			// saveState(StateMachine.OFFLINE_STATE);
			// saveStatus(StateMachine.ERROR_STATUS);
			// saveError(StateMachine.FBID_SERVER_REGISTER_ERROR);
			// goToBucketListActivity();
			// }
		}
	}
}
