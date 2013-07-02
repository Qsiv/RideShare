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

import com.example.rideshare.Registration.UserRegistration;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;
import android.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Request extends Activity 
{
	//public static final String SERVER_URL = "http://10.0.2.2:8080/";
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("Request A Ride");
        setContentView(R.layout.new_ride_req);
    }
	
	public void postClickR(View v)
	{
		if(!isNetworkAvailable(this)){
    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		return;
    	}
		EditText loc = (EditText) findViewById(R.id.current_location_editText);
		String location = loc.getText().toString().trim();
		//Log.i("Request","Language = "+Locale.getDefault().getDisplayLanguage().equals("English") + " ( "+Locale.getDefault().getDisplayLanguage());
		if(location.length() == 0){
			Toast.makeText(getApplicationContext(), R.string.current_location, Toast.LENGTH_SHORT).show();
			loc.setText("");
			return;
		}
		if(Locale.getDefault().getDisplayLanguage().equals("English"))
			location = location.toLowerCase(Locale.ENGLISH);
		
		EditText des = (EditText) findViewById(R.id.destination_editText);
		String destination = des.getText().toString().trim();
		if(destination.length() == 0){
			Toast.makeText(getApplicationContext(), R.string.destination, Toast.LENGTH_SHORT).show();
			des.setText("");
			return;
		}
		if(Locale.getDefault().getDisplayLanguage().equals("English"))
			destination = destination.toLowerCase(Locale.ENGLISH);
		
		EditText date = (EditText) findViewById(R.id.date_editText);
		String travelDate = date.getText().toString().trim();
		
		EditText time = (EditText) findViewById(R.id.time_pickup_editText);
		String travelTime = time.getText().toString().trim();
		
		//get date
		String [] parsed_Date = travelDate.split("/");
		if(parsed_Date.length != 3){
			Toast.makeText(getApplicationContext(), R.string.date_error, Toast.LENGTH_SHORT).show();
			date.setText("");
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
		String [] parsed_Time = travelTime.split(":");
		if(parsed_Time.length != 2){
			Toast.makeText(getApplicationContext(), R.string.time_error, Toast.LENGTH_SHORT).show();
			time.setText("");
			return;
		}
		String hours = 	parsed_Time[0];
		String mins  = 	parsed_Time[1];
				
		RequestRide req = new RequestRide();
		//String result = req.doInBackground(location,destination,month,day,year,hours,mins);
		String server_response = "init";
		req.execute(location,destination,month,day,year,hours,mins, MainActivity.activeUser.name,MainActivity.activeUser.email);
    	try{
    		server_response = req.get(10000, TimeUnit.MILLISECONDS);
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
	
	
	
	class RequestRide extends AsyncTask<String, Void, String> {
		 
	    @Override
	    protected String doInBackground(String... params) {
	        String uri = SERVER_URL + "/requestRide";//"http://10.0.2.2:8080/requestRide";
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
