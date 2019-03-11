package com.vidyo.vidyosample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import android.os.*;
/**
 * Created by knlee on 3/5/2019.
 */

public class customHttp extends AsyncTask<Void, Void, String>{
    private Exception e;

    protected String doInBackground(Void...arg0){
        HttpURLConnection urlConnection = null;
        String roomlinks = null;
        try {
            URL url = new URL("http://www.vidyokorea.com/coastguard.php");
            //URL url = new URL("http://www.customserver.com/?username=username&password=password");
            urlConnection = (HttpURLConnection) url.openConnection();
            //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), Charset.forName("UTF-8")));
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            in.close();
            roomlinks = stringBuilder.toString();//urls of roomlinks with delimeter probably comma separated it has to be parsed
        } catch(Exception e){
            Log.d("VidyoSampleActivity","httpError"+e.toString());
        }finally {
            urlConnection.disconnect();
            return roomlinks;
        }
    }

}
