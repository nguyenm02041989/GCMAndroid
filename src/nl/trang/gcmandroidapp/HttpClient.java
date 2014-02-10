package nl.trang.gcmandroidapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class HttpClient {

	private HttpClient() {
		
	}
	
    public static boolean PostToServer(String endpoint, Map<String, String> params)
            throws IOException {

        URL url;
        try {
            url = new URL(endpoint);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            java.util.Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        String body = bodyBuilder.toString();

        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        
        try {

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            int status = conn.getResponseCode();
            
            // If response is not success
            if (status != 200) {

                throw new IOException("Post failed with error code " + status);
            }
            
            //Get Response	
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            
            while((line = rd.readLine()) != null) {
              response.append(line);
              response.append('\r');
            }
            rd.close();                
            
            try {
            
            	JSONObject jObject = new JSONObject(response.toString());
            	JSONObject jObjectRes = jObject.getJSONObject("response");
            	
            	if(jObjectRes.get("code").toString().equals("100")) {
            		
            		// The registration has been succesful
            		return true;
            	}
            }
            catch(JSONException eJ) {
            	
            	Log.i("TEST", "JSONException: " + eJ.getMessage());
            	return false;
            }
            
        } finally {
        	
            if (conn != null) {
                conn.disconnect();
            }
        }
        return false;
    }	
}
