package nl.trang.gcmandroidapp;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity {

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// There is a better way to do this! Use fragments instead.
		addPreferencesFromResource(R.xml.preferences);
		
		final CheckBoxPreference chPref = (CheckBoxPreference) findPreference("notify_new_messages"); 
		chPref.setOnPreferenceChangeListener(onPrefChange);
	}
	
	private OnPreferenceChangeListener onPrefChange = new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			
			boolean cState = (newValue.toString().equals("true") ? true : false);
			GCloudService gService = GCloudService.getInstance();
			
			if(cState) {
				
				gService.reActivateDevice();
			}
			else {
				
				// User disabled notifications
				
				gService.unRegisterDevice();
				
			}
			return true;
		}
	};

	
}
