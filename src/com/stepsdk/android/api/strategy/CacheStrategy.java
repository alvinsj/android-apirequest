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
package com.stepsdk.android.api.strategy;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.stepsdk.android.api.APIClient;
import com.stepsdk.android.api.APIRequestHandler;
import com.stepsdk.android.cache.CacheStore;
import com.stepsdk.android.cache.api.CachableHttpEntity;

public abstract class CacheStrategy {
	public static final int STRATEGY_MATCH = 1;
	public static final int STRATEGY_LATEST = 2;
	public static final int STRATEGY_RESPONSE_VERSION = 3;
	public static final int STRATEGY_MATCH_REPLACE = 4;

	
    private String mCacheGroup;
    private String mCacheId;
	
	public static CacheStrategy build(int strategy, String cacheGroup, String cacheId, final CacheStore cacheStore){
		CacheStrategy cache = null;
		switch(strategy){
		case STRATEGY_MATCH: cache = new CacheStrategyMatch(cacheGroup, cacheId){

			@Override
			protected CacheStore getCacheStore(Context context) {
				return cacheStore;
			}
			
		};break;
		case STRATEGY_LATEST: cache = new CacheStrategyLatest(cacheGroup, cacheId){

			@Override
			protected CacheStore getCacheStore(Context context) {
				return cacheStore;
			}
			
		};break;
		case STRATEGY_RESPONSE_VERSION: cache = new CacheStrategyResponseVersion(cacheGroup, cacheId){

			@Override
			protected CacheStore getCacheStore(Context context) {
				return cacheStore;
			}
			
		};break;
		case STRATEGY_MATCH_REPLACE: cache = new CacheStrategyMatchReplace(cacheGroup, cacheId){

			@Override
			protected CacheStore getCacheStore(Context context) {
				return cacheStore;
			}
			
		};break;
		}
		return cache;
	}
    
    protected abstract CacheStore getCacheStore(Context context);
    
	
	public CacheStrategy(String cacheGroup, String cacheId){
		mCacheGroup = cacheGroup;
		mCacheId = cacheId;
	}
	
	public String getCacheGroup(){
		return mCacheGroup;
	}
	
	public String getCacheId(){
		return mCacheId;
	}
	
    public abstract void getRequest(APIClient apiManager, String address, final Map<String,String> headerParams, APIRequestHandler handler);
    
    public abstract void postRequest(APIClient apiManager, String address, Map<String, String> params ,APIRequestHandler handler);
    
    protected void log(String message) {
    	Log.i("APICache", message);
    }
	
}
