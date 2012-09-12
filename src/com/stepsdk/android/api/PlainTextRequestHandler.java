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

import android.util.Log;

import java.io.InputStream;

public abstract class PlainTextRequestHandler extends APIRequestHandler {
    public static final String TAG = "PlainTextRequestHandler";

    protected String mResponse;

    @Override
    public void onResponse(HttpEntity response) throws Exception {
        mResponse = parsePlainTextObject(response);
    }

    public String getResponse() {
        return mResponse;
    };

    public String parsePlainTextObject(HttpEntity ent) throws Exception {
        if (ent != null) {
            try {
                InputStream instream = ent.getContent();
                String result = NetworkUtil.convertStreamToString(instream);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Error Reading text: " + e.getMessage());
                throw new PlainTextResponseError(e.getMessage());
            }
        }
        throw new PlainTextResponseError("Null entity returned.");
    }

    public class PlainTextResponseError extends Exception {
        private static final long serialVersionUID = 1L;

        public PlainTextResponseError(String string) {
            super(string);
        }

    }

}
