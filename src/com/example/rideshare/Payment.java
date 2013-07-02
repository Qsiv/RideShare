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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.example.rideshare.Profile.UpdateProfile_serverSide;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Payment extends Activity
{
	EditText payField;
	public void onCreate (Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setTitle("Payment");
        setContentView(R.layout.activity_payment);
        payField = (EditText) findViewById(R.id.field_account);
	}
	
	public void payClick(View v)
	{
		if (verifyCard(payField.getText().toString()))
		{
			/*
			 * Send notification to server about payment
			 */
			Bundle extras = getIntent().getExtras();
			String tripID = extras.getString("trip_id");
			
			sendPayment userV = new sendPayment();
	    	String success = "";
	    	userV.execute(tripID);
	    	try{
	    		success = userV.get(10000, TimeUnit.MILLISECONDS);
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
			finish();
		}

	}
	
	public boolean verifyCard(String number)
	{
		int sum = 0;
		if (number.length() != 16 || String.valueOf(Long.parseLong(number)).length() != 16)
		{
			Toast.makeText(getApplicationContext(), "Invalid Entry", Toast.LENGTH_SHORT).show();
			return false;
		}
		else
		{
			for (int i = 15; i > -1; i--)
			{
				if(i%2 == 0)	//even
				{
					if((Integer.parseInt("" + number.charAt(i)) * 2) <= 9)
						sum += (Integer.parseInt("" + number.charAt(i)) * 2);
					else
						sum += (((Integer.parseInt("" + number.charAt(i)) * 2) % 10 )+ 1);
				}
					
				else			//odd
					sum += Integer.parseInt("" + number.charAt(i));
			}
		}
		if (sum % 10 == 0)
		{
			Toast.makeText(getApplicationContext(), "Payment Accepted", Toast.LENGTH_SHORT).show();
			return true;	
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Invalid Entry", Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	class sendPayment extends AsyncTask<String, Void, String> {
	   	 
        @Override
        protected String doInBackground(String... params) {
        	//String uri = "http://10.0.2.2:8080/";
        	String uri = SERVER_URL + "/payment";
            uri += "?";
            
            List<NameValuePair> data = new LinkedList<NameValuePair>();
            data.add(new BasicNameValuePair("trip_id", params[0]));
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
        @Override
        protected void onPostExecute(String result){
        	//success = result;
        }
        
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
