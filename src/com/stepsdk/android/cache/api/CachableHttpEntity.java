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
package com.stepsdk.android.cache.api;

import com.stepsdk.android.cache.Cachable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.entity.EntitySerializer;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

public class CachableHttpEntity implements Cachable{

    public HttpEntity mHttpEntity = null;
    public String mKey = null;
    public String mType = null;
    
    public CachableHttpEntity(){

    }
    
    public CachableHttpEntity(String type, String key, HttpEntity ent){
        mHttpEntity  = ent;
        mType = type;
        mKey = key;
    }

    
    public String cacheId() {
        return mKey;
    }

    public String cacheType() {
        return mType;
    }

    public Cachable fromCache(String type, String key, HashMap<String, String> cache) {
        String osStream = cache.get("outputStream");
        if(osStream == null)
            return null;
        InputStream is = new ByteArrayInputStream(osStream.getBytes());
        mHttpEntity = new InputStreamEntity(is, osStream.getBytes().length);
        mType = type;
        mKey = key;
        return this;
    }

    public HashMap<String, String> toCache() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String osString = "";
        try {
            mHttpEntity.writeTo(os);
            osString = new String(os.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("outputStream", osString);
        return map;
    }

    public HttpEntity getHttpEntity() {
        return mHttpEntity;
    }
    
}
