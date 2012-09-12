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
package com.stepsdk.android.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.stepsdk.android.util.QuickUI;

public class BlindHttpUploadTask extends AsyncTask<String, Integer, Boolean > {
    public static final String TAG = "UploadLocalFileTask";

    private static final int MAX_BUFFER_SIZE = 1024; 
    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;
    
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private String mTargetUrl;

    public BlindHttpUploadTask( Context context, String targetUrl) {
        super();
        mContext = context;
        mTargetUrl = targetUrl;
    }
    
    @Override
    protected Boolean doInBackground(String... pos) {
        String urlString = mTargetUrl;
        
        String filename;
        try {
            filename = URLDecoder.decode(pos[1], "UTF-8");
        }catch(Exception e) {
            filename = pos[1];
        }
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try {

            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

            DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
            OutputStreamWriter dosw = new OutputStreamWriter(dos);

            dosw.write(twoHyphens + boundary + lineEnd);
            dosw.write("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + filename +"\"" + lineEnd);
            dosw.write(lineEnd);
            dosw.flush();
            
            // create a buffer of maximum size
            String filepath;
            // handle special character
            try {
                filepath = URLDecoder.decode(pos[0], "UTF-8");
            }catch(Exception e) {
                filepath = pos[0];
            }
            InputStream fis = new FileInputStream(new File(filepath));
            int bufferSize = Math.min(fis.available(), MAX_BUFFER_SIZE);
            
            double fileSize = fis.available();
            double downloaded = 0;
            
            // read file and write it into form...
            byte[] buffer = new byte[bufferSize];
            int bytesRead = fis.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bufferSize);

                bufferSize = Math.min(fis.available(), MAX_BUFFER_SIZE);
                bytesRead = fis.read(buffer, 0, bufferSize);
                downloaded += bytesRead;
               
            }
            
            dos.flush();

            dosw.write(lineEnd);
            dosw.write(twoHyphens + boundary + twoHyphens + lineEnd);

            fis.close();
            
            dosw.flush();
            //dos.flush();
    
            InputStream is = conn.getInputStream();
            int ch;
    
            StringBuffer b =new StringBuffer();
            while( ( ch = is.read() ) != -1 ){
                b.append( (char)ch );
            }

            dos.close();
            //dosw.close();
            publishProgress(100);
            return true;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
   


    @Override
    protected void onPreExecute() {
        //mProgressDialog = QuickUI.dialogProgressHorizontal(mContext, mContext.getString(R.string.msg_upload_file), false);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mProgressDialog.dismiss();
        if(result){               
            AlertDialog alertDialog = QuickUI.dialogAlert(mContext
                    , "File uploaded"
                    , "File uploaded"
                    , false);
            alertDialog.setButton("OK", new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show(); 
            
        }
        else {
            AlertDialog alertDialog = QuickUI.dialogAlert(mContext
                            , "Error"
                            , "File upload error"
                            , false);
            alertDialog.show();
        }
    }
}