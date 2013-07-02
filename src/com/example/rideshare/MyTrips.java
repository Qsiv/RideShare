package com.example.rideshare;

import static com.example.rideshare.CommonUtilities.SERVER_URL;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
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

import com.example.rideshare.Search.SearchUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class MyTrips extends Activity 
{
	
	 StringBuilder trip = new StringBuilder("");
	LinearLayout tripList;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.my_trips);
        setContentView(R.layout.mytrips);
        tripList = (LinearLayout) findViewById(R.id.trips);
        
        if (notification_type.toString().equalsIgnoreCase("accepted_by_ROU") || 
			notification_type.toString().equalsIgnoreCase("RSU_joined") || 
			notification_type.toString().equalsIgnoreCase("ROU_invite")) 
        {
        	
        	trip = new StringBuilder("store"); 
        	//do server-side stuff
        	Trips st = new Trips();
    		//String result = su.doInBackground(location,destination,month,day,year,orderBy);
    		String server_response = "init";
    		st.execute();
        	try{
        		server_response = st.get(10000, TimeUnit.MILLISECONDS);
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
    		
    
            //pass results to SearchResults activity class before finishing this activity
           /* if(server_response.equals("init")){
            	Toast.makeText(getApplicationContext(), "Error on Storing trip", Toast.LENGTH_SHORT).show();
            } */
            
            notification_type.setLength(0);
            trip_user_name.setLength(0);
            trip_user_email.setLength(0);
            tripID.setLength(0);
            finish();
        }
        
        else if (notification_type.toString().equalsIgnoreCase("canceled_by_ROU"))
        {
        	trip = new StringBuilder("remove");
        	cancelTrip();
        	finish();
        }
        
        else {
        	trip = new StringBuilder("retrieve");
        	updateInfo();
        }
        
    }
	
	void updateInfo() {
		
		Trips st = new Trips();
		//String result = su.doInBackground(location,destination,month,day,year,orderBy);
		String server_response = "init";
		st.execute();
    	try{
    		server_response = st.get(10000, TimeUnit.MILLISECONDS);
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
		

        //pass results to SearchResults activity class before finishing this activity
        if(server_response.equals("init")){
        	Toast.makeText(getApplicationContext(), "Error retrieving trips. Please try again.", Toast.LENGTH_SHORT).show();
        }
        
        String [] id_name_email = server_response.split("-");
        
        int count;
		for (count = 1; count < id_name_email.length; count += 3 ) 
		{
			//make container Linear Layout
			LinearLayout innerLL = new LinearLayout(this);
			innerLL.setOrientation(LinearLayout.HORIZONTAL);
			innerLL.setLayoutParams(new LinearLayout.LayoutParams(
	                LayoutParams.MATCH_PARENT,
	                LayoutParams.WRAP_CONTENT, 1));
			
			//make textview
    		final TextView valueTV = new TextView(this);
			valueTV.setText(id_name_email[count] + "     " + id_name_email[count-1]);
			valueTV.setId(Integer.parseInt(id_name_email[count-1]));
			valueTV.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT, 1));
			
			//make rating bar
			final RatingBar barRB  = new RatingBar(this);
			barRB.setId(Integer.parseInt(id_name_email[count-1]));
			barRB.setLayoutParams(new LinearLayout.LayoutParams(
	                LayoutParams.MATCH_PARENT,
	                LayoutParams.WRAP_CONTENT, 1));
			barRB.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					boolean timePassed = false; 
					
					//boolean = true if time has passed 
					
					if (timePassed) {
						//allow rating and send it to server
						float frate = ((RatingBar) v).getRating();
						String rating = Float.toString(frate);
						sendRating(rating);
					}
					else
							Toast.makeText(getApplicationContext(), R.string.rate_after_ride, Toast.LENGTH_SHORT).show();
				    		
				}});
			
			//add the textView and ratingBar to the inner linear layout
			((LinearLayout) innerLL).addView(valueTV);
			((LinearLayout) innerLL).addView(barRB);
			
        	//add the inner linear layout to the Linear Layout contained in the scrollView
			((LinearLayout) tripList).addView(innerLL);
        
			notification_type.setLength(0);
			trip_user_name.setLength(0);
			trip_user_email.setLength(0);
			tripID.setLength(0);
		}
	}

////////BEGIN sendRATING
void sendRating(String rating) 
{
	Rating rate = new Rating();
	
	String server_response = "init";
	rate.execute(rating);
	try{
		server_response = rate.get(10000, TimeUnit.MILLISECONDS);
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
}
/////////////////////END sendRATING


void cancelTrip() {
        Trips st = new Trips();
		//String result = su.doInBackground(location,destination,month,day,year,orderBy);
		String server_response = "init";
		st.execute();
    	try{
    		server_response = st.get(10000, TimeUnit.MILLISECONDS);
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
		

        //pass results to SearchResults activity class before finishing this activity
       /* if(server_response.equals("init")){
        	Toast.makeText(getApplicationContext(), "Error on Getting trip", Toast.LENGTH_SHORT).show();
        } */
       
        notification_type.setLength(0);
        trip_user_name.setLength(0);
        trip_user_email.setLength(0);
        tripID.setLength(0);
        finish();
    }
        
	static StringBuilder notification_type = new StringBuilder("");
	static StringBuilder trip_user_name = new StringBuilder("");
	static StringBuilder trip_user_email = new StringBuilder("");
	static StringBuilder trip_user_phone = new StringBuilder("");
	static StringBuilder tripID = new StringBuilder("");
	static void setNotificationType(StringBuilder type, StringBuilder name, StringBuilder email_address, StringBuilder phone, StringBuilder id)
	{
		notification_type = type;
		trip_user_name = name;
		trip_user_email = email_address;
		trip_user_phone = phone;
		tripID = id;
	}
	
	
	class Trips extends AsyncTask<String, Void, String> {
		 
	    @Override
	    protected String doInBackground(String... params) {
	        //handle one way if searching for driver and another way if searcher for rider
	    	//if looking for driver search other database and to field is not relevant
	    	
	    	String uri = "";
	    	
	    	if (trip.toString().equals("store")) //store trip in my trips section on server
	    	{
	    		uri += (SERVER_URL+"/storeTrips");//"http://10.0.2.2:8080/storeTrips";
	    	
	        uri += "?";
	        
	        List<NameValuePair> data = new LinkedList<NameValuePair>();
	        data.add(new BasicNameValuePair("activeEmail", MainActivity.activeUser.email));
	        data.add(new BasicNameValuePair("trip_id", tripID.toString()));
	        data.add(new BasicNameValuePair("name", trip_user_name.toString()));
	        data.add(new BasicNameValuePair("email", trip_user_email.toString()));
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
	    }  
	else if (trip.toString().equals("retrieve")) //get trips in my trips section on server
	{
		uri += (SERVER_URL+"/retrieveTrips");//"http://10.0.2.2:8080/retrieveTrips";
	
		uri += "?";
        
        List<NameValuePair> data = new LinkedList<NameValuePair>();
        data.add(new BasicNameValuePair("activeEmail", MainActivity.activeUser.email));
        data.add(new BasicNameValuePair("trip_id", tripID.toString()));
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
	}
	else if (trip.toString().equals("remove")) //store trip in my trips section on server
	    	{
	    		uri += (SERVER_URL+"/removeTrips");//"http://10.0.2.2:8080/removeTrips";
	    	
	        uri += "?";
	        
	        List<NameValuePair> data = new LinkedList<NameValuePair>();
	        data.add(new BasicNameValuePair("activeEmail", MainActivity.activeUser.email));
	        data.add(new BasicNameValuePair("trip_id", tripID.toString()));
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
	    }
	     
	        return null;
	    }
	    
	}

	
	class Rating extends AsyncTask<String, Void, String> {
		 
	    @Override
	    protected String doInBackground(String... params) {
	        //handle one way if searching for driver and another way if searcher for rider
	    	//if looking for driver search other database and to field is not relevant
	    	
	    	String uri = (SERVER_URL+"/rateUser");//"http://10.0.2.2:8080/rateUser";
	    	
	        uri += "?";
	        
	        List<NameValuePair> data = new LinkedList<NameValuePair>();
	        data.add(new BasicNameValuePair("activeEmail", MainActivity.activeUser.email));
	        data.add(new BasicNameValuePair("rating", params[0]));
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
		Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
	}
	
}
