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

import com.stepsdk.android.util.NetworkUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class DownloadFileTask extends AsyncTask<Void, Integer, Boolean> {
    public static final String TAG = "DownloadRemoteFileTask";

    private static final int MAX_BUFFER_SIZE = 1024; // 1kb

    public static final int DOWNLOADING = 0;

    public static final int COMPLETE = 1;

    public static final int STOPPED = 2;

    private ProgressDialog mProgressDialog;

    private Context mContext;

    private File mTargetFolder;

    private boolean mInterrupt = false;

    private String mUrl;

    private String mId;

    private boolean mEnableProgress = false;

    private File mData = null;

    public File getData() {
        return mData;
    }

    public DownloadFileTask(Context context, String url, String cacheId) {
        super();
        mContext = context;
        mUrl = url;
        mId = cacheId;
        mTargetFolder = defaultCacheFolder(context);
    }
    
    public abstract File defaultCacheFolder(Context context);

    public DownloadFileTask enableProgressBar() {
        mEnableProgress = true;
        return this;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            //if (NetworkUtil.isOnline(mContext)) {
                if(mUrl == null){
                    Log.e("DownloadFileTask", "Download failure (empty url) from "+mContext.getClass().getName());
                    return false;
                }
                
                mData = new File(mTargetFolder, mId);
                if (mData.exists())
                    return true;
                
                HttpURLConnection conn = (HttpURLConnection)new URL(mUrl).openConnection();
                conn.connect();

                double fileSize = conn.getContentLength();
                double downloaded = 0;

                InputStream stream = conn.getInputStream();

                OutputStream out = new FileOutputStream(mData);

                int status = DOWNLOADING;
                while (status == DOWNLOADING) {
                    byte buffer[];
                    if (fileSize - downloaded > MAX_BUFFER_SIZE)
                        buffer = new byte[MAX_BUFFER_SIZE];
                    else
                        buffer = new byte[(int)(fileSize - downloaded)];
                    int read = stream.read(buffer);
                    if (read == -1 || mInterrupt) {
                        publishProgress(100);
                        break;
                    }
                    out.write(buffer, 0, read);
                    downloaded += read;
                    publishProgress((int)((downloaded / fileSize) * 100));
                }

                status = COMPLETE;
                conn.disconnect();
                
                return true;
            //} else {
            //    return false;
            //}
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("DownloadFileTask", "Download failure: "+mUrl+" ("+e.getMessage()+")");
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... changed) {
        if (mEnableProgress)
            mProgressDialog.setProgress(changed[0]);
    }

    @Override
    protected void onPreExecute() {
        if (mEnableProgress) {
            mProgressDialog = progessDialogForStatus(mContext, "Downloading...", true);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    mInterrupt = true;
                    DownloadFileTask.this.cancel(false);
                }

            });
            mProgressDialog.show();
        }

    }
    
    protected ProgressDialog progessDialogForStatus(Context context, String message, boolean cancelable) {
    	ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        return progressDialog;
    }
    
    protected AlertDialog dialogAlertForNotification(Context context, String title, String message, boolean cancelable) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
        dlgBuilder.setMessage(message).setCancelable(cancelable)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return dlgBuilder.create();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mEnableProgress && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        if (result && !mInterrupt) {
            if (mEnableProgress)
            	dialogAlertForNotification(mContext, "Download", "File download successfully", false)
                        .show();
        } else {
            if (mEnableProgress)
                dialogAlertForNotification(mContext, "Download error", "Error downloading file", false)
                        .show();
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        mInterrupt = true;
    }

}
