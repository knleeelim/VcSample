package com.elimnet.nownproctor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.util.Log;
import android.os.*;
/**
 * Created by knlee on 3/5/2019.
 */

public class customHttp extends AsyncTask<String, Void, String>{
    private Exception e;

    protected String doInBackground(String... arg0){
        String urlParameters = arg0[0];
        HttpURLConnection urlConnection = null;
        String return_msg = null;
        try {
            /*
            URL url = new URL("http://10.29.16.213/coastGuard/coastguard.php");
            byte[] postData = urlParameters.getBytes("UTF-8");
            int postDataLength = postData.length;
            //URL url = new URL("http://www.customserver.com/?username=username&password=password");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            urlConnection.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(postData);
            //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("UTF-8")));
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            in.close();
            return_msg = stringBuilder.toString();//urls of roomlinks with delimeter probably comma separated it has to be parsed*/
            return_msg = "ktest3,dAefhhOhis";
        } catch(Exception e){
            Log.d("VidyoSampleActivity","httpError"+e.toString());
        }finally {
            //urlConnection.disconnect();
            return return_msg;
        }
    }

}
