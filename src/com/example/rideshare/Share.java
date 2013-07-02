package com.example.rideshare;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Share extends Activity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("Share A Trip");
        setContentView(R.layout.new_ride_offer);
    }
	
	public void postClickO (View v)
	{
		//insert code to handle post
		Toast.makeText(getApplicationContext(), "Trip posted succesfully", Toast.LENGTH_SHORT).show();
		finish();
	}
}
