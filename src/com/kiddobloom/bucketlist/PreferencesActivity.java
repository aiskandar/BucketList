package com.kiddobloom.bucketlist;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class PreferencesActivity extends SherlockActivity implements OnClickListener {

	SharedPreferences sp;
	CheckBox registered;
	CheckBox skip;
	CheckBox synced;
	TextView tv;
	TextView state;
	TextView status;
	TextView error;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		getSupportActionBar().setTitle("User Preferences");
		getSupportActionBar().setSubtitle("by kiddoBLOOM");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			
		sp = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
		
		Button okb = (Button) findViewById(R.id.prefButtonOK);
		okb.setOnClickListener(this);
		
		Button cancelb = (Button) findViewById(R.id.prefButtonCancel);
		cancelb.setOnClickListener(this);

		registered = (CheckBox) findViewById(R.id.pref_cb_reg);
		registered.setOnClickListener(this);
		
		skip = (CheckBox) findViewById(R.id.pref_cb_fb);
		skip.setOnClickListener(this);

		synced = (CheckBox) findViewById(R.id.pref_cb_initial_sync);
		synced.setOnClickListener(this);
		
		tv = (TextView) findViewById(R.id.pref_tv_userid);
		state = (TextView) findViewById(R.id.pref_tv_state);
		status = (TextView) findViewById(R.id.pref_tv_status);
		error = (TextView) findViewById(R.id.pref_tv_error);

		updateUI();
	
	}
	
	private void updateUI() {

		boolean b2 = sp.getBoolean(getString(R.string.pref_userid_registered_key), false);
		registered.setChecked(b2);
		
		String text = sp.getString(getString(R.string.pref_fb_userid_key), null);
		tv.setText("facebook id: " + text);
		
		boolean b3 = sp.getBoolean(getString(R.string.pref_skip_key), false);
		skip.setChecked(b3);
		
		boolean b4 = sp.getBoolean(getString(R.string.pref_initial_synced_key), false);
		synced.setChecked(b3);
		
		int text1 = sp.getInt(getString(R.string.pref_state_key), 100);
		state.setText("state: " + StateMachine.stateStr[text1]);
		
		int text2 = sp.getInt(getString(R.string.pref_status_key), 100);
		status.setText("status: " + StateMachine.statusStr[text2]);
		
		int text3 = sp.getInt(getString(R.string.pref_error_key), 100);
		error.setText("error: " + StateMachine.errorStr[text3]);

	}
	
	public void savePreferences() {
		
		SharedPreferences.Editor editor = sp.edit();
		
		editor.putBoolean(getString(R.string.pref_userid_registered_key), registered.isChecked());
		editor.putBoolean(getString(R.string.pref_skip_key), skip.isChecked());
		editor.putString(getString(R.string.pref_fb_userid_key), tv.getText().toString());
		editor.putBoolean(getString(R.string.pref_initial_synced_key), synced.isChecked());
		
//		editor.putString(getString(R.string.pref_state_key), state.getText().toString());
//		editor.putString(getString(R.string.pref_status_key), status.getText().toString());
//		editor.putString(getString(R.string.pref_error_key), error.getText().toString());

		editor.commit();

	}

	@Override
	public void onClick(View v) {

		Log.d("tag", "button is pressed");

		if (v.getId() == R.id.prefButtonOK) {
			savePreferences();
			PreferencesActivity.this.setResult(RESULT_OK);
			finish();
		} else if (v.getId() == R.id.prefButtonCancel) {
			Log.d("tag", "cancel is pressed");
			PreferencesActivity.this.setResult(RESULT_CANCELED);
			finish();
		} else if (v.getId() == R.id.pref_cb_fb) {
			CheckBox cb = (CheckBox) v;
		} else if (v.getId() == R.id.pref_cb_reg) {
			CheckBox cb = (CheckBox) v;
		}
	}
	
}
