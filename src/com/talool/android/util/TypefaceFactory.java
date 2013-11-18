package com.talool.android.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * A factory for TypeFaces
 * 
 * @author clintz
 * 
 */
public class TypefaceFactory
{
	private static TypefaceFactory instance = null;
	private static Typeface fontAwesomeTypeFace = null;
	private static Typeface markerFeltNormalTypeFace = null;
	private static Typeface markerFeltWideTypeFace = null;

	private TypefaceFactory()
	{}

	public synchronized static TypefaceFactory createInstance(final AssetManager assetManager)
	{
		if (instance == null)
		{
			instance = new TypefaceFactory();
			fontAwesomeTypeFace = Typeface.createFromAsset(assetManager, "fontawesome-webfont.ttf");
			markerFeltNormalTypeFace = Typeface.createFromAsset(assetManager, "MarkerFelt_Normal.ttf");
			markerFeltWideTypeFace = Typeface.createFromAsset(assetManager, "MarkerFeltWide_Regular.ttf");
		}
		return instance;
	}

	public static TypefaceFactory get()
	{
		return instance;
	}

	public Typeface getFontAwesome()
	{
		return fontAwesomeTypeFace;
	}
	
	public Typeface getMarkerFelt()
	{
		return markerFeltNormalTypeFace;
	}
	
	public Typeface getMarkerFeltWide()
	{
		return markerFeltWideTypeFace;
	}
}
