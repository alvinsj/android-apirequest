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

import com.stepsdk.android.api.APIRequest;

import android.content.Context;

import java.util.HashMap;
import java.util.Queue;

public class APIDataLoader {
    private Context mContext;

    private boolean mIsSynchronous;

    private Queue<APIRequest> mRequestQueue;

    private HashMap<APIRequest, APIDataRequestHandler> mRequestResponses;

    private Queue<APIRequest> mAsyncRequests;

    private int mMax = -1;

    /**
     * Load model/data asynchronously, better handling with queued or managed
     * requests
     * 
     * @param context activity context
     * @param isSynchronous load request one after another, or load immediately
     */
    public APIDataLoader(Context context, boolean isSynchronous) {
        mContext = context;
        mIsSynchronous = isSynchronous;
    }

    public boolean load(APIRequest request, String cacheId, APIDataRequestHandler handler) {
        if (mIsSynchronous)
            return queueRequest(request, cacheId, handler);
        else
            return runRequest(request, cacheId, handler);
    }

    public boolean loadNow(APIRequest request, String cacheId, APIDataRequestHandler handler) {
        // cut queue lah
        return runRequest(request, cacheId, handler);
    }

    private boolean queueRequest(APIRequest request, String cacheId, APIDataRequestHandler handler) {
        if (isFull())
            return false;

        mRequestQueue.add(request);
        return true;
    }

    private boolean runRequest(APIRequest request, String cacheId, APIDataRequestHandler handler) {
        request.startDownload(cacheId,  mContext.getCacheDir(), handler );
        return true;
    }

    private boolean isFull() {
        return !(mRequestQueue.size() < mMax || mMax == -1);
    }

    public void stopAll() {

    }



}
