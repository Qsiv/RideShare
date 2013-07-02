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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.example.rideshare.MainActivity.UserValidation;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Registration extends Activity 
{	
	//public static final String SERVER_URL = "http://10.0.2.2:8080/";
	public static String server_response;
	static MainActivity context;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Registration");
        setContentView(R.layout.registration);
    }
	static void passContext(MainActivity co) 
	{
		context = co;
	}
	
	String uName, uEmail, uPassword, uNumber;
	
	public void registerClick(View v)
	{
		//insert code to handle registration
		if(!isNetworkAvailable(this)){
    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		return;
    	}
		EditText nameText = (EditText)findViewById(R.id.field_Name);
		EditText emailText = (EditText)findViewById(R.id.field_email);
    	EditText passwordText = (EditText)findViewById(R.id.field_password);
    	EditText phoneNumber = (EditText)findViewById(R.id.field_phone);
    	
   
    	String userName = nameText.getText().toString().trim(); 
    	String userEmail = emailText.getText().toString().trim(); 
    	String userPassword = passwordText.getText().toString();
    	String userNumber = phoneNumber.getText().toString().trim(); 
    	
    	switch(isValid(userEmail,userPassword)){
    		case 0: break; //Correct Password length and valid email length
    		case 1:	//email is too short
    			Toast.makeText(getApplicationContext(), R.string.email_too_short, Toast.LENGTH_SHORT).show();
    			return;
    		case 2: //email is too long
    			Toast.makeText(getApplicationContext(), R.string.email_too_long, Toast.LENGTH_SHORT).show();
    			return;
    		case 3: //password is too short
    			Toast.makeText(getApplicationContext(), R.string.passw_too_short, Toast.LENGTH_SHORT).show();
    			return;
    		case 4: //password is too long
    			Toast.makeText(getApplicationContext(), R.string.passw_too_long, Toast.LENGTH_SHORT).show();
    			return;
    	}
    	if(userPassword.length() == 0){
    		Toast.makeText(getApplicationContext(), R.string.no_phone_num, Toast.LENGTH_SHORT).show();
			return;
    	}
    	if(userName.length() == 0){
    		Toast.makeText(getApplicationContext(), R.string.no_name, Toast.LENGTH_SHORT).show();
			return;
    	}
    	
		
		
		UserRegistration userReg = new UserRegistration();
		//String result = userReg.doInBackground(userName,userEmail,userPassword,userNumber);
		userReg.execute(userName,userEmail,userPassword,userNumber);
		//user already exists
    	try{
    		server_response = userReg.get(5000, TimeUnit.MILLISECONDS);
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
    	if(server_response == null){
    		Toast.makeText(getApplicationContext(), R.string.unknown_connection_error, Toast.LENGTH_SHORT).show();
    	}
    	else if (server_response.equals("User Already exists")){
			Toast.makeText(getApplicationContext(), server_response, Toast.LENGTH_SHORT).show();
		}
		else //go to dashboard as new user
		{
			MainActivity.activeUser.name = userName;
			MainActivity.activeUser.email = userEmail;
			MainActivity.activeUser.phoneNumber = userNumber;
			
			//Store phone number in local storage
			
			///////////////////////////////////////////////
	//****IF STROING NUMBER NOT ACTING CORRECTLY, CHECK THIS. and make static function in MainActivity which will save preferences through the main activity****//
			///////////////////////////////////////////////
			/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(new MainActivity()); 
	    	Editor edit = prefs.edit(); //Needed to edit the preferences
	           edit.putString("user_name", userName);
	           edit.putString("user_email", userEmail);
	           edit.putString("user_password", userPassword);
	           edit.putString("user_phoneNumber", userNumber);
	    	   edit.commit();
			*/
			Toast.makeText(getApplicationContext(), R.string.registration_success, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, Dashboard.class);
			startActivity(intent);
			finish();
		}
		
	}
	public int isValid(String email, String password){
		if(email.length() < 4) return 1;
		if(email.length() > 25) return 2;
		if(password.length() < 6) return 3;
		if(password.length() > 20) return 4;
		return 0;
	}

class UserRegistration extends AsyncTask<String, Void, String> {
	 
    @Override
    protected String doInBackground(String... params) {
        String uri = SERVER_URL + "/register"; //"http://10.0.2.2:8080/register";
        uri += "?";
        
        List<NameValuePair> data = new LinkedList<NameValuePair>();
        data.add(new BasicNameValuePair("name", params[0]));
        data.add(new BasicNameValuePair("email", params[1]));
        data.add(new BasicNameValuePair("password", params[2]));
        data.add(new BasicNameValuePair("phoneNumber", params[3]));
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
        	server_response = "failed";
            uee.printStackTrace();
        } catch (ClientProtocolException cpe) {
        	server_response = "failed";
            cpe.printStackTrace();
        } catch (IOException ioe) {
        	server_response = "failed";
            ioe.printStackTrace();
        }
     
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
    	//server_reponse = result;
    }
    
}
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		//Intent intent = new Intent(this, MainActivity.class);
		//startActivity(intent);
		finish();
	}

}