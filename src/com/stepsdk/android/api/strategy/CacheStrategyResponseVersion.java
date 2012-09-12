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

import java.util.Map;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.util.Log;

import com.stepsdk.android.api.APIManager;
import com.stepsdk.android.api.APIRequestHandler;
import com.stepsdk.android.cache.CacheStore;
import com.stepsdk.android.cache.api.CachableHttpEntity;

public class CacheStrategyResponseVersion extends CacheStrategy{
	public CacheStrategyResponseVersion(String cacheGroup, String cacheId) {
		super(cacheGroup, cacheId);
	}
	
	@Override
	public void getRequest(APIManager apiManager, String address, final Map<String,String> headerParams,
			APIRequestHandler handler) {
		
		apiManager.get(address, headerParams, getAPIRequestHandler(handler, apiManager.getContext()));
        
	}
	
	private String getCacheVersion(){
		return getCacheId()+"|version";
	}

	@Override
	public void postRequest(APIManager apiManager, String address,
			Map<String, String> params, APIRequestHandler handler) {
		
        apiManager.post(address, params, getAPIRequestHandler(handler, apiManager.getContext()));
	}
	
	private void handleCachedResponse(CachableHttpEntity ent, APIRequestHandler handler){
        log("Use Versioned CACHE("+getCacheGroup()+":"+getCacheId()+")");
        
        handler.before();
        try {
            handler.onResponse(ent.getHttpEntity());
        } catch (Exception e) {
            handler.onException(e);
        }
	}
	
	private APIRequestHandler getAPIRequestHandler(final APIRequestHandler handler, final Context context){
		return new APIRequestHandler() {
		   
		    @Override
		    public void onException(Exception e) {
		    	
		    	// get back the original version
	    		CachableHttpEntity ent = new CachableHttpEntity();
		    	if(getCacheStore(context).getMatched(ent, getCacheGroup(), getCacheId()) != null){
		    		handleCachedResponse(ent, handler);
		    	}else
		    		handler.onException(e);
		        
		    }
		    
		    @Override
		    public void onResponse(HttpEntity response) throws Exception {
		        
		    	CacheStore store = getCacheStore(context);
		    	// check version change
		    	String version = store.get(getCacheGroup(), getCacheVersion());
		    	if( handler.hasVersionChanged(response, version) && version != null ) {
		    		log("VERSION CHANGED FROM "+version+" to "+handler.getVersion(response)+" of CACHE:("+getCacheId()+")");
		    		// store and use new version
		    		storeAndUseCache(response, version);
		    	}else {
		    		CachableHttpEntity ent = new CachableHttpEntity();
		    		if( version!=null && store.getMatched(ent, getCacheGroup(), getCacheId()) != null ) {
			    		log("VERSION REMAINED AS "+version+" of CACHE:("+getCacheId()+")");
		    			// use old version
		    			handleCachedResponse(ent, handler);
		    		} else{

		    			// old version not found, use current response
		    			version = handler.getVersion(response);
			    		log("NO VERSION FOUND AS "+version+" of CACHE:("+getCacheId()+")");

		    			storeAndUseCache(response, version);
		    		}
		    	}
		    	
		        

		    }
		    
		    private void storeAndUseCache(HttpEntity response, String version){
		    	CachableHttpEntity entity = new CachableHttpEntity(getCacheGroup(), getCacheId(), response);
		        getCacheStore(context).put(entity);
		        getCacheStore(context).put(getCacheGroup(), getCacheVersion(), version);
		        
		        CachableHttpEntity newEntity = new CachableHttpEntity();
		        getCacheStore(context).getMatched(newEntity, getCacheGroup(), getCacheId());

		        try{
		        	handler.onResponse(newEntity.getHttpEntity());
		        }catch(Exception e){
		        	onException(e);
		        }
		    }
		    
		    @Override
		    public void before() {
		        handler.before();
		    }
		    
		    @Override
		    public void after() {
		        handler.after();
		    }
		};

	}

}
