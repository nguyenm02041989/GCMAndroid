package nl.trang.gcmandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	SharedPreferences mPrefs;
	Context mContext;
	String TAG = "TEST";
	
	// You always need an instance, because a user can 
	// disable notitfactions at any time.
	GCloudService gService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = getApplicationContext();
		
		gService = GCloudService.getInstance(this);
		gService.setGoogleProjectNumber("<YOUR PROJECT NUMBER>");
		gService.setThirdPartyAppId("<A Third Party App ID>");
		gService.setThirdPartyUrl("<THIRD PARTY DOMAIN>");
		
		// Here you can register for the (un)register event. 
		gService.setOnRegisterEventListener(new OnRegisterEventListener() {
			
			@Override
			public void OnRegister() {
				Log.i(TAG, "You have succesfully registered for the Google Cloud Messaging service");
			}
		});
		
		gService.setOnUnRegisterEventListener(new OnUnRegisterEventListener() {
			
			@Override
			public void OnUnRegister() {
				Log.i(TAG, "You have succesfully unregistered for the Google Cloud Messaging service");
			}
		});
		
		
		gService.initialize();
		
		// Check if you have some new messages
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		
		if(sharedPrefs.getBoolean(Preferences.PREF_ENABLE_NOTIFY, false)) {
			
			TextView txtVw = (TextView) findViewById(R.id.textViewMessage);
			txtVw.setText(sharedPrefs.getString(Preferences.PREF_NOTIFY_MESSAGE, ""));
			
			// Remove the message from the preferences.
	        SharedPreferences.Editor editor = sharedPrefs.edit();
	        editor.putString(Preferences.PREF_NOTIFY_MESSAGE, "");
	        editor.commit();			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
