package com.example.gigyaexample;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

//Import Gigya SDK files
import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.event.GSLoginUIListener;

public class MainActivity extends Activity {
	private GSAPI gsAPI;
	private static MainActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.activity_main);
		//Replace this API key with your own from the gigya.com dashboard.
		gsAPI = new GSAPI("2_Y82PzwJ_chSFImHXaIDJClnLyJzmk-VFOavSsaNTzl6m901s_NNxRAS0xJ3bd3_N", this);
	}
	
	//Returns the context for this Activity (used in dialog alerts)
	public static Context getContext() {
    	return instance;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void LoginPushed(View view){
		if (gsAPI.getSession() == null || gsAPI.getSession().isValid() == false){
			//Show Gigya's Login UI to login with a social network
			gsAPI.showLoginUI(null, new LoginUIListener(), null);
		} else {
			new AlertDialog.Builder(this).setTitle("Alert").setMessage("You are already logged in.").setNeutralButton("Close", null).show();
		}
	}
	
	public void LogoutPushed(View view){
		if (gsAPI.getSession() != null && gsAPI.getSession().isValid() == true){
			//End the user's session with Gigya
			gsAPI.logout();
			new AlertDialog.Builder(this).setTitle("Alert").setMessage("You are now logged out.").setNeutralButton("Close", null).show();
		} else {
			new AlertDialog.Builder(this).setTitle("Alert").setMessage("You are already logged out.").setNeutralButton("Close", null).show();
		}
	}
	
	public void SharePushed(View view){
		if (gsAPI.getSession() != null && gsAPI.getSession().isValid() == true){
			//Create a userAction object to be shared
			GSObject userAction = new GSObject();
			userAction.put("title", "This is my title.");
			userAction.put("description", "This is my description.");
			userAction.put("linkBack", "http://www.gigya.com");
			EditText userMessage = (EditText)findViewById(R.id.editText1);
			userAction.put("userMessage", userMessage.getText().toString());
			
			GSObject image = new GSObject();
			image.put("src", "http://www.gigya.com/wp-content/themes/gigyatm/images/gigya-logo.gif");
			image.put("href", "http://www.gigya.com");
			image.put("type", "image");
			
			GSArray mediaItems = new GSArray();
			mediaItems.add(image);
			
			userAction.put("mediaItems",mediaItems);
			GSObject shareParams = new GSObject();
			shareParams.put("userAction", userAction);
			
			//Define a listener/handler for the API response
			GSResponseListener resListener = new GSResponseListener() {
		        @Override
		        public void onGSResponse(String method, GSResponse response, Object context) {
		            try {
		            	//If the call succeeds
		                if (response.getErrorCode() == 0) {
		        			new AlertDialog.Builder(MainActivity.this).setTitle("Alert").setMessage("Your share has been published.").setNeutralButton("Close", null).show();
		                } else { 
		                    Log.w("GigyaExample","Error from Gigya API: " + response.getErrorMessage());
		                }
		            } catch (Exception ex) {  
		            	ex.printStackTrace();  
		            }
		        }
		    };
		    
		    //Send the API request, passing the response listener we defined above
		    gsAPI.sendRequest("socialize.publishUserAction", shareParams, resListener, null);
		} else {
			new AlertDialog.Builder(this).setTitle("Alert").setMessage("You must be logged in to share.").setNeutralButton("Close", null).show();
		}
	}

}

//Defining a Login event listener
class LoginUIListener implements GSLoginUIListener {
	//Fired when a user logs in with a social network
	public void onLogin(String provider, GSObject user, Object context) {
		String alertText = "";
		try {
			alertText = "Welcome " + user.getString("nickname") + ", you are now logged in.";
			new AlertDialog.Builder(MainActivity.getContext()).setTitle("Alert").setMessage(alertText).setNeutralButton("Close", null).show();
		} catch (GSKeyNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void onClose(boolean arg0, Object arg1) {
		// TODO Auto-generated method stub
	}
	
	public void onLoad(Object arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onError(GSResponse response, Object context) {
		new AlertDialog.Builder(MainActivity.getContext()).setTitle("Alert").setMessage("There was an error logging in.").setNeutralButton("Close", null).show();
		Log.w("GigyaExample","Error from Gigya API: " + response.getErrorMessage());
		
	}
}
