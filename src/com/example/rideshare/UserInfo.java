package com.example.rideshare;

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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import android.util.Log;

import com.example.rideshare.Search.SearchUser;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserInfo extends Activity{
	
	static StringBuilder email_address;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("User Info");
        setContentView(R.layout.activity_user_info);
        
        contactServer();
    }
	
	static void passEmail(StringBuilder e) 
	{
		email_address = e;
	}
	
	void contactServer() 
	{
		if(!isNetworkAvailable(this)){
			Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		return;
		}
		//do server-side stuff
		Bundle extras = getIntent().getExtras();
		String email = extras.getString("email");
		GetUserInfo gu_info = new GetUserInfo();
		//String result = su.doInBackground(location,destination,month,day,year,hours_f,mins_f,hours_t,mins_t);
		String server_response = "init";
		gu_info.execute(email);
    	try{
    		server_response = gu_info.get(10000, TimeUnit.MILLISECONDS);
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
        	Toast.makeText(getApplicationContext(), R.string.error_user_info, Toast.LENGTH_SHORT).show();
        	email_address.setLength(0);
        	finish();
        }
        else {
        	
        	String [] name_email_num = server_response.split("-");
        	if(name_email_num.length < 3){
        		Toast.makeText(getApplicationContext(), R.string.unknown_connection_error, Toast.LENGTH_SHORT).show();
        		return;
        	}
        	//Log.i("UserInfo","Name_email_num length: "+name_email_num.length);
        	TextView show_name = (TextView) findViewById(R.id.user_show_name);
        			 show_name.setText(name_email_num[0]);
        			 
        	TextView show_num = (TextView) findViewById(R.id.user_show_num);
        			 show_num.setText(name_email_num[1]);
        	
        	TextView show_email = (TextView) findViewById(R.id.user_show_email);
        			 show_email.setText(name_email_num[2]);
        	
        }
		
	}
	
	class GetUserInfo extends AsyncTask<String, Void, String> {
		 
	    @Override
	    protected String doInBackground(String... params) {
	        //handle one way if searching for driver and another way if searcher for rider
	    	//if looking for driver search other database and to field is not relevant
	    	
	    	String uri = SERVER_URL+"/userInfo";
	    	
	        uri += "?";
	        
	        List<NameValuePair> data = new LinkedList<NameValuePair>();
	        data.add(new BasicNameValuePair("email", params[0]));
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
		super.onBackPressed();
		//Intent intent = new Intent(this, SearchResults.class);
        //startActivity(intent);
        finish();
	}
	

}
