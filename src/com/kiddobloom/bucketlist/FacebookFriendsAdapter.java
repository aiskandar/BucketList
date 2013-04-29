package com.kiddobloom.bucketlist;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

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
		int pos;
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
		
		ListView lv = (ListView) vg;
		
		//Log.d("tag", "cursor:" + c.getPosition() + " id:" + itemId);
		
		if (baseview == null) {

			// inflate the view object for the row - baseview if of type BucketListRowLayout object
			baseview = mInflater.inflate(R.layout.facebook_friends_item_layout, vg, false);
			
			// save the child views of the base view 
			// In this case: checkbox, textviews, and ratingbar objects will be saved in ViewHolder
			// Purpose of the ViewHolder is to save the findViewById for these child views
			// The ViewHolder object will be saved in the tag of the baseview by calling setTag
			vh = new ViewHolder();
			vh.qb =  (ProfilePictureView) baseview.findViewById(R.id.profilepic);
			vh.tv1 = (TextView) baseview.findViewById(R.id.blogHeader);
			
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

			
		} else {	
			// the row view has been inflated before - get the saved child views
			vh = (ViewHolder) baseview.getTag();
		}
		
		FriendData fd = this.getItem(position);
		
		//Log.d("tag"," fd: " +fd);
		vh.tv1.setText(fd.name);
		vh.qb.setProfileId(fd.userId);

		baseview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				ViewHolder vh = (ViewHolder) v.getTag();
//				Log.d("tag", "onclick row for id:" + vh.itemId + " position:" + vh.pos);
//				
//				ListView lv = (ListView) v.getParent();
//				lv.performItemClick(v, vh.pos, vh.itemId);
			}
		});
		
		return baseview;
	}
	
}
