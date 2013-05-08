package com.kiddobloom.bucketlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MyAdapter extends SimpleCursorAdapter {

	LayoutInflater mInflater;		
	Context context;
	int resId[] = {R.drawable.path, R.drawable.faith, R.drawable.pray};
	
	public MyAdapter(Context c, String[] from, int[] to) {
		super(c, R.layout.item_layout, null, from, to, 0);
		context = c;
		mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public class ViewHolder {
		LinearLayout tw;
		CheckBox cb;
		ImageButton ib1;
		ImageButton ib2;
		TextView tv1;
		TextView tv2;
		int itemId;
		int pos;
	}
	
	public class ImageViewHolder {
		int itemId;
		String rating;
		String share;
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
			vh.tw = (LinearLayout) baseview.findViewById(R.id.textWrapper);
			vh.cb = (CheckBox) baseview.findViewById(R.id.ctv1);
			vh.tv1 = (TextView) baseview.findViewById(R.id.blogHeader);
			vh.tv2 = (TextView) baseview.findViewById(R.id.textView2);
			vh.ib1 = (ImageButton) baseview.findViewById(R.id.ib1);	
			vh.ib2 = (ImageButton) baseview.findViewById(R.id.ib2);	
			
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
					
					ContentValues cv = new ContentValues();
					boolean checked = cb.isChecked();
					cv.put(MyContentProvider.COLUMN_DONE, Boolean.toString(checked));					
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
			//Log.d("tag2", "yellow");
			vh.tw.setBackgroundResource(R.color.paper);
		} else {
			//Log.d("tag2", "white");
			int resIdx = resId[position % 3];
			vh.tw.setBackgroundResource(resIdx);
		}

		String share_str = c.getString(MyContentProvider.COLUMN_INDEX_SHARE);
		if (share_str.equals("false")) {
			vh.ib2.setImageResource(R.drawable.share_no);
		} else {
			vh.ib2.setImageResource(R.drawable.share);
		}

		baseview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				ViewHolder vh = (ViewHolder) v.getTag();
				Log.d("tag", "onclick row for id:" + vh.itemId + " position:" + vh.pos);
				
				ListView lv = (ListView) v.getParent();
				lv.performItemClick(v, vh.pos, vh.itemId);
			}
		});
		
		// save the itemId of the data in the adapter into the child views
		// purpose is for the checkbox and ratingbar event listeners to understand the
		// current adapter itemId that is using this View
		// Everytime getview is called - we reset itemId in the view object
		
		ImageViewHolder ivh = new ImageViewHolder();
		ivh.itemId = itemId;
		ivh.rating = rating_str;
		ivh.share = share_str;
		
		vh.cb.setTag(itemId);
		vh.ib1.setTag(ivh);
		vh.ib2.setTag(ivh);
		vh.itemId = itemId;
		vh.pos = position;
		
		return baseview;
	}
	
}
