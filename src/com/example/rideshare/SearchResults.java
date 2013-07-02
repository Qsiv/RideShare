package com.example.rideshare;

import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class SearchResults extends Activity {
	private int email_index = 0;
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setTitle("Search Results");
        setContentView(R.layout.activity_search_results);
        

    	LinearLayout LL = (LinearLayout) findViewById(R.id.hold_search_results_LL);
    	
    	//format will be name at index i, and corresponding email at index i + 1
    	//[0] = user.name + '-' + 
    	//[1] = user.location + '-' + 
    	//[2] = user.destination + '-' + 
    	//[3] = user.month + '-' + 
    	//[4] = user.day + '-' + 
    	//[5] = user.year + '-' + 
    	//[6] = user.email + '-' + 
    	//[7] = user.hours + '-' + 
    	//[8] = user.minutes + '-'
    	final String [] names_emails_times = search_results.split("-");
    	Log.i("SearchResults","Search_results length: "+names_emails_times.length);
    	Log.i("SearchResults","Response: "+search_results);
    	//if(names_emails_times.length%9 != 0){
    	//	Log.e("SearchResults","Error Obtaining Search Results");
    	//	Toast.makeText(getApplicationContext(), R.string.unknown_connection_error, Toast.LENGTH_SHORT).show();
    	//	return;
    	//}
    	int count;
		int stride = 9; //# of info inputs per result
		
    	for ( count = 0; count < names_emails_times.length && !search_results.equals("No matches") && names_emails_times.length > 8; count += stride) 
    	{
    		//create a textView for every resulting user
    		final TextView valueTV = new TextView(this);
    		Log.i("onCreate",search_results);
	    	String view_content = "Time: " + names_emails_times[count+7] + ":" + names_emails_times[count+8] + "\n\t" + 
	    							names_emails_times[count+1] + " -> " + names_emails_times[count+2] + " (" +
	    							names_emails_times[count+3] + " " + names_emails_times[count+4] + ", " + 
	    							names_emails_times[count+5] + ")\n\tUser: " + names_emails_times[count] + ", Email: " +
	    							names_emails_times[count+6] + '\n';
	    	email_index = count+6;
	        valueTV.setText(view_content);
	        valueTV.setId(count/stride);
	        valueTV.setOnClickListener(new OnClickListener() {
				String ea = names_emails_times[valueTV.getId() + 1];
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(SearchResults.this,UserInfo.class);
					intent.putExtra("email", names_emails_times[email_index]);
			        startActivity(intent);
			        UserInfo.passEmail(new StringBuilder(ea));
				}});
	        valueTV.setLayoutParams(new LinearLayout.LayoutParams(
	                LayoutParams.MATCH_PARENT,
	                LayoutParams.WRAP_CONTENT));

	        	//add it to the Linear Layout contained in the scrollView
	        ((LinearLayout) LL).addView(valueTV);

	    	}

	    	count = (++count) / stride;		//now contains number of results
        
	}
	

	static String search_results;
	
	static void setResults(String res)
	{
		search_results = res;
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
