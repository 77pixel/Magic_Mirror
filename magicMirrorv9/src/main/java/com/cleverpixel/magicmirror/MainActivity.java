package com.cleverpixel.magicmirror;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity 
{
	Camera myCamera = null;
	SurfaceView mySurfaceView;
	SurfaceHolder mySurfaceHolder;
	private CustomView viewItems;
	
	private CameraInfo[] cams;
	private boolean isPreview;
	
	private ImageSwitcher imHair = null;
    private ImageSwitcher imFrame = null;

	private int idHair = 1;
    private int idFrame = 1;
	private int cameraId = 1;
	
	private int vWidth;
	private int vHeight;
	
	private boolean faceClick = false;

	private LinearLayout menuR = null;
	private Boolean rightMenuSlide = false; 
	
	private LinearLayout menuL = null;
	private Boolean leftMenuSlide = false; 
	
	private int menuLw;
	private int menuRw;

	private InterstitialAd interstitial;
	
	//*********************************************************************************************
	//Overrides ***********************************************************************************
	//*********************************************************************************************
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        setContentView(R.layout.activity_main);
        
        startAdds();

		Display display = getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);

	    //odczyt kamer w urzadzeniu____________________________________________
	    int camCount = Camera.getNumberOfCameras();
	    cams = new CameraInfo[camCount];
	    
	    for(int i = 0; i < camCount; i++)
		{
	    	try 
	    	{    		
	    		myCamera = Camera.open(i);
	    		cams[i] = new CameraInfo(myCamera, size.y, size.x);
	    		//Log.i("test", "w: " + cams[i].bestSize.width + "h: "  + cams[i].bestSize.height);
	    		myCamera.release();
	    		myCamera = null;
	    	} 
	    	catch(Exception e) 
	    	{        
	        	//Log.i("test", "e: " +e.getMessage().toString() );
	        }
		}

        //setVSize(cams[cameraId].bestSize.width  , cams[cameraId].bestSize.height);
        setVSize(size.x, size.y);

	    mySurfaceView = (SurfaceView)findViewById(R.id.cameraSurface);
        mySurfaceHolder = mySurfaceView.getHolder();
        mySurfaceHolder.addCallback(mySurfaceCallback);

        // nakladka na kamere__________________________________________________
        viewItems = (CustomView) findViewById(R.id.myCustomView);

        viewItems.setViewSize(size.x, size.y);
        viewItems.setBitmapId(idHair, 0);
        viewItems.setFrameId(idFrame);

        // BUTTONY ******************************************************************************** 
	    // strzalki ###############################################################################
        findViewById(R.id.ibTop)   .setOnClickListener(buttonListner); 
        findViewById(R.id.ibBottom).setOnClickListener(buttonListner);
        findViewById(R.id.ibLeft)  .setOnClickListener(buttonListner);
        findViewById(R.id.ibRight) .setOnClickListener(buttonListner);
        
        //zoom ####################################################################################
        findViewById(R.id.ibIn)    .setOnClickListener(buttonListner);
        findViewById(R.id.ibOut)   .setOnClickListener(buttonListner);
       
        //pokaz/ukryj menu ########################################################################
	    findViewById(R.id.BtRightMenu).setOnClickListener(menuListner);
	    findViewById(R.id.BtLeftMenu) .setOnClickListener(menuListner);
	    
	    // kamera, icofb, folder #####################################################################
        findViewById(R.id.ibSwitchCamera).setOnClickListener(centerBtListner);
	    findViewById(R.id.ibTake)        .setOnClickListener(centerBtListner);
	    findViewById(R.id.ibFB)          .setOnClickListener(centerBtListner);
	    findViewById(R.id.ibPic)         .setOnClickListener(centerBtListner);


        //zmieniacz rekwizytow ####################################################################
        findViewById(R.id.ibFrameLeft).setOnClickListener(frameListner);
        findViewById(R.id.ibFrameRight).setOnClickListener(frameListner);

        //ikonki___________________________________________________________________________________
        imFrame = (ImageSwitcher) findViewById(R.id.switchFrame);
        imFrame.setFactory(new ViewFactory()
        {
            @Override
            public View makeView()
            {
                return new ImageView(MainActivity.this);
            }
        });

        imFrame.setInAnimation(this, android.R.anim.slide_in_left);
        imFrame.setOutAnimation(this, android.R.anim.slide_out_right);

        imFrame.setBackgroundResource(viewItems.frameList.get(idFrame));


        //zmieniacz rekwizytow ####################################################################
        findViewById(R.id.ibHairLeft).setOnClickListener(hairListner);   
        findViewById(R.id.ibHairRight).setOnClickListener(hairListner); 
    
        //ikonki___________________________________________________________________________________
        imHair = (ImageSwitcher) findViewById(R.id.switchHair);
        imHair.setFactory(new ViewFactory() 
        {
            @Override
            public View makeView() 
            {
                return new ImageView(MainActivity.this);
            }
        });
        
        imHair.setInAnimation(this, android.R.anim.slide_in_left);
        imHair.setOutAnimation(this, android.R.anim.slide_out_right);
        
        imHair.setBackgroundResource(viewItems.hairId(idHair, 0));
        setIcons(1, 0);
        
        //menu ************************************************************************************
        
        LinearLayout ml1 =(LinearLayout)findViewById(R.id.laySliderHair);
        ml1.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        menuLw = ml1.getMeasuredWidth();
        ml1 = null;
        
        LinearLayout mr1 =(LinearLayout)findViewById(R.id.layZoom);
        mr1.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        
        GridLayout mr2 =(GridLayout)findViewById(R.id.layArrows);
        mr2.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        
        menuRw = mr1.getMeasuredWidth() + mr2.getMeasuredWidth();
        mr1 = null;
        mr2 = null;
        
        menuR = (LinearLayout) findViewById(R.id.RightMenu);
	    ObjectAnimator slideR = ObjectAnimator.ofFloat(menuR, "translationX", 0, menuRw);
		slideR.setDuration(1);
	    slideR.start();
	    
	    menuL = (LinearLayout) findViewById(R.id.LeftMenu);
	    ObjectAnimator slideL = ObjectAnimator.ofFloat(menuL, "translationX", 0, -menuLw);
		slideL.setDuration(1);
	    slideL.start();
       
	}
	
    @Override
    protected void onResume() 
    {
    	super.onResume();
    	cameraInit(cameraId);
    }

    @Override
    protected void onPause() 
    { 
    	super.onPause();
    	cameraDel();
    }
    
    private void setIcons(int id, int col)
    {
    	LinearLayout layColor = (LinearLayout) findViewById(R.id.LayHairColor);	
    	
    	((ViewGroup) layColor).removeAllViews();
    	
    	for (int i = 0; i < 4; i++)
    	{
    		int resid = viewItems.hairId(id, i);
    		
    		if(resid != 0)
    		{
    			
	    		ImageButton ib = new ImageButton(getBaseContext());
		    	if(col != i)
	    		ib.setBackgroundResource(resid);
		    	ib.setTag(i);
		    	ib.setLayoutParams(new LayoutParams(64, 64));
		    	ib.setOnClickListener(new OnClickListener()
		    	{
					@Override
					public void onClick(View arg0) 
					{
						int colid = (Integer) arg0.getTag();
						imHair.setBackgroundResource(viewItems.hairId(idHair, colid)); 
	                	viewItems.setBitmapId(idHair, colid);
	                	setIcons(idHair,colid);
					}
				});
		    	layColor.addView(ib);
    		}
    	}
    }
    
    //*********************************************************************************************
    //listnery do buttonow ************************************************************************
    //*********************************************************************************************
    
    //starzalki i skala ___________________________________________________________________________
    final OnClickListener buttonListner = new OnClickListener() 
    {
        public void onClick(final View v) 
        {
            switch(v.getId()) 
            {
                //strzalki _______________________________________________________________________
            	case R.id.ibTop:
                	viewItems.offsety +=10;
                break;
                case R.id.ibBottom:
                	viewItems.offsety -=10;
                break;
                case R.id.ibLeft:
                	viewItems.offsetx +=10 ;
                break;
                case R.id.ibRight:
                	viewItems.offsetx -=10 ;                
                break;  
                
                //zoom ____________________________________________________________________________
                case R.id.ibIn:
                	viewItems.scaleFactor += 0.1F ;
                break;
                case R.id.ibOut:
                	viewItems.scaleFactor -= 0.1F ;
                break;    
                
            }
        }
    };

    //menu wysuwanie_______________________________________________________________________________
    final OnClickListener menuListner = new OnClickListener() 
    {
        public void onClick(final View v) 
        {
            switch(v.getId()) 
            {
	            //prawe menu ______________________________________________________________________
            	case R.id.BtRightMenu:
		            if(!rightMenuSlide) //pokaz menu
		    		{
		        	    ObjectAnimator slide = ObjectAnimator.ofFloat(menuR, "translationX", menuRw, 0);
		        	    slide.setDuration(500);
		        	    slide.start();
		        	    rightMenuSlide = true;    
		    		}
		    		else //ukryj menu
		    		{
		    			ObjectAnimator slide = ObjectAnimator.ofFloat(menuR, "translationX", 0, menuRw);
		    			slide.setDuration(500);
		    			slide.start();
		        	    rightMenuSlide = false;
		    		}
	            break;
	            
	            //lewe menu ______________________________________________________________________
	            
            	case R.id.BtLeftMenu:
            		
            		if(!leftMenuSlide) //pokaz menu
            		{
    	        	    ObjectAnimator slide = ObjectAnimator.ofFloat(menuL, "translationX", -menuLw, 0);
    	        	    slide.setDuration(500);
    	        	    slide.start();
    	        	    leftMenuSlide = true; 
    	        	    
            		}
            		else //ukryj menu
            		{
            			ObjectAnimator slide = ObjectAnimator.ofFloat(menuL, "translationX", 0, -menuLw);
            			slide.setDuration(500);
            			slide.start();
                	    leftMenuSlide = false;
            		}
            	break;
            }
        }
    };
    
    //zrob zdjecie, icofb, zmiana kamery, folder______________________________________________________
    final OnClickListener centerBtListner = new OnClickListener() 
    {
        public void onClick(final View v) 
        {
            switch(v.getId()) 
            {
                //zmien kamere ____________________________________________________________________
            	case R.id.ibSwitchCamera:
            		if (cameraId == 0) cameraId = 1;             		
            		else cameraId = 0;
            		cameraInit(cameraId);
                break;
                //zr�b foto________________________________________________________________________
            	case R.id.ibTake:
                	myCamera.takePicture(null, null, null, picCallback);
                break;
                //wyslij do FB ____________________________________________________________________
                case R.id.ibFB:
                	faceClick = true;
                	myCamera.takePicture(null, null, null, picCallback);
                break;
                //pokaz folderpic _________________________________________________________________
                case R.id.ibPic:
                	openImgFolder();                
                break;  

            }
        }
    };

    //zmaian wlosow _______________________________________________________________________________
    final OnClickListener hairListner = new OnClickListener() 
    {
    	public void onClick(final View v) 
        {	
    		switch(v.getId()) 
			{
            	//w lewo___________________________________________________________________________
            	case R.id.ibHairLeft:
            		idHair -= 1;
                	if (idHair < 0 ) idHair = (viewItems.hairList.size()-1);
                	imHair.setBackgroundResource(viewItems.hairId(idHair, 0)); 
                	viewItems.setBitmapId(idHair, 0);
                	setIcons(idHair, 0);
                break;
                //w prawo _________________________________________________________________________
                case R.id.ibHairRight:
                	idHair += 1;
                	if (idHair > (viewItems.hairList.size()-1)) idHair = 0;
                	imHair.setBackgroundResource(viewItems.hairId(idHair, 0));
                	viewItems.setBitmapId(idHair, 0);
                	setIcons(idHair, 0);
                break;
			}
		}
    };

    //zmiana ramki _________________________________________________________________________________
    final OnClickListener frameListner = new OnClickListener()
    {
        public void onClick(final View v)
        {
            switch(v.getId())
            {
                //w lewo___________________________________________________________________________
                case R.id.ibFrameLeft:
                    idFrame -= 1;
                    if (idFrame < 0 ) idFrame = (viewItems.frameList.size()-1);
                    imFrame.setBackgroundResource(viewItems.frameList.get(idFrame));
                    viewItems.setFrameId(idFrame);
                    break;
                //w prawo _________________________________________________________________________
                case R.id.ibFrameRight:
                    idFrame += 1;
                    if (idFrame > (viewItems.frameList.size()-1)) idFrame = 0;
                    imFrame.setBackgroundResource(viewItems.frameList.get(idFrame));
                    viewItems.setFrameId(idFrame);
                    break;
            }
            viewItems.invalidate();
        }
    };

	//*********************************************************************************************
	//funkcje *************************************************************************************
    //*********************************************************************************************
	
    //zrob zdjecie
    PictureCallback picCallback = new PictureCallback()
    {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) 
		{
		    File pictureFileDir = getDir();

		    if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) 
		    {
		      Toast.makeText(getBaseContext(), "Can't create directory to save image.",
		          Toast.LENGTH_LONG).show();
		      return;

		    }

		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		    String date = dateFormat.format(new Date());
		    String photoFile = "magic_" + date + ".jpg";

		    String filename = pictureFileDir.getPath() + File.separator + photoFile;

		    File pictureFile = new File(filename);
		    try {
		    	
		    	Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
	            Bitmap bitmap;
	            
		    	if(cameraId == 1)
		    	{
		    		bitmap = rotate(realImage, -90);
		    	}
		    	else
		    	{
		    		bitmap = rotate(realImage, 90);
		    	}
		    	
	    		//bitmapa z klatki kamery________________________
	    		
	    		Bitmap reBitmap = Bitmap.createScaledBitmap(bitmap, vWidth, vHeight, false);			
	    		
	    		//zapisz do canvasa______________________________	            		
	    		Bitmap mBitmap = Bitmap.createBitmap(viewItems.cWidth, viewItems.cHeight, Bitmap.Config.ARGB_8888);
	    		Canvas canvas = new Canvas(mBitmap);

	    		canvas.drawBitmap(reBitmap, 0, 0, new Paint());
	    		
	    		//rysuj dodatki__________________________________
	    		for(int i = 0; i < viewItems.fv.length; i++)
	    		{	
	    			if(cameraId == 1)
	    			{
	    				Matrix matrix = new Matrix(); 
	        			matrix.preScale(-1.0f, 1.0f); 
	        			Bitmap mBmp = Bitmap.createBitmap(viewItems.fv[i].bmpMixed, 0, 0, viewItems.fv[i].bmpMixed.getWidth(), viewItems.fv[i].bmpMixed.getHeight(), matrix, false);
	        			canvas.drawBitmap(mBmp, viewItems.fv[i].xRev, viewItems.fv[i].y, viewItems.fv[i].paint);
	        			mBmp.recycle();
	    			}
	    			else
	    			{
	    				canvas.drawBitmap(viewItems.fv[i].bmpMixed, viewItems.fv[i].x, viewItems.fv[i].y, viewItems.fv[i].paint);
	    			}
	    		}
	    		
	    		Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), viewItems.frameList.get(idFrame)),
	    											  vWidth, vHeight, false);
	    		canvas.drawBitmap(bm, 0, 0, new Paint());
	    		
	    		reBitmap.recycle();
	    		bm.recycle();

	            FileOutputStream fos = new FileOutputStream(pictureFile);               
	            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	            fos.close();   
		      
	            Toast.makeText(getBaseContext(), "Image Saved: " + photoFile, Toast.LENGTH_LONG).show();
		    } 
		    catch (Exception error) 
		    {  
		      Toast.makeText(getBaseContext(), "Image could not be saved.", Toast.LENGTH_LONG).show();
		    }
			
		    //pokaz facebook______________________________________________________________________________
		    if(faceClick)
		    {
		    	Intent intent = new Intent(MainActivity.this, FBActivity.class);
	        	try
	        	{
	        		intent.putExtra("file", filename);
	        	}
	        	catch(Exception e)
	        	{
	
	        	}	
				startActivity(intent);
				faceClick = false;
		    }
		    
		    cameraDel();
        	cameraInit(cameraId);
		}
    };
	  
    public static Bitmap rotate(Bitmap bitmap, int degree) {
		    
		  int w = bitmap.getWidth();
		  int h = bitmap.getHeight();

		  Matrix mtx = new Matrix();
		  mtx.postRotate(degree);

		  return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}
    
    private File getDir() 
	  {
	    File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	    return new File(sdDir, "MagicMirror");
	  }
    
    //holder kamery________________________________________________________________________________
    
    //powierzchnia wyswietlacza____________________________________________________________________
    SurfaceHolder.Callback mySurfaceCallback = new SurfaceHolder.Callback()
	{
		 @Override
		 public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
		 {
			 cameraInit(cameraId);
		 }

		 @Override
		 public void surfaceCreated(SurfaceHolder holder) 
		 {

		 }
	
		 @Override
		 public void surfaceDestroyed(SurfaceHolder holder) 
		 {
		   // TODO Auto-generated method stub 
		 }
	};
	
	//wykryto twarz________________________________________________________________________________
	private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() 
	{	
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) 
		{		

			if (faces.length > 0)
			{	
				viewItems.makeFrame(faces, cameraId);
				viewItems.faceDet = 1;		
			}
			
			viewItems.invalidate();
		}
	};

	
	
	//ustaw wielko�� ekranu________________________________________________________________________
	private void setVSize(int w, int h)
	{
		vWidth  = w;
		vHeight = h;
	}
	
	//zatrzymaj camere_____________________________________________________________________________
	private void cameraDel()
    {
    	if (myCamera != null)
    	{
	     	if(isPreview)
	    	{
	    		myCamera.stopPreview();
	    		myCamera.setPreviewCallback(null);
	    	}
	     	
	     	try
	     	{
	        	myCamera.stopFaceDetection();
		    	myCamera.release();	
	     	}
	     	catch(Exception ex)
	     	{
	     		
	     	}
	     	
	    	myCamera = null;
	    	isPreview = false;
    	}
    }
    
	//init dla kamery______________________________________________________________________________
    private void cameraInit(int id)
    {
    	cameraDel();
    	
    	int w = cams[cameraId].bestSize.width;
    	int h = cams[cameraId].bestSize.height;
    	
		viewItems.setViewSize(vWidth, vHeight);
		viewItems.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));
    	
    	cameraId = id;
    	myCamera = Camera.open(id);
    	Camera.Parameters myParameters = myCamera.getParameters();
		myParameters.setPreviewSize(w, h);
		myParameters.setRotation(90); 
		myCamera.setParameters(myParameters);
		myCamera.setDisplayOrientation(90);
		myCamera.startPreview();
		isPreview = true;
		
		myCamera.setFaceDetectionListener(faceDetectionListener);
		myCamera.startFaceDetection(); 
		
		mySurfaceHolder.setFixedSize(vWidth, vHeight);        
		
		try 
		{
			myCamera.setPreviewDisplay(mySurfaceHolder);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
    }
    
    //otworz podglad zdjecia_______________________________________________________________________
    private void openImgFolder()
    {  	
    	File folder = getDir() ;
    	String[] allFiles = folder.list();
	    
    	try
        {
    		Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
    		File file = new File(folder + "/" + allFiles[(allFiles.length-1)]); 
    		myIntent.setDataAndType(Uri.fromFile(file), "image/*");
    		startActivity(myIntent);
        }
        catch (Exception e) 
        {
        
        }
    }   
    
    public void startAdds()
	{

        AdView mAdView;
        mAdView = new AdView(this);
	    mAdView.setAdUnitId("ca-app-pub-7426364944132734/9583744407");
	    mAdView.setAdSize(AdSize.SMART_BANNER);
	    AdRequest request = new AdRequest.Builder()
	     			//.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
	     			//.addTestDevice("6027F1093E8AB0750A0291A0ABFF6FAE")  // xperia z
	     			.build();
	     
	     
	    interstitial = new InterstitialAd(MainActivity.this);
	    // Insert the Ad Unit ID
	    interstitial.setAdUnitId("ca-app-pub-7426364944132734/6760082001");
	    interstitial.loadAd(request);
	    
	    // Prepare an Interstitial Ad Listener
        interstitial.setAdListener(new AdListener() 
        {
            public void onAdLoaded() 
            {
            	if (interstitial.isLoaded()) 
            	{
                    interstitial.show();
                }    
            }
        });
	    
	    
	    RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLay);
	    LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	    layout.addView(mAdView, params);
	    mAdView.loadAd(request);
	}
}