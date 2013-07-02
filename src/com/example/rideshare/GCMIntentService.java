package com.example.rideshare;

import java.io.IOException;
import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import static com.example.rideshare.CommonUtilities.API_KEY;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.PROJECT_NUMBER;
//import static com.example.rideshare.CommonUtilities.generateNotification;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;

import com.example.rideshare.deviceinfoendpoint.Deviceinfoendpoint;
import com.example.rideshare.deviceinfoendpoint.model.DeviceInfo;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;


/**
 * This class is started up as a service of the Android application. It listens
 * for Google Cloud Messaging (GCM) messages directed to this device.
 * 
 * When the device is successfully registered for GCM, a message is sent to the
 * App Engine backend via Cloud Endpoints, indicating that it wants to receive
 * broadcast messages from the it.
 * 
 * Before registering for GCM, you have to create a project in Google's Cloud
 * Console (https://code.google.com/apis/console). In this project, you'll have
 * to enable the "Google Cloud Messaging for Android" Service.
 * 
 * Once you have set up a project and enabled GCM, you'll have to set the
 * PROJECT_NUMBER field to the project number mentioned in the "Overview" page.
 * 
 * See the documentation at
 * http://developers.google.com/eclipse/docs/cloud_endpoints for more
 * information.
 */
public class GCMIntentService extends GCMBaseIntentService {
  //private final Deviceinfoendpoint endpoint;

  /*
   * TODO: Set this to a valid project number. See
   * http://developers.google.com/eclipse/docs/cloud_endpoint for more
   * information.
   */

  /**
   * Register the device for GCM.
   * 
   * @param mContext
   *            the activity's context.
   */
  private static final String TAG = "GCMIntentService";

  public GCMIntentService() {
      super(PROJECT_NUMBER);
  }
  AsyncTask<Void, Void, Void> mRegisterTask;
  @Override
  protected void onRegistered(Context mContext, final String reg_id) {
      Log.i(TAG, "Device registered: regId = " + reg_id);
      //Send reg_id to server
      Toast.makeText(getApplicationContext(), "Registering GCM ID on Server", Toast.LENGTH_SHORT).show();
      final Context context = this;
      mRegisterTask = new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... params) {
              ServerUtilities.register(context, reg_id);
              return null;
          }

          @Override
          protected void onPostExecute(Void result) {
              mRegisterTask = null;
          }

      };
      mRegisterTask.execute(null, null, null);
  }

  @Override
  protected void onUnregistered(Context mContext, final String reg_id) {
      Log.i(TAG, "Device unregistered");
      //Tell server to delete current reg_id for user
      final Context context = this;
      mRegisterTask = new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... params) {
              ServerUtilities.unregister(context, reg_id);
              return null;
          }

          @Override
          protected void onPostExecute(Void result) {
              mRegisterTask = null;
          }

      };
      mRegisterTask.execute(null, null, null);
      //ServerUtilities.unregister(mContext, reg_id);
  }

//JOINED_BY_RSU_NOTIFICATION = 'RSU_joined'
//	-rsu_user, rsu_email, rsu_phone, trip_id
//PAYMENT_RECEIVED_FROM_ROU = 'ROU_paid'
//	-rsu_user, rsu_email, trip_id
//ACCEPT_BY_ROU_NOTIFICATION = 'accepted_by_ROU'
//	-rou_user, rou_email, rou_phone, trip_id
//REJECT_BY_ROU_NOTIFICATION = 'rejected_by_ROU' 	
//	-rou_user, trip_id
//CANCEL_BY_ROU_NOTIFICATION = 'canceled_by_ROU' 	
//	-rou_user, rou_email, trip_id
//INVITE_BY_ROU_NOTIFICATION = 'ROU_invite'
//	-rou_user, rou_email, rou_phone, trip_id
  @Override
  protected void onMessage(Context mContext, Intent intent) {
	Log.i(TAG, "Received message. Extras: " + intent.getExtras());
	//Guaranteed to be given in the message
	String notifi_type = intent.getExtras().getString("notification_type");
	String from_user = intent.getExtras().getString("user");
	String tripID = intent.getExtras().getString("trip_id");
	//------
	String from_email = "", from_phone = "";
	if(!notifi_type.equalsIgnoreCase("rejected_by_ROU")){
		from_email = intent.getExtras().getString("email");
	}
	if(notifi_type.equalsIgnoreCase("RSU_joined") || 
		notifi_type.equalsIgnoreCase("accepted_by_ROU") || notifi_type.equalsIgnoreCase("ROU_invite")){
		from_phone = intent.getExtras().getString("phoneNumber");
	}
	//accepted_by_ROU, rejected_by_ROU, canceled_by_ROU, RSU_joined, ROU_invite
	  																		
	if (notifi_type.equalsIgnoreCase("ROU_invite") || 
		notifi_type.equalsIgnoreCase("accepted_by_ROU") || 
		notifi_type.equalsIgnoreCase("RSU_joined") || 
		notifi_type.equalsIgnoreCase("canceled_by_ROU"))
	{
		//if(mContext == null) Log.e("GCMIntentService","mContext is null");
		//mContext.startActivity(new Intent(mContext, MyTrips.class));
		MyTrips.setNotificationType(new StringBuilder(notifi_type), 
									new StringBuilder(from_user), 
									new StringBuilder(from_email), 
									new StringBuilder(from_phone), 
									new StringBuilder(tripID));
	}
	  	  
	//Toast.makeText(getApplicationContext(), "onMessage: Received GCM Message from "+from_user+" ("+from_email+")", Toast.LENGTH_SHORT).show();
	// notifies user
	new CommonUtilities().generateNotification(mContext, notifi_type, from_user, tripID);
  }
 
  
  private final void handleMessage(Intent intent){
	String from_user = intent.getExtras().getString("user");
	String from_email = intent.getExtras().getString("email");
	Toast.makeText(getApplicationContext(), "handleMessage: Received GCM Message from "+from_user+" ("+from_email+")", Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onDeletedMessages(Context mContext, int total) {
	Log.i(TAG, "Received deleted messages notification");
	//String message = getString(R.string.gcm_deleted, total);
	String message = "Delete Message Received";
	// notifies user
 	//new CommonUtilities().generateNotification(mContext,message);
  }

  @Override
  public void onError(Context mContext, String errorId) {
	Log.i(TAG, "Received error: " + errorId);
  }

  @Override
  protected boolean onRecoverableError(Context mContext, String errorId) {
	// log message
	Log.i(TAG, "Received recoverable error: " + errorId);
	return super.onRecoverableError(mContext, errorId);
  }

  /**
   * Issues a notification to inform the user that server has sent a message.
   */
}
