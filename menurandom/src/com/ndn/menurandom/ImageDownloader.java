/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ndn.menurandom;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

//import android.net.http.AndroidHttpClient;
import com.ndn.menurandom.AndroidHttpClient;
import android.net.http.*;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader {
    private static final String LOG_TAG = "ImageDownloader";

    private int mDefaultImageResourceId;
    private boolean mUseLoadingProgress;
    
    private File mCacheDir;
    private String mStrCacheDir;
    private Context mContext;
    
    public ImageDownloader (Context context, String cacheDir, int defaultId, boolean useLoadingProgress)
    {
    	mDefaultImageResourceId = defaultId;
    	mUseLoadingProgress = useLoadingProgress;
    	mStrCacheDir = cacheDir;
    	
    	initialize(context);
    }
    
    private void initialize(Context context)
    {
    	mContext = context;
    	if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
    		mCacheDir=new File(android.os.Environment.getExternalStorageDirectory(), mStrCacheDir);
        else
        	mCacheDir=context.getCacheDir();
        
    	if(!mCacheDir.exists())
    	{
    		mCacheDir.mkdirs();
    	}
    }
    
    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, ImageView imageView) {
        
    	if(url == null || imageView == null)	{
    		return;
    	}
    	
    	resetPurgeTimer();
        makeFrameLayout(imageView);
        showLoadingProgress(imageView);
        
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null) {
            forceDownload(url, imageView);
        } else {
        	cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
            hideLoadingProgress(imageView);
        }
    }
    
    private void makeFrameLayout(ImageView imageView)
    {
    	boolean isExist = false;
    	ViewGroup vg = (ViewGroup)imageView.getParent();
    	if(vg instanceof FrameLayout)
    	{
    		FrameLayout frameLayout = (FrameLayout)vg;
    		String tag = (String)frameLayout.getTag();
    		if(tag != null && tag.equals("fl_imagedownloader"))
    		{
    			isExist = true;
    		}
    	}
    	
    	if(!isExist)
    	{
			int childCount = vg.getChildCount();
			int index = 0;
			while(index < childCount )
			{
				if(imageView == vg.getChildAt(index))
				{
					break;
				}
				index ++;
			}
			vg.removeViewAt(index);
			
			FrameLayout frameLayout = new FrameLayout(vg.getContext().getApplicationContext());
			frameLayout.setTag("fl_imagedownloader");
			ViewGroup.LayoutParams lpImageView = (ViewGroup.LayoutParams)imageView.getLayoutParams();
			frameLayout.setLayoutParams(lpImageView);
			imageView.setLayoutParams(new LayoutParams(lpImageView.width, lpImageView.height));
			frameLayout.setPadding(imageView.getPaddingLeft(), imageView.getPaddingTop(), imageView.getPaddingRight(), imageView.getPaddingBottom());
			imageView.setPadding(0,0,0,0);
			frameLayout.addView(imageView);
			vg.addView(frameLayout, index);
			
			
			ProgressBar progressBar = new ProgressBar(frameLayout.getContext());
			progressBar.setTag("pb_imagedownloader");
			int leftRightPadding = (imageView.getLayoutParams().width - 50) / 2;
			int topBottomPadding = (imageView.getLayoutParams().height - 50) / 2;
			progressBar.setPadding(leftRightPadding, topBottomPadding, leftRightPadding, topBottomPadding);
			frameLayout.addView(progressBar);
			
    	}
    }
    
    private void showLoadingProgress(ImageView imageView)
    {
    	if(mUseLoadingProgress)
    	{
	    	FrameLayout frameLayout = (FrameLayout)imageView.getParent();
	        ProgressBar progressBar = (ProgressBar)frameLayout.findViewWithTag("pb_imagedownloader");
	        progressBar.setVisibility(View.VISIBLE);
    	}
    }

    private void hideLoadingProgress(ImageView imageView)
    {
    	FrameLayout frameLayout = (FrameLayout)imageView.getParent();
        ProgressBar progressBar = (ProgressBar)frameLayout.findViewWithTag("pb_imagedownloader");
        progressBar.setVisibility(View.INVISIBLE);
    }

    

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
        	BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
            DownloadedDrawable downloadedDrawable = null;
            if(mContext != null)
        	{
            	Resources res = mContext.getResources();
        		Drawable d = res.getDrawable(mDefaultImageResourceId);
        		if(d instanceof BitmapDrawable)
        		{
        			 Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        			 downloadedDrawable = new DownloadedDrawable(res, bitmap, task);
        		}
        	}
           
            
            imageView.setImageDrawable(downloadedDrawable);
            imageView.setMinimumHeight(156);
            task.execute(url);
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    Bitmap downloadBitmap(String url) {
        int IO_BUFFER_SIZE = 4 * 1024;

        // AndroidHttpClient is not allowed to be used from the main thread
        HttpClient client = null;
        HttpGet getRequest = null;

        try {

        	client = AndroidHttpClient.newInstance("Android");
            getRequest = new HttpGet(url);

        	HttpResponse response = client.execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    File file = getFile(url);
                    OutputStream outputStream = new FileOutputStream(file);
                    copyStream(inputStream, outputStream);
                    outputStream.close();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    return decodeFile(file);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
        	if(getRequest != null) getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
        	if(getRequest != null) getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
        	if(getRequest != null) getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } finally {
            if (client != null && (client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
        return null;
    }
    
    private void copyStream(InputStream is, OutputStream os) throws Exception
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){
        	
        	throw ex;
        }
    }

   

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return downloadBitmap(url);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
                if (this == bitmapDownloaderTask && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    hideLoadingProgress(imageView);
                }
            }
        }
    }


    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    private class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable( Resources res, Bitmap bitmap, BitmapDownloaderTask bitmapDownloaderTask) {
            super(res, bitmap);
        	bitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }
    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int DELAY_BEFORE_PURGE = 120 * 1000; // in milliseconds

    private Map<String, SoftReference<Bitmap>> mMemoryCache = Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());
    
    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
    	
    	Bitmap returnVal = null;
    	if(mMemoryCache.containsKey(url))
    	{
    		SoftReference<Bitmap> ref = mMemoryCache.get(url);
            returnVal = ref.get();	
    	}
        
        if(returnVal == null)
        {
        	File file = getFile(url);
        	returnVal = decodeFile(file);
        }
        return returnVal;
    }
    
    
    private File getFile(String url)
    {
    	//I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        File f = new File(mCacheDir, filename);
        return f;
    }
    
    private Bitmap decodeFile(File file)
    {
    	try{
        	BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize=1;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, option);
    	}catch(Exception e){
    		return null;
    	}
    }
    
    
    public void deleteCache(String url)
    {
    	mMemoryCache.remove(url);
    	File file = getFile(url);
    	file.delete();
    }
    
    
    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        mMemoryCache.put(url, new SoftReference<Bitmap>(bitmap));
    }
    
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
    	mMemoryCache.clear();
//    	File[] files=mCacheDir.listFiles();
//        if(files==null)
//            return;
//        for(File f:files)
//            f.delete();
    }
    

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
    
    
    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
