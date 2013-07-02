package com.example.rideshare;

import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.TAG;

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpPost;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     *
     */
    static void register(final Context context, final String reg_id) {
        Log.i(TAG, "registering device (regId = " + reg_id + ")");
        String serverUrl = SERVER_URL + "/GCMregister";
        serverUrl += "?";
        List<NameValuePair> data = new LinkedList<NameValuePair>();
        data.add(new BasicNameValuePair("email",MainActivity.activeUser.email));
        data.add(new BasicNameValuePair("registration_id", reg_id));
        String paramString = URLEncodedUtils.format(data, "utf-8");
        serverUrl += paramString;
        
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                String response = post(serverUrl);
                if(response.equals("registered"))
                	GCMRegistrar.setRegisteredOnServer(context, true);
                else
                	Log.e(TAG, "registering device failed: "+response);
                return;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String reg_id) {
        Log.i(TAG, "unregistering device (regId = " + reg_id + ")");
        String serverUrl = SERVER_URL + "/GCMUnregister";
        serverUrl += "?";
        List<NameValuePair> data = new LinkedList<NameValuePair>();
        data.add(new BasicNameValuePair("email", MainActivity.activeUser.email));
        String paramString = URLEncodedUtils.format(data, "utf-8");
        serverUrl += paramString;
        //params.put("registration_id", reg_id);
        try {
            String response = post(serverUrl);
            if(response.equals("unregistered")) 
            	GCMRegistrar.setRegisteredOnServer(context, false);
            else
            	Log.e(TAG, "unregistering device Unsuccessful: "+response);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
        	Log.i(TAG,"Unregistered from GCM, but still registered on server");
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static String post(String endpoint) throws IOException {
    	String uri = endpoint;
        
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);
         
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
             
            if(entity != null) 
                return EntityUtils.toString(entity);
        
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (ClientProtocolException cpe) {
            cpe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
     
        return null;
      }
}