package com.cleverpixel.magicmirror;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
 
public class FBActivity extends FragmentActivity 
{ 
	private LoginButton loginBtn;
	private Button postImageBtn;
	private Button closeBtn;
	private TextView userName;
	private ProgressBar spinner;
	private TextView txtSend;
	private ProfilePictureView profilePictureView;
	
	private EditText eMessage;
	
	private Bitmap bmp;
	
	private UiLifecycleHelper uiHelper;
 
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
 
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
 
		uiHelper = new UiLifecycleHelper(this, statusCallback);
		uiHelper.onCreate(savedInstanceState);
 
		setContentView(R.layout.activity_facebook);
		
		if(getIntent().hasExtra("file")) 
		{
		    
			ImageView imSend = (ImageView) findViewById(R.id.imageLogo);
		    
		    String path = getIntent().getExtras().getString("file");
		    
		    Log.d("ttt", path);
		    
		    Bitmap bmp = BitmapFactory.decodeFile(path);
		    
		    Display display = getWindowManager().getDefaultDisplay();
		    Point size = new Point();
		    display.getSize(size);
		    
		    LayoutParams params = (LayoutParams) imSend.getLayoutParams();
		    params.width = size.x/2;
		    params.height = size.y/2;
		    imSend.setLayoutParams(params);

		    imSend.setImageBitmap(bmp);		    
		}
		
		eMessage = (EditText) findViewById(R.id.editMessage);
		profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
		
		userName = (TextView) findViewById(R.id.user_name);
		loginBtn = (LoginButton) findViewById(R.id.fb_login_button);
		loginBtn.setUserInfoChangedCallback(new UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				if (user != null) 
				{
					userName.setText("Hello, " + user.getName());
					profilePictureView.setProfileId(user.getId());
				} 
				else 
				{
					userName.setText("You are not logged");
					profilePictureView.setProfileId(null);
				}
			}
		});
 
		postImageBtn = (Button) findViewById(R.id.post_image);
		postImageBtn.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View view) {
				postImage();
			}
		});
		
		closeBtn = (Button) findViewById(R.id.btRightMenu);
		closeBtn.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View v) {
				FBActivity.this.finish();
			}
		});
		
		buttonsEnabled(false);
		
		spinner = (ProgressBar)findViewById(R.id.progressBar1);
	    spinner.setVisibility(View.GONE);
	    txtSend = (TextView) findViewById(R.id.textSend);
	    txtSend.setVisibility(View.GONE);
	    
	}
 
	private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (state.isOpened()) {
				buttonsEnabled(true);
				Log.d("FacebookSampleActivity", "Facebook session opened");
			} else if (state.isClosed()) {
				buttonsEnabled(false);
				Log.d("FacebookSampleActivity", "Facebook session closed");
			}
		}
	};
 
	public void buttonsEnabled(boolean isEnabled) 
	{
		postImageBtn.setEnabled(isEnabled);
	}
 
	public void postImage() 
	{
		if (checkPermissions()) 
		{
			spinner.setVisibility(View.VISIBLE);
			txtSend.setText(" Sending picture ");
			txtSend.setVisibility(View.VISIBLE);
			/*
			Request uploadRequest = Request.newUploadPhotoRequest( Session.getActiveSession(), bmp, new Request.Callback()
				{
					@Override
					public void onCompleted(Response response) 
					{	
						spinner.setVisibility(View.GONE);
						txtSend.setText("Done!");
					}
				});
			*/
			Bundle params = uploadRequest.getParameters();
			String message = eMessage.getText().toString();
			params.putString("message", message);
			
			uploadRequest.executeAsync();
		} 
		else 
		{
			requestPermissions();
		}
	}
 
 
	public boolean checkPermissions() {
		Session s = Session.getActiveSession();
		if (s != null) {
			return s.getPermissions().contains("publish_actions");
		} else
			return false;
	}
 
	public void requestPermissions() {
		Session s = Session.getActiveSession();
		//if (s != null)s.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
	}
 
	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
		buttonsEnabled(Session.getActiveSession().isOpened());
	}
 
	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}
 
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		uiHelper.onSaveInstanceState(savedState);
	}
 
}
