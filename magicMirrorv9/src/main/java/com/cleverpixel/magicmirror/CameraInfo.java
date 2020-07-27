package com.cleverpixel.magicmirror;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;

public class CameraInfo
{		
	List<Size>    	prevSizes;
	List<Size>    	picSizes;
	public Size     bestSize;
	
	public Camera.Parameters params;


	public CameraInfo(Camera _cam, int w, int h) 
	{
		params = _cam.getParameters();
		
		prevSizes = params.getSupportedPreviewSizes();
		picSizes = params.getSupportedPictureSizes();
		
		getBestPreviewSize(w,h);
		
	}
	
	
	private void getBestPreviewSize(int width, int height)
	{
	     bestSize = prevSizes.get(0); 
	     for(int i = 1; i < prevSizes.size(); i++)
	     {
	    	 if((prevSizes.get(i).width * prevSizes.get(i).height) > (bestSize.width * bestSize.height))
	    	 {
	    		 bestSize = prevSizes.get(i);
	    	 }
	     }
	}
	
}
