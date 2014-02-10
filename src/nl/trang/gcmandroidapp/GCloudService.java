package nl.trang.gcmandroidapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This class handles the process of registrations with Google and
 * a third party web app.
 * 
 * @author trang
 *
 */
public class GCloudService {

	private static GCloudService mInstance;
	
	protected Activity mActivity;
	
	protected SharedPreferences mPrefs;
	protected Context mContext;
	protected String TAG = "TEST";
	
	// Google Cloud
	protected GoogleCloudMessaging mGCM;	
	protected String mGoogleProjectNumber;
	protected String mRegId; // ID received from Google after subscribing
	
	// ThirdParty
	protected String mThirdPartyAppId;
	protected String mThirdPartyUrl;
	protected String mThirdPartyTaskRegister = "/gcm/registerdevice";
	protected String mThirdPartyTaskUnRegister = "/gcm/unregisterdevice";
	protected String mThirdPartyTaskReActivate = "/gcm/reactivatedevice";
	
	private final static int RESOLUTION_REQUEST = 9000;
	
	protected OnRegisterEventListener mOnRegEvListener;
	protected OnUnRegisterEventListener mOnUnRegEvListener;
	
	/**
	 * Constructor
	 * 
	 * @param activity
	 */
	protected GCloudService(Activity activity) {
		
		mActivity = activity;
		mContext = mActivity.getApplication();		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
	}

	/**
	 * This class handles the task of unregistering the device
	 * @author trang
	 *
	 */
	public class ThirdPartyTask extends AsyncTask<String, Integer, Integer> {

		public ThirdPartyTask() {}

        @Override
        protected Integer doInBackground(String... inputs) {

            Map<String, String> params = new HashMap<String, String>();
        	boolean isTriggerRegister = true;
            String url = mThirdPartyUrl + mThirdPartyTaskRegister;
            
        	for(int i = 0; i < inputs.length; i++) {
        		
        		if(i == 0) {
        			params.put("regid", inputs[i]);
        		}
        		else if(i == 1) {
        			params.put("appid", inputs[i]);
        		}
        		else if(i == 2) {
        			params.put("task", inputs[i]);
        			url = mThirdPartyUrl + inputs[i];
        		}
        		else if(i == 3 && inputs[i].equals("false")) {
        			isTriggerRegister = false;
        		}
        	}
            
            try {
            	
            	if(HttpClient.PostToServer(url, params)) {
            		
            		if(isTriggerRegister) {
            			triggerRegisterEvent();
            		}
            		else {
            			triggerUnRegisterEvent();
            		}
            	}
            }
            catch(IOException e){
            	
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
	
	public void unRegisterDevice() {
		
		new ThirdPartyTask().execute(mRegId, mThirdPartyAppId, mThirdPartyTaskUnRegister, "false");
	}
	
	protected void triggerRegisterEvent() {
		
		if(mOnRegEvListener != null) {
			mOnRegEvListener.OnRegister();
		}		
	}
	
	protected void triggerUnRegisterEvent() {
		
		if(mOnUnRegEvListener != null) {
			mOnUnRegEvListener.OnUnRegister();
		}		
	}
	
	public void setOnRegisterEventListener(OnRegisterEventListener evListener) {
		
		this.mOnRegEvListener = evListener;
	}
	
	public void setOnUnRegisterEventListener(OnUnRegisterEventListener evListener) {
		
		this.mOnUnRegEvListener = evListener;
	}
	
	/**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        
    	int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        
        if (resultCode != ConnectionResult.SUCCESS) {
        	
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                
            	GooglePlayServicesUtil.getErrorDialog(
            										resultCode, 
            										mActivity,
            										RESOLUTION_REQUEST
            										).show();
            } 
            else {
                
            	// Device Not supported
                mActivity.finish();
            }
            return false;
        }
        return true;
    }
	
	public String getGoogleProjectNumber() {
		return mGoogleProjectNumber;
	}
	
	public static GCloudService getInstance(Activity activity) {
		
		if(mInstance == null) {
			mInstance = new GCloudService(activity);
		}
		return mInstance;
	}

	public static GCloudService getInstance() {
		
		if(mInstance != null) {
			return mInstance;
		}
		return null;
	}
	
	public String getRegistrationId() {
		
		String regId = mPrefs.getString(Preferences.PREF_REG_ID, "");
		
		if(regId.isEmpty()) {
			return "";
		}
		
        int regVersion = mPrefs.getInt(Preferences.PREF_APP_VERSION, Integer.MIN_VALUE);
        int curVersion = getAppVersion(mContext);
        if (regVersion != curVersion) {
            return "";
        }
		return regId;
	}

	public String getThirdPartyAppId() {
		return mThirdPartyAppId;
	}

	public String getThirdPartyUrl() {
		return mThirdPartyUrl;
	}
	
    /**
	 * Initialize the service
	 */
	public void initialize() {
		
		boolean isEnableNotify = mPrefs.getBoolean("notify_new_messages", true);
		
		if (checkPlayServices() && isEnableNotify 
				&& ! mThirdPartyAppId.isEmpty() 
				&& ! mThirdPartyTaskRegister.isEmpty()) {
			
			Log.i(TAG, "initialize: isEnableNotify true");
			
			mGCM = GoogleCloudMessaging.getInstance(mActivity);
			mRegId = getRegistrationId();
			
			if(mRegId.isEmpty()) {
				
				registerDevice();
			}
		}
		else {
			
			Log.i(TAG, "initialize: isEnableNotify false");
		}
	}	

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */    
    private void registerDevice() {
        new AsyncTask<Void, Void, String>() {
        	
            @Override
            protected String doInBackground(Void... params) {
                
            	String msg = "";
                try {
                    if (mGCM == null) {
                    	mGCM = GoogleCloudMessaging.getInstance(mContext);
                    }

                    mRegId = mGCM.register(mGoogleProjectNumber);
                    
                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, mRegId);
                    
                    // Execute ThirdParty tasks
                    new ThirdPartyTask().execute(mRegId, mThirdPartyAppId, mThirdPartyTaskRegister, "true");

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	
            }
        }.execute(null, null, null);
    }    
    
    public void reActivateDevice() {
    	
    	new ThirdPartyTask().execute(mRegId, mThirdPartyAppId, mThirdPartyTaskReActivate, "true");
    }
    
	public void setGoogleProjectNumber(String projectNumber) {
		this.mGoogleProjectNumber = projectNumber;
	}
	
	public void setThirdPartyAppId(String appId) {
		this.mThirdPartyAppId = appId;
	}

	public void setThirdPartyUrl(String urlThirdParty) {
		
		this.mThirdPartyUrl = urlThirdParty;
	}
	
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        
        int appVersion = getAppVersion(mContext);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Preferences.PREF_REG_ID, regId);
        editor.putInt(Preferences.PREF_APP_VERSION, appVersion);
        editor.commit();
    }
    
}
