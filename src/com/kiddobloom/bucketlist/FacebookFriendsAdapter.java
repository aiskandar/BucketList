package com.kiddobloom.bucketlist;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.RequestsDialogBuilder;

import com.facebook.widget.WebDialog.OnCompleteListener;
import com.kiddobloom.bucketlist.MyAdapter.GetImageResult;
import com.kiddobloom.bucketlist.MyAdapter.GetImageTask;
import com.kiddobloom.bucketlist.MyAdapter.ImageViewHolder;
import com.kiddobloom.bucketlist.MyAdapter.GetImageTask.FlushedInputStream;

public class FacebookFriendsAdapter extends ArrayAdapter<FriendData> {

	LayoutInflater mInflater;		
	Context context;
	
	public FacebookFriendsAdapter(Context context, int resource, List objects) {
		
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}
		
	public class ViewHolder {
		ProfilePictureView qb;
		TextView tv1;
		Button ib;
		TextView tv2;
		int pos;
	}
	
	public class ButtonViewHolder {
		String facebook_id;
		int position;
	}
		
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public View getView(int position, View baseview, ViewGroup vg) {

		//Log.d("tagfb", "getview position:" + position);
		ViewHolder vh = null;
		int itemId = (int) getItemId(position);
		
		ListView lv = (ListView) vg;
		
		//Log.d("tagfb", "position:" + position + " id:" + itemId);
		
		if (baseview == null) {

			// inflate the view object for the row - baseview if of type BucketListRowLayout object
			baseview = mInflater.inflate(R.layout.facebook_friends_item_layout, vg, false);
			
			// save the child views of the base view 
			// In this case: checkbox, textviews, and ratingbar objects will be saved in ViewHolder
			// Purpose of the ViewHolder is to save the findViewById for these child views
			// The ViewHolder object will be saved in the tag of the baseview by calling setTag
			vh = new ViewHolder();
			vh.qb =  (ProfilePictureView) baseview.findViewById(R.id.fbprofilepic);
			vh.tv1 = (TextView) baseview.findViewById(R.id.fbprofile);
			vh.ib = (Button) baseview.findViewById(R.id.invitebutton);
			vh.tv2 = (TextView) baseview.findViewById(R.id.fblistitems);
			
			// save the ViewHolder
			baseview.setTag(vh);
			
			vh.tv2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					TextView tvl = (TextView) v;
					ButtonViewHolder ivh = (ButtonViewHolder) v.getTag();
					Log.d("tag", "preview texts clickded fb_id: " + ivh.facebook_id + " for position: " + ivh.position);
					
					Intent launch = new Intent(getContext(), DetailedEntryActivity.class);
					launch.putExtra("com.kiddobloom.bucketlist.current_tab", BucketListActivity.currentTab);
					launch.putExtra("com.kiddobloom.bucketlist.facebook_id", ivh.facebook_id);
					getContext().startActivity(launch);	
				}
			});
			
			vh.ib.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Button ib = (Button) v;
					ButtonViewHolder ivh = (ButtonViewHolder) v.getTag();
					Log.d("tagfb", "invite button clicked fb_id: " + ivh.facebook_id + " for position: " + ivh.position);
				
			        Bundle params = new Bundle();  
			        params.putString("title", "Bucket List app");
			        params.putString("message", "I want you to check out the Bucketlist Android app");
			        params.putString("to", ivh.facebook_id);
			        //params.putString("redirect_url", "https://play.google.com");
			        //params.putString("app_id", getContext().getResources().getString(R.string.app_id));
			        WebDialog requestsDialog = new WebDialog.RequestsDialogBuilder(getContext(), Session.getActiveSession(), params).build(); 
			        requestsDialog.setOnCompleteListener(new OnCompleteListener() {

		                @Override
		                public void onComplete(Bundle values,
		                    FacebookException error) {
		                    if (error != null) {
		                        if (error instanceof FacebookOperationCanceledException) {
		                            Toast.makeText(getContext(), 
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(getContext(), 
		                                "Network Error", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    } else {
		                        final String requestId = values.getString("request");
		                        if (requestId != null) {
		                            Toast.makeText(getContext(), 
		                                "Request sent",  
		                                Toast.LENGTH_SHORT).show();
		                        } else {
		                            Toast.makeText(getContext(), 
		                                "Request cancelled", 
		                                Toast.LENGTH_SHORT).show();
		                        }
		                    }   
		                }

		            });
			        
			        requestsDialog.show();
			     }
			});

			
		} else {	
			// the row view has been inflated before - get the saved child views
			vh = (ViewHolder) baseview.getTag();
		}
		
		FriendData fd = this.getItem(position);
		//Log.d("tag"," fd: " +fd);
		vh.tv1.setText(fd.name);
		vh.qb.setProfileId(fd.facebookId);
		
		//Log.d("tag", "facebook id: " + fd.userId + " exists: " + fd.exists);
		
		// check to see if facebook userId is already registered at bucketlist server
		new CheckFriendTask().execute(resStr, i.toString());
		
//		if (fd.exists != null) { 
//			
//			View vt = (View) vh.ib;
//			vh.ib.setVisibility(View.GONE);		
//			vh.tv2.setVisibility(View.VISIBLE);
//
//			String myHtmlString = null;
//			if (fd.entry0 != null && fd.entry1 != null) {
//				myHtmlString = "&#8226; " + fd.entry0 + "<br/>";
//				myHtmlString += "&#8226; " + fd.entry1 + "<br/>";
//				myHtmlString += "&#8226; " + "..." + "<br/>";
//			} else if (fd.entry0 != null) {
//				myHtmlString = "&#8226; " + fd.entry0 + "<br/>";
//				myHtmlString += "&#8226; " + "..." + "<br/>";
//				myHtmlString += "&#8226; " + "..." + "<br/>";
//			} else if (fd.entry1 != null) {
//				myHtmlString = "&#8226; " + fd.entry1 + "<br/>";
//				myHtmlString += "&#8226; " + "..." + "<br/>";
//				myHtmlString += "&#8226; " + "..." + "<br/>";
//			} else {
//				myHtmlString = fd.name + "'s bucket list are private" + "<br/>";
//			}
//			
//			vh.tv2.setText(Html.fromHtml(myHtmlString));
//		} else {
//			vh.ib.setVisibility(View.VISIBLE);
//			vh.tv2.setVisibility(View.GONE);
//		}

		ButtonViewHolder ivh = new ButtonViewHolder();
		ivh.facebook_id = fd.facebookId;
		ivh.position = position;
		
		// set tag
		vh.ib.setTag(ivh);
		vh.tv2.setTag(ivh);
		
		return baseview;
	}

	
	/*
	 * This is the callback function for GetImageTask to bucketlist server
	 */	
	private class CheckFriendTask extends AsyncTask<String, Integer, GetImageResult> {

		private static final String LOG_TAG = "tag";

		@Override
		protected GetImageResult doInBackground(String... arg0) {

			Log.d("tagaa", "CheckFriendTask - rowid: " + arg0[0].toString());
			String rowid = arg0[0].toString();
			
			HttpEntity entity = null;
			HttpResponse resp = null;
			String response = null;

			try {
				final HttpGet get = new HttpGet(
						"http://23.20.35.242/check_friend.php?fbid=" + rowid);
				
				HttpClient mHttpClient = new DefaultHttpClient();
				resp = mHttpClient.execute(get);
				response = EntityUtils.toString(resp.getEntity());
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d("tag", "CheckFriendTask: response = " + response);

				boolean error = response.startsWith("error:");
				
				if (error == true) {
					String arr[] = response.split(":");
				
					Log.d("tag", "CheckFriendTask: error response");
					for(int i=0; i < arr.length ; i++) {
						Log.d("tag", "error: " + arr[i]);
					}
					
				} else {
					
					// decode JSON
					
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
