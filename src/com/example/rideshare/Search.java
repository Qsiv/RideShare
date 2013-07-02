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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import android.util.Log;

import com.example.rideshare.Request.RequestRide;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;
import static com.example.rideshare.CommonUtilities.VERSION;

//import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class Search extends Activity 
{
	boolean offer_request; //true = offer and false = request
	//public static final String SERVER_URL = "http://10.0.2.2:8080/";
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("Search");
        setContentView(R.layout.ride_search);
	}
        
	public void searchClick (View v)
    {
		if(!isNetworkAvailable(this)){
    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		return;
    	}
		EditText loc = (EditText) findViewById(R.id.cur_location_editText);
		String location = loc.getText().toString().trim();
		if(location.length() == 0){
			Toast.makeText(getApplicationContext(), R.string.current_location, Toast.LENGTH_SHORT).show();
			loc.setText("");
			return;
		}
		if(Locale.getDefault().getDisplayLanguage().equals("English"))
			location = location.toLowerCase(Locale.ENGLISH);
		
		EditText des = (EditText) findViewById(R.id.dest_editText);
		String destination = des.getText().toString().trim();
		if(destination.length() == 0){
			Toast.makeText(getApplicationContext(), R.string.destination, Toast.LENGTH_SHORT).show();
			des.setText("");
			return;
		}
		if(Locale.getDefault().getDisplayLanguage().equals("English"))
			destination = destination.toLowerCase(Locale.ENGLISH);
		
		EditText date = (EditText) findViewById(R.id.date_ET);
		String travelDate = date.getText().toString().trim();
		
		RadioButton rb = (RadioButton) findViewById(R.id.radio_offer);	
		if (rb.isChecked())
			offer_request = true;
		
			rb = (RadioButton) findViewById(R.id.radio_request);
		if (rb.isChecked())
			offer_request = false;

		RadioButton departure_RB = (RadioButton) findViewById(R.id.radio_departure);
		boolean departChecked = departure_RB.isChecked();
		
		RadioButton rating_RB = (RadioButton) findViewById(R.id.radio_rating);
		boolean ratingChecked = rating_RB.isChecked();
		
		String orderBy="";
		if (departChecked)
			orderBy+="departure";
		else if (ratingChecked)
			orderBy+="rating";
			
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
					
		
		
		//do server-side stuff
		SearchUser su = new SearchUser();
		//String result = su.doInBackground(location,destination,month,day,year,hours_f,mins_f,hours_t,mins_t);
		String server_response = "init";
		su.execute(location,destination,month,day,year,orderBy);
    	try{
    		server_response = su.get(10000, TimeUnit.MILLISECONDS);
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
    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    	}
		
		Intent intent = new Intent(this, SearchResults.class);
        startActivity(intent);
		
        //pass results to SearchResults activity class before finishing this activity
        if(server_response.equals("init")){
        	Toast.makeText(getApplicationContext(), "Error on Posting", Toast.LENGTH_SHORT).show();
        	return;
        }
        SearchResults.setResults(server_response);
        
    	finish();
    }
	
	
	class SearchUser extends AsyncTask<String, Void, String> {
		 
	    @Override
	    protected String doInBackground(String... params) {
	        //handle one way if searching for driver and another way if searcher for rider
	    	//if looking for driver search other database and to field is not relevant
	    	
	    	String uri = "";
	    	
	    	if (!offer_request) //request radio button checked
	    	{
	    		uri += (SERVER_URL+"/searchRider");//"http://10.0.2.2:8080/searchRider";
	    	} 
	    	else if (offer_request) //offer radio button checked
	    	{
	    		uri += (SERVER_URL+"/searchDriver");//"http://10.0.2.2:8080/searchDriver";
	    	}
	    	
	    	
	        uri += "?";
	        
	        List<NameValuePair> data = new LinkedList<NameValuePair>();
	        data.add(new BasicNameValuePair("location", params[0]));
	        data.add(new BasicNameValuePair("destination", params[1]));
	        data.add(new BasicNameValuePair("month", params[2]));
	        data.add(new BasicNameValuePair("day", params[3]));
	        data.add(new BasicNameValuePair("year", params[4]));
	        data.add(new BasicNameValuePair("ordering", params[5]));
	        data.add(new BasicNameValuePair("version",VERSION));
	        String paramString = URLEncodedUtils.format(data, "utf-8");

	        uri += paramString;
	        
	        HttpClient httpClient = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet(uri);
	         
	        try {
	            HttpResponse response = httpClient.execute(httpGet);
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
