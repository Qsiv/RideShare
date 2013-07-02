package com.example.rideshare;

import static com.example.rideshare.CommonUtilities.PROJECT_NUMBER;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;


public class Notifications extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(R.string.notifications);
		setContentView(R.layout.activity_notifications);
	}
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		//Intent intent = new Intent(this, Dashboard.class);
        //startActivity(intent);
        finish();
	}
}