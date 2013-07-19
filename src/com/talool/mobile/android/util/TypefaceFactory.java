package com.talool.mobile.android.util;

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

	private TypefaceFactory()
	{}

	public synchronized static TypefaceFactory createInstance(final AssetManager assetManager)
	{
		if (instance == null)
		{
			instance = new TypefaceFactory();
			fontAwesomeTypeFace = Typeface.createFromAsset(assetManager, "fontawesome-webfont.ttf");
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
}
