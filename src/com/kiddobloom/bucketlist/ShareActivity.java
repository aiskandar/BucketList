package com.kiddobloom.bucketlist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import android.app.Activity;

public class ShareActivity extends Activity implements OnClickListener {

	LinearLayout name = null;
	//LinearLayout radio = null;
	CheckBox cb;
	AccountManager am;
	SharedPreferences sp;
	
	public ShareActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	  	getActionBar().setTitle("Bucket List");
    	getActionBar().setSubtitle("by kiddoBLOOM");   
    	
    	am = AccountManager.get(this);
		sp = getSharedPreferences(getString(R.string.pref_name), 0);
		

		
    	setContentView(R.layout.share_activity);
    	
    	// disable all views
    	cb = (CheckBox) findViewById(R.id.checkBox1);
    	
    	name = (LinearLayout) findViewById(R.id.nameWrapper);
//    	radio = (LinearLayout) findViewById(R.id.avatarWrapper);
    	
//    	enableDisableViewGroup(name, false);
//    	enableDisableViewGroup(radio, false);
    	
    	cb.setOnClickListener(this);
    			
	}
	
	public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
		
	    int childCount = viewGroup.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	    	View view = viewGroup.getChildAt(i);
	    	view.setEnabled(enabled);
		    if (view instanceof ViewGroup) {
		        enableDisableViewGroup((ViewGroup) view, enabled);
		    }
		}
	}

	@Override
	public void onClick(View v) {
		
		CheckBox cb = (CheckBox) v;
		
//		if (cb.isChecked() == true) {
//		   	enableDisableViewGroup(name, true);
//	    	enableDisableViewGroup(radio, true);	
//		} else {
//	    	enableDisableViewGroup(name, false);
//	    	enableDisableViewGroup(radio, false);
//		}
	}
	
	public void goNext(View v) {
		
		int user_id = sp.getInt(getString(R.string.pref_fb_userid_key), 0);
		
		if (cb.isChecked() == true) {
			// save the settings in preferences
			SharedPreferences.Editor editor = sp.edit();
			
			EditText et = (EditText) name.findViewById(R.id.name_edit);
//			RadioGroup rg = (RadioGroup) radio.findViewById(R.id.radioGroup1);

//			if (et == null || rg == null) {
//				return;
//			}

			// create the Android account for sync adapter
			final Account account = new Account(et.getText().toString(),
					Constants.ACCOUNT_TYPE);
			am.addAccountExplicitly(account, null, null);
			ContentResolver.setSyncAutomatically(account,
					MyContentProvider.AUTHORITY, true);

//			int radioButtonId = rg.getCheckedRadioButtonId();
//			View radioButton = rg.findViewById(radioButtonId);
//			int radioPos = rg.indexOfChild(radioButton);
//
//			Log.d("tag", "radio button checked:" + radioPos);
//			
//			editor.putBoolean(getString(R.string.pref_share_key), cb.isChecked());
//			editor.putString(getString(R.string.pref_name_key), et.getText().toString());
//			editor.putInt(getString(R.string.pref_avatar_key), radioPos);
//			
//			editor.commit();
//			
//			// register the casual name to the server 
//			new RegisterNameTask().execute(et.getText().toString(), String.valueOf(user_id));
//			
		} 
//		
//    	Intent launch = new Intent(this, BucketListActivity.class);
//    	startActivity(launch);
//    	finish();
	}
	
	private class RegisterNameTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {

			Log.d("tag", "name: " + arg0[0].toString());
			Log.d("tag", "userid: " + arg0[1].toString());

			final ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
			nvp.add(new BasicNameValuePair("name", arg0[0].toString()));
			nvp.add(new BasicNameValuePair("userid", arg0[1].toString()));
			
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
						"http://andyiskandar.me/register_name.php");
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

			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.d("tag", "response: " + response);
			} else {
				Log.d("tag", "Server error " + resp.getStatusLine());
				response = "error:" + resp.getStatusLine();
			}

			return response;
		}

		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
			Log.d("tag", "progress: " + progress[0]);
		}

		protected void onPostExecute(String result) {

			//tv = (TextView) findViewById(R.id.message);
			boolean error = result.startsWith("error:");
			
			if (error == true) {
				String arr[] = result.split(":");
				//tv.setText(arr[1]);				

			} else {
				// save the user_id in the preferences
				SharedPreferences.Editor editor = sp.edit();
				// Log.d("tag", "userid: " + Integer.parseInt(result));
				//editor.putInt(getString(R.string.pref_userid_key), Integer.parseInt(result));
				//editor.commit();

				// go to the next screen
				//Intent launch = new Intent(getApplicationContext() , BucketListActivity.class);
				//startActivity(launch);
				//finish();
			}
		}

	}
}