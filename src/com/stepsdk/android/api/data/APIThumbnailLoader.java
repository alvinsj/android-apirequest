/**
 * Copyright (c) 2012 Alvin S.J. Ng
 * 
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject 
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT 
 * WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT 
 * SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @author 		Alvin S.J. Ng <alvinsj.ng@gmail.com>
 * @copyright	2012	Alvin S.J. Ng
 * 
 */

package com.stepsdk.android.api.data;

import com.stepsdk.android.api.APIManager;
import com.stepsdk.android.api.APIRequest;
import com.stepsdk.android.app.AppConfig;
import com.stepsdk.android.event.Event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class APIThumbnailLoader extends Observable{
    protected APIDataLoader mThumbnailLoader;

    //protected HashMap<String, SoftReference<Bitmap>> mThumbnailImages = new HashMap<String, SoftReference<Bitmap>>();

    protected boolean mBusy = false;

    protected Context mContext = null;

    protected BaseAdapter mAdapter = null;
    
    protected Map<Integer, String> mPositionMap = new HashMap<Integer, String>();
    
    protected Map<String, String> mCacheMap = new HashMap<String, String>();
    
    public APIThumbnailLoader(Context context, BaseAdapter adapter) {
        mAdapter = adapter;
        mContext = context;
    }

    public APIThumbnailLoader(Context context) {
        mContext = context;
    }
    
    public void loadThumbnail(final ImageView iv, String url, final String cacheId, Observer observer){
        addObserver(observer);
        loadThumbnail(iv, url, cacheId);
    }
    
    private String arrayToString(Object[] array){
        if(array.length==0)
            return "[]";
        String s = "";
        for(Object i: array)
            s += i.toString()+",";
        return s;
    }
    
    public void loadThumbnail( ImageView iv, String url, String cacheId ) {
        loadThumbnail(-1, iv, url, cacheId);
    }

    public void loadThumbnail(final int position, final ImageView iv, final String url, final String cacheId) {
        
//        // load from position map
//        if(position >= 0 && mPositionMap.containsKey(position)){            
//            Bundle b = new Bundle();
//            b.putString("url", url);
//            b.putString("filepath", mPositionMap.get(position));
//            b.putString("cacheId", cacheId);
//
//            Message msg = new Message();
//            msg.setData(b);
//            msg.obj = iv;
//
//            onThumbnailLoadedHandler().sendMessage(msg);
//
//            return;
//        }
//        
//        // load from cache map
//        if( mCacheMap.containsKey(position) ){
//            Bundle b = new Bundle();
//            b.putString("url", url);
//            b.putString("filepath", mCacheMap.get(position));
//            b.putString("cacheId", cacheId);
//
//            Message msg = new Message();
//            msg.setData(b);
//            msg.obj = iv;
//
//            onThumbnailLoadedHandler().sendMessage(msg);
//            
//            return;
//        }
        
        if(mThumbnailLoader == null)
            mThumbnailLoader = new APIDataLoader(mContext, false);

        if (!mBusy)/* && mThumbnailImages.containsKey(cacheId)
                && mThumbnailImages.get(cacheId).get() != null) {

            iv.setImageBitmap(mThumbnailImages.get(cacheId).get());
            iv.setVisibility(View.VISIBLE);

        } else */{
            onThumbnailLoading();

            if (mBusy)
                return;
            AppConfig.log("APIThumbnailLoader", "Downloading("+url+"):"+cacheId);

            mThumbnailLoader.load(new APIRequest(new APIManager(mContext), url, "GET"), cacheId,
                    new APIDataRequestHandler() {

                        @Override
                        public void onException(Exception e) {
                            AppConfig.log("APIThumbnailLoader", "Error downloading thumbnail: "+cacheId);
                        }

                        @Override
                        public void onResponse(File data) {

                            Bundle b = new Bundle();
                            b.putString("url", url);
                            b.putString("filepath", data.getAbsolutePath());
                            b.putString("cacheId", cacheId);
                            b.putInt("position", position);

                            Message msg = new Message();
                            msg.setData(b);
                            msg.obj = iv;
                            
                            AppConfig.log("APIThumbnailLoader", "Downloaded("+url+"):"+data.getAbsolutePath());
                            onThumbnailLoadedHandler().sendMessage(msg);

                            data = null;
                        }
                        
                        @Override
                        public void after() {
                            AppConfig.log("APIThumbnailLoader", "DownloadingTask ended: "+url);
                        	super.after();
                        }
                    });
        }

    }
    
    // Handler to prevent CallFromWrongThreadException
    protected Handler onThumbnailLoadedHandler(){ 
        return mHandler;
    }
    
    private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(mBusy) return;
                String filepath = msg.getData().getString("filepath");
                String cacheId = msg.getData().getString("cacheId");
                String url = msg.getData().getString("url");
                int position = msg.getData().getInt("position");

                ImageView iv = (ImageView)msg.obj;
                AppConfig.log("APIThumbnailLoader", "Setting thumbnail from:"+filepath);

                try {
                    Bitmap bm = BitmapFactory.decodeFile(filepath);
                    if (iv != null
                            && bm != null
                            && isImageViewPositionId(iv, position, cacheId)) {
                    	
                        iv.setImageURI(Uri.parse(filepath));
                        iv.setVisibility(View.VISIBLE);
                        
                        AppConfig.log("APIThumbnailLoader", "Image("+filepath+") set for :"+cacheId);

                        onThumbnailLoaded(filepath);

                        //mThumbnailImages.put(cacheId, new SoftReference<Bitmap>(bm));
                        mCacheMap.put(cacheId, filepath);
                        if(position>=0){
                            if( mPositionMap.containsKey(position)){
                                mPositionMap.clear();
                                mPositionMap.put(position, filepath);
                            }else{
                                mPositionMap.put(position, filepath);
                            }
                        }

                    }else{
                    	if(iv == null)
                    		throw new Exception("ImageView reference is missing");
                    	else if(bm == null)
                    		throw new Exception("Bitmap cannot be decoded");
                    	else 
                    		AppConfig.log("APIThumbnailLoader", "Wrong position for:"+cacheId);
                    }
                } catch (Exception e) {
                    File f = new File(filepath);
                    f.delete();
                    f = null;
                    AppConfig.log("APIThumbnailLoader", "Exception for ("+filepath+"):"+e.getMessage());

                }
                setChanged();
                notifyObservers(new Event("loadThumbnail").withObjects(msg.obj, url, cacheId, filepath));
                clearChanged();
            }
        };
    

    private boolean mPause = false;
    
    public void pause() {
        mBusy = true;
        mPause = true;
    }
    
    public void play() {
        mBusy = false;

        if (mAdapter != null && mPause) {
            mAdapter.notifyDataSetChanged();
            mPause = false;
        }
    }
    
    public boolean isBusy(){    return mBusy;   }
    public Map<Integer, String> getPositionMap(){   return mPositionMap;    }
    public Map<String, String> getCacheMap(){   return mCacheMap; }
    
    protected void onThumbnailLoaded(String filepath) {}
    protected void onThumbnailLoading() {}
    
    protected boolean isImageViewPositionId(ImageView iv, int position, String cacheId){ return true;    }

}
