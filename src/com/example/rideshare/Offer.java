package com.example.rideshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Offer extends Activity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle(R.string.new_ride_offer);
        setContentView(R.layout.new_ride_offer);
    }
	
	public void postClickO(View v)
	{
		if(!isNetworkAvailable(this)){
    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		return;
    	}
		EditText loc = (EditText) findViewById(R.id.location_ET);
		String location = loc.getText().toString().trim();
		if(location.length() == 0){
			Toast.makeText(getApplicationContext(), R.string.current_location, Toast.LENGTH_SHORT).show();
			loc.setText("");
			return;
		}
		if(Locale.getDefault().getDisplayLanguage().equals("English"))
			location = location.toLowerCase(Locale.ENGLISH);
		
		EditText des = (EditText) findViewById(R.id.destination_ET);
		String destination = des.getText().toString().trim();
		if(destination.length() == 0){
			Toast.makeText(getApplicationContext(), R.string.destination, Toast.LENGTH_SHORT).show();
			des.setText("");
			return;
		}
		if(Locale.getDefault().getDisplayLanguage().equals("English"))
			destination = destination.toLowerCase(Locale.ENGLISH);
		
		EditText trDate = (EditText) findViewById(R.id.date_trip_ET);
		String travDate = trDate.getText().toString().trim();

		
		EditText trTime = (EditText) findViewById(R.id.time_trip_ET);
		String travTime = trTime.getText().toString().trim();
		
		EditText seats_ET = (EditText) findViewById(R.id.available_seats_ET);
		String seats = seats_ET.getText().toString().trim();
		
		EditText cost_ET = (EditText) findViewById(R.id.seat_cost_ET);
		String seat_cost = cost_ET.getText().toString().trim();

		//get date
		String [] parsed_Date = travDate.split("/");
		if(parsed_Date.length != 3){
			Toast.makeText(getApplicationContext(), R.string.date_error, Toast.LENGTH_SHORT).show();
			trDate.setText("");
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);
		
		try {
			cal.set(Calendar.MONTH, Integer.parseInt(parsed_Date[0]) - 1);
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parsed_Date[1]));
			cal.set(Calendar.YEAR, Integer.parseInt(parsed_Date[2]));
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Enter a valid date", Toast.LENGTH_SHORT).show();
		}
		
		String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG,Locale.US);
		String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		String year = Integer.toString(cal.get(Calendar.YEAR));
		if(year.length() == 2) year = "20" + year;
		
		//get time
		String [] parsed_Time = travTime.split(":");
		if(parsed_Time.length != 2){
			Toast.makeText(getApplicationContext(), R.string.time_error, Toast.LENGTH_SHORT).show();
			trTime.setText("");
			return;
		}
		String hours = 	parsed_Time[0];
		String mins  = 	parsed_Time[1];
				
		OfferRide offer = new OfferRide();
		//String result = req.doInBackground(location,destination,month,day,year,hours,mins);
		String server_response = "init";
		offer.execute(location,destination,month,day,year,hours,mins, MainActivity.activeUser.name,MainActivity.activeUser.email, seats, seat_cost);
    	try{
    		server_response = offer.get(10000, TimeUnit.MILLISECONDS);
    	}
    	catch(ExecutionException e){
    		Toast.makeText(getApplicationContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
    	    e.printStackTrace();
    	}
    	catch(InterruptedException e){
    		Toast.makeText(getApplicationContext(), R.string.interrupted, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
    	catch(CancellationException e){
    		Toast.makeText(getApplicationContext(), R.string.cancelled, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
    	catch(TimeoutException e){
    		Toast.makeText(getApplicationContext(), R.string.connection_timed_out, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
		//if (server_response.equals("okay"))
		//	Toast.makeText(getApplicationContext(), "Request posted succesfully", Toast.LENGTH_SHORT).show();
		//else
		//	Toast.makeText(getApplicationContext(), "Error: Request not posted", Toast.LENGTH_SHORT).show();
		 
			 
		//go to dashboard if ride posts...or not..maybe only make it go back if ride posts
		Intent intent = new Intent(this, Dashboard.class);
		startActivity(intent);
		finish();
	}
	
	
	
	class OfferRide extends AsyncTask<String, Void, String> {
		 
	    @Override
	    protected String doInBackground(String... params) {
	        String uri = SERVER_URL + "/offerRide";
	        uri += "?";
	        
	        List<NameValuePair> data = new LinkedList<NameValuePair>();
	        data.add(new BasicNameValuePair("location", params[0]));
	        data.add(new BasicNameValuePair("destination", params[1]));
	        data.add(new BasicNameValuePair("month", params[2]));
	        data.add(new BasicNameValuePair("day", params[3]));
	        data.add(new BasicNameValuePair("year", params[4]));
	        data.add(new BasicNameValuePair("hour", params[5]));
	        data.add(new BasicNameValuePair("minute", params[6]));
	        data.add(new BasicNameValuePair("name", params[7]));
	        data.add(new BasicNameValuePair("email", params[8]));
	        data.add(new BasicNameValuePair("seats", params[9]));
	        data.add(new BasicNameValuePair("seat_cost", params[10]));
	        String paramString = URLEncodedUtils.format(data, "utf-8");

	        uri += paramString;
	        
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
	@Override
	public void onBackPressed()
	{
		//super.onBackPressed();
		Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
	}
	
}
