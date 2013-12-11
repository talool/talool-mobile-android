package com.talool.android.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.analytics.tracking.android.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
	private ImageView imageView;
	private LinearLayout linearLayout;
	private static Map<String,Bitmap> cache;
	private String url;
	public ImageDownloader(ImageView imageView)
	{
		super();
		this.imageView = imageView;
		createCache();
	}
	
	public ImageDownloader(LinearLayout linearLayout)
	{
		this.linearLayout = linearLayout;
		createCache();
	}
    @Override
    protected Bitmap doInBackground(String... param) {
    
    	
    	if(param != null)
    	{
    		url = param[0];
    		if(url != null)
    		{
    			Bitmap cachedImage = cache.get(url);
    			if(cachedImage != null)
    			{
    				Log.i("Cache hit made");
    				return cachedImage;
    			}
    			else
    			{
    				return downloadBitmap(param[0]);
    			}
    		}
    	}
    	
    	return null;
    }
    
    private void createCache()
    {
		if(cache == null)
		{
			cache = new HashMap<String, Bitmap>();
		}
    }

    @Override
    protected void onPostExecute(Bitmap result) {
    	if(result == null)
    	{
    		return;
    	}
    	else if(imageView != null)
    	{
    		cache.put(url, result);
    		this.imageView.setImageBitmap(result);
    	}
    	else if(linearLayout != null)
    	{
    		cache.put(url, result);
    		BitmapDrawable d = new BitmapDrawable(result);
    		linearLayout.setBackgroundDrawable(d);
    	}
    	
    }

    private Bitmap downloadBitmap(String url) {
        // initilize the default HTTP client object
        final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttoGet request 
        final HttpGet getRequest = new HttpGet(url);
        try {

            HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream 
                    inputStream = entity.getContent();

                    // decoding stream data back into image Bitmap that android understands
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // You Could provide a more explicit error message for IOException
            getRequest.abort();
        } 

        return null;
    }
}