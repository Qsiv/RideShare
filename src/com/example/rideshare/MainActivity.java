package com.example.rideshare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import static com.example.rideshare.CommonUtilities.SERVER_URL;
import static com.example.rideshare.CommonUtilities.isNetworkAvailable;



public class MainActivity extends Activity {
	static ActiveUser activeUser = new ActiveUser();
	//public static final String SERVER_URL = "http://10.0.2.2:8080/";
	public static boolean server_responed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	//Check in local storage if 'remember me' checkbox was checked last time 
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this); //Get the preferences
		Boolean rememberYou = prefs.getBoolean("rememberMe", false);
    	
		//if rememberMe was checked, log user in
		if (rememberYou != null && rememberYou)
		{
			rememberMe_login();
		}
		
        super.onCreate(savedInstanceState); 
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        
        setContentView(R.layout.activity_main);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ROBOTOSLAB-BOLD.TTF");
        TextView tv = (TextView) findViewById(R.id.ride_share_title);
        tv.setTypeface(tf);
        
        //Intent intent = new Intent(this, RegisterActivity.class);
        //startActivity(intent);
        //Button regButton = (Button)findViewById(R.id.button_register);       
    }
    
    public void loginClick (View v)
    {
    	if(!isNetworkAvailable(this)){
    		Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	EditText emailText = (EditText)findViewById(R.id.user_email_editText);
    	EditText passwordText = (EditText)findViewById(R.id.user_password_editText);
    	
    	String userEmail = emailText.getText().toString().trim(); 
    	String userPassword = passwordText.getText().toString(); 
    	
    	if (isValid(userEmail, userPassword))
    	{
    	
    	//set activeUser
    	activeUser.email = userEmail;
    	
    	//Get phone number in local storage
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this); //Get the preferences
		String userNumber = prefs.getString("user_phoneNumber", "null");
    	activeUser.phoneNumber = userNumber;
    	
    		CheckBox cb = (CheckBox) findViewById(R.id.remember_me_checkbox);
    		if (cb.isChecked())			//store the fact that's is check in local storage
    		{
    			Editor edit = prefs.edit(); //Needed to edit the preferences
    				edit.putBoolean("rememberMe", true);
    				edit.commit();
    		}
    
    		Intent intent = new Intent(this, Dashboard.class);
            startActivity(intent);
            finish();
    		
    	}
    	else
    	{
            //Toast.makeText(getApplicationContext(), "This ID is invalid", Toast.LENGTH_SHORT).show();
    		emailText.setText("");
    	}
    		
    }
    
    private void rememberMe_login()
    {
    	//get stored user info
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this); //Get the preferences
    	String userName = prefs.getString("user_name", "null");
    	String userEmail = prefs.getString("user_email", "null");
    	String userPassword = prefs.getString("user_password", "null");
    	String userNumber = prefs.getString("user_phoneNumber", "null");
    	
    	if (userName == null || userEmail == null || userNumber == null || userPassword == null)
    	{
    		
            //set rememberMe to false in local storage, so user must re-login
            Editor edit = prefs.edit(); //Needed to edit the preferences
	           edit.putBoolean("rememberMe", false);
	    	   edit.commit();  
	    	   
	    	Toast.makeText(getApplicationContext(), R.string.please_login, Toast.LENGTH_SHORT).show();
	        startActivity(new Intent(this, MainActivity.class));
            finish();
    	}
    	
    	//
    	if (isValid(userEmail, userPassword))
    	{
    	
    	//set activeUser
        	activeUser.password = userPassword;
        	activeUser.email = userEmail;
        	activeUser.phoneNumber = userNumber;
    	
    		CheckBox cb = (CheckBox) findViewById(R.id.remember_me_checkbox);
    		if (cb.isChecked())			//store the fact that's is check in local storage
    		{
    			Editor edit = prefs.edit(); //Needed to edit the preferences
    				edit.putBoolean("rememberMe", true);
    				edit.commit();
    		}
    
    		Intent intent = new Intent(this, Dashboard.class);
            startActivity(intent);
            finish();
    		
    	}
    	
    }
    
    public void regClick (View v)
    {
    	Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
        finish();
    }

    public boolean isValid (String id, String pw)
    {
    	if (id.length() < 4 || id.length() > 25)		//usernames must be between 4 and 12 characters, inclusive
    	{
    		Toast.makeText(getApplicationContext(), R.string.email_invalid, Toast.LENGTH_SHORT).show();
    		EditText emailText = (EditText)findViewById(R.id.user_email_editText);
    		emailText.setText("");
    		return false;
    	}
    	
    	else if (pw.length() < 6 || pw.length() > 20) 	//passwords must be between 6 and 10 characters, inclusive
    	{
    		Toast.makeText(getApplicationContext(), R.string.password_invalid, Toast.LENGTH_SHORT).show();
    		EditText passwdText = (EditText)findViewById(R.id.user_password_editText);
    		passwdText.setText("");
    		return false;
    	}
    	String success = "init";
    	UserValidation userV = new UserValidation();
    	//String success = userV.doInBackground(id,pw);
    	userV.execute(id,pw);
    	try{
    		success = userV.get(10000, TimeUnit.MILLISECONDS);
    	}
    	catch(ExecutionException e){
    		Toast.makeText(getApplicationContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    		return false;
    	}
    	catch(InterruptedException e){
    		Toast.makeText(getApplicationContext(), R.string.interrupted, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    		return false;
    	}
    	catch(CancellationException e){
    		Toast.makeText(getApplicationContext(), R.string.cancelled, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    		return false;
    	}
    	catch(TimeoutException e){
    		Toast.makeText(getApplicationContext(), R.string.connection_timed_out, Toast.LENGTH_SHORT).show();
    		e.printStackTrace();
    		return false;
    	}
    	if(success == null){
    		Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	if (success.equals("failed") || success.equals("incorrect"))
    	{
    		Toast.makeText(getApplicationContext(), R.string.incorrect, Toast.LENGTH_SHORT).show(); //"Email or Password is invalid"
    		return false;
    	}
    		
    	String [] pieces = success.split("-");
    	if (pieces[0].equals("correct"))
    	{
    		activeUser.name = pieces[1]; //set name of the active user
    		return true;
    	}
    	else{
    		Toast.makeText(getApplicationContext(), R.string.unknown_connection_error, Toast.LENGTH_SHORT).show();
    	}
    	
    	
    	return false;
    }
    
    
    class UserValidation extends AsyncTask<String, Void, String> {
    	 
        @Override
        protected String doInBackground(String... params) {
        	//String uri = "http://10.0.2.2:8080/";
        	String uri = SERVER_URL;
            uri += "?";
            
            List<NameValuePair> data = new LinkedList<NameValuePair>();
            data.add(new BasicNameValuePair("email", params[0]));
            data.add(new BasicNameValuePair("password", params[1]));
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
            	server_responed = true;
                uee.printStackTrace();
            } catch (ClientProtocolException cpe) {
            	server_responed = true;
                cpe.printStackTrace();
            } catch (IOException ioe) {
            	server_responed = true;
                ioe.printStackTrace();
            }
         
            return null;
        }
        @Override
        protected void onPostExecute(String result){
        	server_responed = true;
        	//success = result;
        }
        
  }
    

    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
