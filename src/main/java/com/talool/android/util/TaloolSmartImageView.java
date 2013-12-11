package com.talool.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;

import com.loopj.android.image.SmartImageView;

public class TaloolSmartImageView extends SmartImageView {
	
	private OnPreDrawListener pdl;
	private static final boolean DEBUG = false;

	public TaloolSmartImageView(Context arg0) {
		super(arg0);
		initScaling();
	}
	
	public TaloolSmartImageView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		initScaling();
	}

	public TaloolSmartImageView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		initScaling();
	}

	public void initScaling()
	{
		
		final TaloolSmartImageView iv = this;
		pdl = new ViewTreeObserver.OnPreDrawListener() {
		    public boolean onPreDraw() {
		        scaleImage(iv.getMeasuredWidth());
		        return true;
		    }
		};
		this.getViewTreeObserver().addOnPreDrawListener(pdl);
	}
	
	private void scaleImage(int scaledWidth)
	{	
		// Get the ImageView's bitmap
	    Drawable drawing = this.getDrawable();
	    if (drawing == null) {
	        return; // Checking for null & return, as suggested in comments
	    }
	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    // Get original image dimensions
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();

	    // Determine how much to scale: we always scale the height.  
	    int scaledHeight = scaledWidth * height / width;
	    
	    // remove the listener so this doesn't go on forever
	 	this.getViewTreeObserver().removeOnPreDrawListener(pdl);
	 		
	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.getLayoutParams(); 
	    params.width = scaledWidth;
	    params.height = scaledHeight;
	    this.setLayoutParams(params);
	    
	    if (DEBUG)
	    {
	    	Log.i("Test", "original width = " + Integer.toString(width));
	    	Log.i("Test", "original height = " + Integer.toString(height));
	    	Log.i("Test", "bounding width = " + Integer.toString(scaledWidth));
	    	Log.i("Test", "scaled height = " + Integer.toString(scaledHeight));
	    }
	}

}
