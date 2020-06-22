package com.example.postfeed;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


class CallAPIInternal extends AsyncTask<Void, String, String>  {

    static final int CONNECTION_TIMEOUT = 1000*60;

    private URL url;
    private List<NameValuePair> nameValuePairs;
    private int callerId;
    private String response="";
    private RestAPIResponse.CallApiResponse resp = RestAPIResponse.CallApiResponse.SUCCESS;

    private RestAPIResponse callerObj;


    CallAPIInternal(URL url, List<NameValuePair> nmp, int callerId, RestAPIResponse apiCallback){
        this.url = url;
        this.nameValuePairs = nmp;
        this.callerId = callerId;
        this.callerObj=apiCallback;

    }

    @Override
    protected String doInBackground(Void... voids) {
        response = "";
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(CONNECTION_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);

            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(nameValuePairs));

            writer.flush();
            writer.close();
            os.close();
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            int responseCode=conn.getResponseCode();
            Log.i("====resposecode==",String.valueOf(responseCode));
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                StringBuilder responseBuilder = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                try {
                    response = responseBuilder.toString();
                    JSONObject jresp = new JSONObject(response);
                    if (jresp.has("result")) {
                        if (jresp.getString("result").equals("Server Error")) {
                            resp = RestAPIResponse.CallApiResponse.ERROR;
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.i("callapi","errorpassingjson");
                    e.printStackTrace();
                    resp = RestAPIResponse.CallApiResponse.ERROR;
                }
            }

            else {
                    //response=String.valueOf(responseCode);
                    String line;
                    BufferedReader bre=new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while((line=bre.readLine())!=null){
                        Log.i("debugging",line);
                    }
                    resp = RestAPIResponse.CallApiResponse.ERROR;
            }
        }
        catch (SocketException e)
        {
            Log.i("debugging","socketexception :"+e.getMessage());
            resp = RestAPIResponse.CallApiResponse.TIMEOUT;
        }
        catch (InterruptedIOException e)
        {
            Log.i("debugging","IntereuptedEXception :"+e.getMessage());
            resp = RestAPIResponse.CallApiResponse.TIMEOUT;
        }
        catch (IOException e) {
            Log.i("debugging","IOexception"+e.getMessage());
            resp = RestAPIResponse.CallApiResponse.TIMEOUT;
        }
        catch(Exception e)
        {
            Log.i("debugging",e.getMessage());
            resp = RestAPIResponse.CallApiResponse.ERROR;
            e.printStackTrace();
        }
        return  response;



    }

    @Override
    protected void onPostExecute(String mResponse) {
        //   Log.i("==responsefrom server=",mResponse);
        super.onPostExecute(mResponse);
        Log.i("debuggingpost","Url: "+url+"\n"+"response: "+mResponse);
        callerObj.postRestAPICall(mResponse, callerId, resp);

    }

    @Override
    protected void onCancelled(String s){
        Log.e("debugging", "Cancelled" + s);
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }


}


public class CallAPI  {
    private final static int MAX_RETRY=1;
    private URL url;
    private List<NameValuePair> nameValuePairs;
    private int callerId;
    private int count=0;
    private CallAPIInternal myCallAPI = null;
    private Handler mHandler = null;
    private Runnable mRunnable = this::restartApi;

    private RestAPIResponse restApiCallback = null;

    public CallAPI(URL url, List<NameValuePair> nmp, int callerId, Context context){
        this.url = url;
        this.nameValuePairs = nmp;
        this.callerId = callerId;
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            nameValuePairs.add(new BasicNameValuePair("appversion",version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void restartApi() {
        if(myCallAPI != null) {
            myCallAPI.cancel(true);
        }
        if (count <= MAX_RETRY) {
            count++;
            Log.i("debugging", "Retry call " + count);
            myCallAPI = new CallAPIInternal(url, nameValuePairs, callerId, (response, callerid, callApiResponse) -> {

                if (restApiCallback != null) {
                    if(callApiResponse==RestAPIResponse.CallApiResponse.ERROR)
                    {

                    }
                    mHandler.removeCallbacks(mRunnable);
                    restApiCallback.postRestAPICall(response, callerid, callApiResponse);
                }
            });
            mHandler = new Handler();
            mHandler.postDelayed(mRunnable, CallAPIInternal.CONNECTION_TIMEOUT); // CallAPIInternal.CONNECTION_TIMEOUT
            myCallAPI.execute();
        } else {
            if (restApiCallback != null) {
                restApiCallback.postRestAPICall("", callerId, RestAPIResponse.CallApiResponse.TIMEOUT);
            }
        }
    }

    public void execute(RestAPIResponse apiCallback) {
        restApiCallback = apiCallback;
        restartApi();
    }
}
