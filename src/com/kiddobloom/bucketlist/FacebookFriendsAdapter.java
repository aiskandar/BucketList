package com.kiddobloom.bucketlist;

import java.util.List;

import android.content.Context;
import android.content.Intent;
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
import com.kiddobloom.bucketlist.MyAdapter.ImageViewHolder;

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
			vh.ib = (Button) baseview.findViewById(R.id.fbinvitebutton);
			vh.tv2 = (TextView) baseview.findViewById(R.id.fblistitems);
			
			// save the ViewHolder
			baseview.setTag(vh);
			
//			// setup the click listener for checkbox
//			vh.cb.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					CheckBox cb = (CheckBox) v;
//					Integer id;
//					id = (Integer) v.getTag();
//					//Log.d("tag", "checkbox clicked for id:" + id + " checked:" + cb.isChecked());
//					
//					Uri base = MyContentProvider.CONTENT_URI;
//					base = Uri.withAppendedPath(base, "edit");
//					base = Uri.withAppendedPath(base, Integer.toString(id));
//					
//					ContentValues cv = new ContentValues();
//					boolean checked = cb.isChecked();
//					cv.put(MyContentProvider.COLUMN_DONE, Boolean.toString(checked));					
//					context.getContentResolver().update(base, cv, null, null);
//				}
//			});

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
		if (fd.exists != null) { 
			
			View vt = (View) vh.ib;
			vh.ib.setVisibility(View.GONE);		
			vh.tv2.setVisibility(View.VISIBLE);

			String myHtmlString = null;
			if (fd.entry0 != null && fd.entry1 != null) {
				myHtmlString = "&#8226; " + fd.entry0 + "<br/>";
				myHtmlString += "&#8226; " + fd.entry1 + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
			} else if (fd.entry0 != null) {
				myHtmlString = "&#8226; " + fd.entry0 + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
				myHtmlString += "&#8226; " + "..." + "<br/>";
			} else if (fd.entry1 != null) {
				myHtmlString = "&#8226; " + fd.entry1 + "<br/>";
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
	
}
