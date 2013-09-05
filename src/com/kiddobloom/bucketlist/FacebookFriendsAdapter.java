package com.kiddobloom.bucketlist;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.Adapter;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FacebookFriendsAdapter extends ArrayAdapter<FriendData> {

	LayoutInflater mInflater;		
	Context context;
	public ArrayList<FriendData> fl;
	
	public FacebookFriendsAdapter(Context context, int resource, ArrayList<FriendData> friendsList) {
		
		super(context, resource, friendsList);
		// TODO Auto-generated constructor stub
		this.context = context;
		fl = friendsList;
		
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
		
		new GetFriendListTask().execute(new GetFriendListParam(fd, this, itemId, position, lv));
		
		// check to see if facebook userId is already registered at bucketlist server
		if (fd.bucketList[0] != null) { 
			
			Log.d("tag", "bucketlist exists for facebook id: " + fd.facebookId);
			
			View vt = (View) vh.ib;
			vh.ib.setVisibility(View.GONE);		
			vh.tv2.setVisibility(View.VISIBLE);

			String myHtmlString = null;
			if (fd.bucketList[0] != null && fd.bucketList[1] != null) {
				myHtmlString = "&#8226; " + fd.bucketList[0] + "<br/>";
				myHtmlString += "&#8226; " + fd.bucketList[1] + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
			} else if (fd.bucketList[0] != null) {
				myHtmlString = "&#8226; " + fd.bucketList[0] + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
			} else if (fd.bucketList[1] != null) {
				myHtmlString = "&#8226; " + fd.bucketList[1] + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
			} else {
				myHtmlString = fd.name + "'s bucket list are private" + "<br/>";
			}
			
			vh.tv2.setText(Html.fromHtml(myHtmlString));
		} else {
			vh.ib.setVisibility(View.VISIBLE);
			vh.tv2.setVisibility(View.GONE);

		}

		ButtonViewHolder ivh = new ButtonViewHolder();
		ivh.facebook_id = fd.facebookId;
		ivh.position = position;
		
		// set tag
		vh.ib.setTag(ivh);
		vh.tv2.setTag(ivh);
		
		return baseview;
	}

	public class GetFriendListParam {
		FriendData fd;
		FacebookFriendsAdapter adapter;
		int rowId;
		int pos;
		ListView lv;
		
		public GetFriendListParam(FriendData fd, FacebookFriendsAdapter adapter, int rowId, int pos, ListView lv) {
			this.fd = fd;
			this.adapter = adapter;
			this.rowId = rowId;
			this.pos = pos;
			this.lv = lv;
		}
	}
	
	/*
	 * This is the callback function for RegisterTask to bucketlist server
	 */	
	private class GetFriendListTask extends AsyncTask<GetFriendListParam, Integer, String> {

		String[] list;
		FacebookFriendsAdapter adapter;
		ListView lv;
		FriendData fd;
		
		@Override
		protected String doInBackground(GetFriendListParam... arg0) {

			GetFriendListParam param = arg0[0];
			fd = param.fd;
			int rowId = param.rowId;
			int pos = param.pos;
			adapter = param.adapter;
			lv = param.lv;
			
			int currentFirstPos = lv.getFirstVisiblePosition();
			int currentLastPos = lv.getLastVisiblePosition();

//			Log.d("tag", "getView for position: " + pos);
//			Log.d("tag", "first visible position: " + currentFirstPos);
//			Log.d("tag", "last visible position: " + currentLastPos);
			
			// this means we can skip server call
			if (pos < currentFirstPos || pos > currentLastPos) {
				Log.d("tag", "skip " + pos);
				return "skip";
			}
			
			final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("fbid", fd.facebookId));

			HttpEntity entity = null;
			HttpResponse resp = null;
			String response = null;

			try {
				entity = new UrlEncodedFormEntity(nvp);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				final HttpPost post = new HttpPost(
						"http://bucketlist.kiddobloom.com/get_friend_list.php");
				post.addHeader(entity.getContentType());
				post.setEntity(entity);

				HttpClient mHttpClient = new DefaultHttpClient();
				resp = mHttpClient.execute(post);
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

			if (resp != null) {
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (response != null) {
						//Log.d("tagaa", "server response:" + response + ":");
	
						if (!response.equals("null\n")) {
							
							// decode JSON
							Gson m2Json = new Gson();
							Type type = new TypeToken<String[]>(){}.getType();
							list = m2Json.fromJson(response, type);
							
							for (int i=0 ; i < list.length ; i++) {
								Log.d("tag", i + " : " + list[i]);
								fd.bucketList[i] = list[i];
							}	
						}

					}
				} else {
					Log.d("tagaa", "server error " + resp.getStatusLine());
					response = "error:" + resp.getStatusLine();
				}
			}

			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
			Log.d("tagaa", "progress: " + progress[0]);
		}

		protected void onPostExecute(String response) {

			if (response != null) {
				
				if (response.equals("null\n")) {
					
				} else if (response.equals("skip")) {
					
				} else if (response.contains("error")) {
					
				} else {
					Log.d("tag", response);
					if (fd.notified == false) {
						adapter.notifyDataSetChanged();
						fd.notified = true;
					}
				}
				
			} else {
				// no response from the server
				
				Toast.makeText(context,
		                "No response from server. Pls check network connection",
		                Toast.LENGTH_LONG).show();

			}
		}
	}	

}
