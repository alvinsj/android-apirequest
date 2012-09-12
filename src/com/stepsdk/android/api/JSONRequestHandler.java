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

import com.stepsdk.android.util.NetworkUtil;

import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.InputStream;

public abstract class JSONRequestHandler extends APIRequestHandler {
    public static final String TAG = "JSONRequestHandler";

    protected JSONObject mResponse;

    @Override
    public void onResponse(HttpEntity response) throws Exception {
        mResponse = parseJSONObject(response);
    }

    public JSONObject getResponse() {
        return mResponse;
    };

    public JSONObject parseJSONObject(HttpEntity ent) throws JSONResponseError {
        String result = null;
        if (ent != null) {
            try {
                InputStream instream = ent.getContent();
                result = NetworkUtil.convertStreamToString(instream);
                
                if(result.startsWith("["))
                    result = "{\"array\":"+result+"}";
                //else
                JSONObject json = new JSONObject(result);
                
                return json;
            } catch (Exception e) {
                if(result != null)
                    Log.d(TAG, "Error JSON format: "+result);
                //Log.e(TAG, "Error Parsing to JSON: " + e.getMessage());
                throw new JSONResponseError(e.getMessage());
            }
        }
        throw new JSONResponseError("Null entity returned.");
    }

    public class JSONResponseError extends Exception {
        private static final long serialVersionUID = 1L;

        public JSONResponseError(String string) {
            super(string);
        }

    }

}
