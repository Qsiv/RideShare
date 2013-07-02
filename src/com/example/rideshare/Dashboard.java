package com.example.rideshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import static com.example.rideshare.CommonUtilities.API_KEY;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.PROJECT_NUMBER;
import com.google.android.gcm.GCMRegistrar;

import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;

import android.content.BroadcastReceiver;

import android.content.IntentFilter;
import android.os.AsyncTask;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Dashboard extends Activity 
{	
	AsyncTask<Void, Void, Void> mRegisterTask;
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("Dashboard");
        setContentView(R.layout.dashboard);
        //-----------------------------------------------------
        //new CommonUtilities().generateNotification(this,"Testing Notifications");
        //-----------------------------------------------------

        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
        
        final String reg_id = GCMRegistrar.getRegistrationId(this);
        if (reg_id.equals("")) {
            // Automatically registers application on startup.
        	Toast.makeText(getApplicationContext(), "Registering Device with GCM", Toast.LENGTH_SHORT).show();
            GCMRegistrar.register(this, PROJECT_NUMBER);
        } else {
            // Device is already registered on GCM, check server.
            //if (GCMRegistrar.isRegisteredOnServer(this)) {
                // Skips registration.
            //	Toast.makeText(getApplicationContext(), "Device Already Registered with GCM and Server", Toast.LENGTH_SHORT).show();
            //} else {
            	// Re-register with the Server
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
            	//Toast.makeText(getApplicationContext(), "Registering GCM ID with Server", Toast.LENGTH_SHORT).show();
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
        //}
    }
	public void profileClick (View v)
	{
		Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
        //finish();
	}
	
	public void searchClick (View v)
	{
		Intent intent = new Intent(this, Search.class);
        startActivity(intent);
        finish();
	}
	
	public void requestClick (View v)
	{
		Intent intent = new Intent(this, Request.class);
        startActivity(intent);
        finish();
	}
	
	public void shareClick (View v)
	{
		Intent intent = new Intent(this, Offer.class);
        startActivity(intent);
        finish();
	}
	public void notifClick (View v)
	{
		Intent intent = new Intent(this, Notifications.class);
        startActivity(intent);
        //finish();
	}
	
	public void tripClick (View v)
	{
		Intent intent = new Intent(this, MyTrips.class);
        startActivity(intent);
        //finish();
	}
	
	@Override
	public void onBackPressed()
	{
		final Context ctx = this;
		AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(ctx);
		exitDialogBuilder.setTitle("Back button pressed");

		exitDialogBuilder.setMessage("Would you like to exit?").setCancelable(true).setPositiveButton("Yes",new DialogInterface.OnClickListener()
		{ public void onClick (DialogInterface dialog, int id)
		{
			Dashboard.this.finish();
		}}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick (DialogInterface dialog, int id)
			{
				dialog.cancel();
			}});
		exitDialogBuilder.show();
		
	}
	
}
