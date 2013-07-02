package com.example.rideshare;

import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;

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

import com.example.rideshare.MainActivity.UserValidation;
import com.example.rideshare.UserInfo.GetUserInfo;

import static com.example.rideshare.CommonUtilities.isNetworkAvailable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class Profile extends Activity {
    Button edName;
    String name;
    TextView txName;
    
    Button edEmail; 		
    String email;
    TextView txEmail;
    
    Button edPhone; 		
    String phone;
    TextView txPhone;
    
    Button edLocation;
    String loc ;
    TextView txLoc;
    
    EditText editEntry;
    LayoutInflater inflater;
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("Profile");
        setContentView(R.layout.activity_profile);
	
    	edName = (Button) findViewById (R.id.edit_button3);
    	txName = (TextView) findViewById (R.id.profile_name);
    	name = this.getResources().getString (R.string.profile_user_name);
    	txName.setText(MainActivity.activeUser.name);
    	
    	edEmail = (Button) findViewById (R.id.edit_button0);
    	email = this.getResources().getString (R.string.profile_user_email);
    	txEmail = (TextView) findViewById (R.id.text_email);
    	txEmail.setText(MainActivity.activeUser.email);
    	
    	edLocation = (Button) findViewById (R.id.edit_button1);
    	loc = this.getResources().getString (R.string.profile_user_location);
    	txLoc = (TextView) findViewById (R.id.text_location);
    	txLoc.setText(loc);
    	
    	edPhone = (Button) findViewById (R.id.edit_button2);
    	phone = this.getResources().getString (R.string.profile_user_phone);
    	txPhone = (TextView) findViewById (R.id.text_phone);
    	
    	if(MainActivity.activeUser.phoneNumber == "null"){
    		GetUserInfo gu_info = new GetUserInfo();
    		String server_response = "init";
    		gu_info.execute(MainActivity.activeUser.email);
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
        	if(server_response.equals("init")){
            	Toast.makeText(getApplicationContext(), R.string.error_user_info, Toast.LENGTH_SHORT).show();
            }
            else {
            	
            	String [] name_email_num = server_response.split("-");
            	if(name_email_num.length < 3){
            		Toast.makeText(getApplicationContext(), R.string.unknown_connection_error, Toast.LENGTH_SHORT).show();
            		return;
            	}
            	MainActivity.activeUser.phoneNumber = name_email_num[1];
            }
    	}
    	txPhone.setText(MainActivity.activeUser.phoneNumber);
    	
    	inflater = this.getLayoutInflater();
    	
	}
	
	public void editName(View v)
    {	
		edit("Name");
    }
	
	public void editEmail(View v)
    {
    	edit("Email");
    }
	
	public void editLocation(View v)
    {
    	//code for changing location
    }
	
	public void editPhone(View v)
    {
    	edit("Phone");
    }
	
	public void edit(final String whatDo)
	{
    	//code for changing name
		final Context ctx = this;
		final View layout = inflater.inflate(R.layout.edit_window, null);
		editEntry = (EditText)layout.findViewById(R.id.edit_entry);
		if(whatDo.equals("Phone"))
			editEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(ctx);

		exitDialogBuilder.setTitle("Edit " + whatDo);
		exitDialogBuilder.setView(layout)
		.setCancelable(true)
		.setPositiveButton("Done", new DialogInterface.OnClickListener()
		{ 
			public void onClick (DialogInterface dialog, int id)
			{
				if(!isNetworkAvailable(getApplicationContext())){
		    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
		    		return;
		    	}
				if (whatDo.equals("Name"))
				{
					MainActivity.activeUser.name = editEntry.getText().toString();
					txName.setText(editEntry.getText().toString());
					//add code that communicates with server
				}
				//else if (whatDo.equals("Email"))
				//{
				//	MainActivity.activeUser.email = editEntry.getText().toString();
				//	txEmail.setText(editEntry.getText().toString());
				//	//add code that communicates with server
				//}
				else if (whatDo.equals("Phone"))
				{
					MainActivity.activeUser.phoneNumber = editEntry.getText().toString();
					txPhone.setText(editEntry.getText().toString());
					//add code that communicates with server
				}
				UpdateProfile_serverSide userV = new UpdateProfile_serverSide();
		    	String success = "";
		    	userV.execute(MainActivity.activeUser.name,MainActivity.activeUser.email,MainActivity.activeUser.phoneNumber);
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
				dialog.cancel();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick (DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});
		exitDialogBuilder.show();
	}
	class UpdateProfile_serverSide extends AsyncTask<String, Void, String> {
   	 
        @Override
        protected String doInBackground(String... params) {
        	//String uri = "http://10.0.2.2:8080/";
        	String uri = SERVER_URL + "/updateProfile";
            uri += "?";
            
            List<NameValuePair> data = new LinkedList<NameValuePair>();
            data.add(new BasicNameValuePair("name", params[0]));
            data.add(new BasicNameValuePair("email", params[1]));
            data.add(new BasicNameValuePair("phoneNumber", params[2]));
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
		//Intent intent = new Intent(this, Dashboard.class);
        //startActivity(intent);
        finish();
	}

}
