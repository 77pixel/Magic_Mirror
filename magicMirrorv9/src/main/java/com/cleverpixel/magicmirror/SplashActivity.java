package com.cleverpixel.magicmirror;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
 
//Activity for Splash screen
public class SplashActivity extends Activity
{
  
	ImageView img;
    private boolean mIsBackButtonPressed;
    private static final int SPLASH_DURATION = 4200; //6 seconds
    private Handler myhandler;
  
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

        myhandler = new Handler();
        
        img = (ImageView) findViewById (R.id.imageLogo);
        AnimationSet set = new AnimationSet(true);

        Animation fadeIn = FadeIn(1800);
        fadeIn.setStartOffset(0);
        set.addAnimation(fadeIn);

        Animation fadeOut = FadeOut(1800);
        fadeOut.setStartOffset(2400);
        set.addAnimation(fadeOut);

        img.startAnimation(set);
        
        // run a thread to start the home screen
        myhandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
               finish();
                 
               if (!mIsBackButtonPressed)
               {
                    // start the home activity
            	   	img.setVisibility(View.GONE);
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(intent);
               }
                
               
            }
  
        }, SPLASH_DURATION);
    }
    	
    private Animation FadeIn(int t)
    {
        Animation fade;
        fade = new AlphaAnimation(0.0f,1.0f);
        fade.setDuration(t);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }
    private Animation FadeOut(int t)
    {
        Animation fade;
        fade = new AlphaAnimation(1.0f,0.0f);
        fade.setDuration(t);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }
    
    
    
    //handle back button press
    @Override
    public void onBackPressed()
    {
        mIsBackButtonPressed = true;
        super.onBackPressed();
    }
        
}