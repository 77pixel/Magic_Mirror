package com.cleverpixel.magicmirror;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class CustomView extends View 
{
	public List<HairList> hairList = new ArrayList<HairList>();
    public List<Integer> frameList  = new ArrayList<Integer>();

	private int bmpW = 550;
	private int bmpH = 650;
	
	public FaceVector[] fv = new FaceVector[0];	
	
	public Paint paint = new Paint();
	
	public int faceDet = 0;
	public int alphaCh = 255;

	public float scaleFactor = 1.0f; 
	
	public float offsetx = 0;
	public float offsety = 0;

    //
	public int cWidth;  
	public int cHeight;
	
	private Bitmap newBitmap;
    private Bitmap frameBitmap;


	private Context context = null;
	
	//konstruktor___________________________________________________________________________________________________________
	public CustomView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);	
		
		this.context = context;
		//this.setDrawingCacheEnabled(true);
		
		final R.drawable drawableResources = new R.drawable();
		final Class<R.drawable> c = R.drawable.class;
		final Field[] fields = c.getDeclaredFields();
		
		//odczyt id plikow
		int popnr = -1;
		HairList hair = null;
		
		for (int i = 0; i < fields.length; i++) {

            String resStr = "";

            int resId = 0;
            try {
                resId = fields[i].getInt(drawableResources);
                resStr = getResources().getResourceEntryName(resId);
            } catch (Exception e) {
                Log.i("inf:", "e:" + e.getMessage());
            }

            //znaleziono plik na h___________________________________
            if (resStr.startsWith("h")) {
                //ten sam id
                String temp = resStr.substring(1, resStr.length() - 1);
                int nr = Integer.parseInt(temp);

                temp = resStr.substring(resStr.length() - 1, resStr.length());
                int kolor = Integer.parseInt(temp);

                if (nr == popnr) {
                    switch (kolor) {
                        case 0:
                            hair.id_blond = resId;
                            break;
                        case 1:
                            hair.id_dblond = resId;
                            break;
                        case 2:
                            hair.id_brown = resId;
                            break;
                        case 3:
                            hair.id_black = resId;
                            break;
                        default:

                    }

                } else {
                    //inny id
                    if (hair != null) {
                        hairList.add(hair);
                        Log.i("test", hair.id + "; 1:" + hair.id_blond + "; 2:" + hair.id_dblond + " ;3:" + hair.id_brown);
                        hair = null;
                    }

                    hair = new HairList();
                    hair.id = nr;
                    hair.id_blond = resId;
                    popnr = nr;
                }

            }

            //znaleziono plik na f___________________________________
            if (resStr.startsWith("f")) {
                String id = resStr.substring(1, resStr.length());
                frameList.add(resId);
            }
        }
		newBitmap = Bitmap.createBitmap( bmpW, bmpH, Bitmap.Config.ARGB_8888);
	}
	
	//rysuj klatke__________________________________________________________________________________________________________

    @Override
	protected void onDraw(Canvas canvas) 
	{
		
		super.onDraw(canvas);
		
		//wykryto face____________________________________________________
		if ( faceDet == 1)
		{	
			paint.setAlpha(alphaCh);
			
			for(int i = 0; i < fv.length; i++)
			{	
				canvas.drawBitmap(fv[i].bmpMixed, fv[i].x, fv[i].y, fv[i].paint);
			}
			faceDet = 0;
		}

        canvas.drawBitmap(frameBitmap, 0, 0,  new Paint());

	}
	 
	//pozycje twarzy________________________________________________________________________________________________________							
	public void makeFrame(Face[] faces, int cam)
	{
		fv = new FaceVector[faces.length];		
		
		//skala ekranu sprowadzona do 2000x2000__________________________________________
		float qW = (float) cWidth / 2000;
		float qH = (float) (cHeight) / 2000;
		
		//iteracja po wszystkich twarzach________________________________________________
		for(int i = 0; i < fv.length; i++)
		{
			FaceVector fvt = new FaceVector();	
			
			//skala bitmapy______________________________________________________________
			int szerokosc = (faces[i].rect.right  + 1000) - (faces[i].rect.left + 1000);
			
			float w = qW * szerokosc;
			float h = ((float)newBitmap.getHeight() * w) / (float)newBitmap.getWidth(); 
			
			//wyskalowanie_______________________________________________________________
			w += w * 2.2f;
			h += h * 2.2f;
			
			//skala ustawiana +/-________________________________________________________
			w *= (scaleFactor);
			h *= (scaleFactor);
			
			//zapisz wielkosc peruki_____________________________________________________
			
			fvt.bmpMixed = Bitmap.createScaledBitmap(newBitmap, (int) w, (int) h, false);					
		
			//Srodek twarzy______________________________________________________________
			float x = qW * (faces[i].rect.centerY() + 1000);
			float y = qH * (faces[i].rect.centerX() + 1000);
			
			//odwaracanie osi x dla front camery	
			fvt.xRev = (int)(x) - offsetx - w/2;
		
			x = cWidth - x;
			if (cam ==1)
			y = cHeight - y;
				
			fvt.x = (int)(x) - offsetx - w/2;
			fvt.y = (int)(y) - offsety - h/2;
			
			fvt.paint = paint;
			fv[i] = fvt;
			fvt = null;

		}
		
		
	}
	
	public void setBitmapId(int hid, int col)
	{
		Bitmap mBitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);

		Bitmap hBitmap = BitmapFactory.decodeResource(context.getResources(), hairId(hid, col));
		canvas.drawBitmap(hBitmap, 0, 0, new Paint());

		newBitmap = Bitmap.createBitmap(mBitmap, 0, 0, bmpW, bmpH, null, true);

		mBitmap.recycle();
		hBitmap.recycle();
		
	}

    public void setFrameId(int fid)
    {
        Bitmap hBitmap = BitmapFactory.decodeResource(context.getResources(), frameList.get(fid));
        frameBitmap = Bitmap.createScaledBitmap(hBitmap, cWidth, cHeight, false);
        hBitmap.recycle();

    }

	public int hairId(int gid, int color)
	{
		HairList hair = hairList.get(gid);
		int ret = 0;
		
		switch (color)
		{
    		case 0:
    			ret = hair.id_blond;
    		break;
    		case 1:
    			ret = hair.id_dblond;
    		break;
    		case 2:
    			ret = hair.id_brown;
    		break;
    		case 3:
    			ret = hair.id_black;
    		break;
		}
		
		return ret;
	}
	
	public void setViewSize(int w, int h)
	{
		this.cWidth  = w;
		this.cHeight = h;
	}
}
