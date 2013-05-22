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

package com.stepsdk.android.api;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.stepsdk.android.api.data.APIDataRequestHandler;
import com.stepsdk.android.api.strategy.CacheStrategy;
import com.stepsdk.android.cache.CacheStore;

public class APIRequest {
    public static final String GET = "GET";

    public static final String POST = "POST";

    private String mUrl;

    private APIClient mAPIManager;

    private String mMethod;

    private String mPath;

    private Map<String, String> mParams = new HashMap<String, String>();

    private Map<String, String> mHeaderParams = new HashMap<String, String>();
    
    private Map<String, String> mDefaultParams = new HashMap<String, String>();
    
    private Map<String, String> mFiles = new HashMap<String, String>();

    public APIRequest(APIClient manager, String url, String method) {
        mUrl = url;
        mAPIManager = manager;
        mMethod = method;
    }

    public APIRequest setParams(Map<String, String> params) {
        mParams = params;
        return this;
    }

    public APIRequest addHeaderParam(String key, String value) {
        if(key != null && value != null)
            mHeaderParams.put(key, value);
        return this;
    }

    public APIRequest addParam(String key, String value) {
        if(key != null && value != null)
            mParams.put(key, value);
        return this;
    }

    public APIRequest setPath(String path) {
        mPath = path;
        return this;
    }
    
    private int mCacheStrategy = 0;
    private String mCacheType = null;
    private String mCacheId = null;
    
    public APIRequest withCache(int strategy, String type, String cacheId){
    	mCacheId = cacheId;
    	mCacheType = type;
    	mCacheStrategy = strategy;
    	return this;
    }

    public String getRequestUrl() {
        return mUrl;
    }
    
    
    public APIRequest start(APIRequestHandler apiRequestHandler){
        startWithCache(mCacheStrategy, mCacheType, mCacheId, apiRequestHandler);
        return this;
    }
    
    protected CacheStore getCacheStore() throws Exception{
    	return mAPIManager.defaultCacheStore();
    }
    
    // cached request
    public APIRequest startWithCache(int strategy, String type, String cacheId, APIRequestHandler apiRequestHandler) {
        try {
	        if (mMethod.equals(GET)){
	            
	            HashMap<String,String> params = mergeParams(defaultParams(),mParams);
	
	            Iterator<String> i = params.keySet().iterator();
	            
	            if(!mUrl.contains("?"))
	                mUrl += "?";
	            
	            while(i.hasNext()){
	                String key = i.next();
	                if(key==null || params.get(key)==null)
	                    continue;
	                mUrl += key+"="+URLEncoder.encode(params.get(key));
	                if(i.hasNext()) mUrl+="&";
	            }
	            
	            // make cached request
	            if(type==null && cacheId == null)
	                mAPIManager.get(mUrl, mHeaderParams, apiRequestHandler);
	            else
	            	mAPIManager.cachedGet(
	                		CacheStrategy.build(strategy, type, cacheId, getCacheStore())
	                		, mUrl
	                		, mHeaderParams
	                		, apiRequestHandler);
	        }
	        else if (mMethod.equals(POST)){
	            HashMap<String,String> params = mergeParams(defaultParams(),mParams);
	                       
	            if(mFiles.size()>0)
	               mAPIManager.post(mUrl, params, mFiles, apiRequestHandler);
	            else if(type == null && cacheId == null)
	                mAPIManager.post(mUrl, params, apiRequestHandler);
	            else
	            	mAPIManager.cachedPost(
	                		CacheStrategy.build(strategy, type, cacheId, getCacheStore())
	                		, mUrl
	                		, params, apiRequestHandler);
	        }
        }catch(Exception e){
        	apiRequestHandler.onException(e);
        }
        return this;
    }


    private HashMap<String, String> mergeParams(Map<String, String> defaultParams,
            Map<String, String> params) {
        
        HashMap<String,String> newParams = new HashMap<String, String>(defaultParams);
        
        Iterator<String> i = params.keySet().iterator();

        while(i.hasNext()){
            String key = i.next();
            newParams.put(key, params.get(key));
        }
        
        return newParams;
    }

    public void startDownload(String cacheId, File toFolder, APIDataRequestHandler apiRequestHandler) {
        mAPIManager.download(mUrl, cacheId, apiRequestHandler, toFolder);
    }
    
    private Map<String, String> defaultParams(){
        return mDefaultParams;
    }
    
    public APIRequest withDefaultParams(Map<String,String> params){
        mDefaultParams = params;
        return this;
    }

    public void addFile(String key, String filepath) {
        mFiles.put(key, filepath);
    }
    
    
}
