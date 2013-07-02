package com.example.rideshare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.content.BroadcastReceiver;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {
    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    //static final String SERVER_URL = "http://10.0.2.2:8080/";
	static final String SERVER_URL = "http://ucdrideshare.appspot.com";
    /**
     * Google API project id registered to use GCM.
     */
    static final String API_KEY = "AIzaSyAfWnSGUuEqr8V9kQEw5SgyxOej36REwcE";
    /**
     * Google Project Number
     */
    static final String PROJECT_NUMBER = "241449802582";
    /**
     * Tag used on log messages.
     */
    static final String TAG = "CommonUtilities";

    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_MESSAGE_ACTION =
            "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";
    static final String VERSION = "v0.4";
    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    static boolean isNetworkAvailable(Context context){
    	ConnectivityManager connectMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetInfo = connectMan.getActiveNetworkInfo();
    	return activeNetInfo != null && activeNetInfo.isConnected();
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
    public void generateNotification(Context mContext, String message, String from_user, String tripID) {
		Intent intent = new Intent(mContext, Dashboard.class);
	  	PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
	  	String title = "", subject = "";
	  	if(message.equalsIgnoreCase("RSU_joined"))
	  	{ title = "User Joined Trip"; subject = from_user+" has joined one of your trips"; }
	  	else if(message.equalsIgnoreCase("ROU_paid")) 
	  	{ title = "User Payment Received"; subject = from_user+" has paid"; }
	  	else if(message.equalsIgnoreCase("accepted_by_ROU"))
	  	{ 
	  		Log.i("CommonUtilities","Send Notification Action to Payment");
	  		title = "Join Request Accepted"; 
	  		subject = from_user+" has accepted your request to join"; 
	  		intent = new Intent(mContext, Payment.class);
	  		intent.putExtra("trip_id",tripID);
	  		pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
	  	} //myIntent.getStringExtra("trip_id");
	  	else if(message.equalsIgnoreCase("rejected_by_ROU"))
	  	{ title = "Join Request Rejected"; subject = from_user+" has rejected your request to join"; }
	  	else if(message.equalsIgnoreCase("canceled_by_ROU"))
	  	{ title = "Trip Canceled"; subject = from_user+" has canceled the trip"; }
	  	else if(message.equalsIgnoreCase("ROU_invite"))
	  	{ title = "Trip Invite Received"; subject = from_user+" has invited you to join their trip"; 
  		intent = new Intent(mContext, Payment.class);
  		intent.putExtra("trip_id",tripID);
  		pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);}
	  	else{
	  		Log.e("generateNotification","Unknown Notification Type: "+message);
	  		return; //error, unknown message
	  	}
	  	// Build notification
	  	// Actions are just fake
	  	Notification noti = new Notification.Builder(mContext)
	  		.setContentTitle(title)
	  		.setContentText(subject)
	  		.setSmallIcon(R.drawable.ic_notif)
	  		.setContentIntent(pIntent).getNotification();
          //.addAction(R.drawable.icon, "Call", pIntent)
          //.addAction(R.drawable.icon, "More", pIntent)
          //.addAction(R.drawable.icon, "And more", pIntent).build();
    
	  	NotificationManager notificationManager = 
	  			(NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

	  	// Hide the notification after its selected
	  	noti.flags |= Notification.FLAG_AUTO_CANCEL;

	  	notificationManager.notify(0, noti); 
	}
}