package com.example.bojidar.sofiapublictransportapp.asynctasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.bojidar.sofiapublictransportapp.model.Line;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bojidar on 2/21/2018.
 */

public class ApiCallAsyncTask extends AsyncTask<String,Void,List<Line>> {

    public interface IResult{
        void onResult(List<Line> result);
        NetworkInfo networkConnectivityInfo();
        void noInternetConnectionToast(boolean flag);
    }

    private IResult iResult;

    @Override
    protected void onPreExecute() {
        NetworkInfo networkInfo = iResult.networkConnectivityInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {

            iResult.noInternetConnectionToast(true);
            cancel(true);
        }else{
           iResult.noInternetConnectionToast(false);
        }
    }

    @Override
    protected List<Line> doInBackground(String... params) {

        String strJSON=extractJSONfromURL("https://api-arrivals.sofiatraffic.bg/api/v1/arrivals/"+params[0]+"/");

        if(strJSON==null){
            return null;
        }


//        Log.d("BR", StringEscapeUtils.unescapeJava(strJSON));

        String resultString = "";
        List<Line> lines = new ArrayList<>();

        String lineNumber ="";
        String lineTypeName ="";
        int lineType = 0; //tram,bus,trolley
        String arrivals= "";
        try {
            JSONObject rootJsonObject = new JSONObject(strJSON);
            JSONArray linesJsonArray = rootJsonObject.getJSONArray("lines");

            for(int position=0;position<linesJsonArray.length();position++){
//                resultString = resultString+rootJSONArray.getJSONObject(position).getString("lineName")+"-> "
//                        +rootJSONArray.getJSONObject(position).getString("timing")+"\n";

                lineTypeName = linesJsonArray.getJSONObject(position).getString("vehicle_type");
                switch(lineTypeName){
                    case "bus":
                        lineType = 0;
                        break;
                    case "tram":
                        lineType = 1;
                        break;
                    case "trolley":
                        lineType = 2;
                        break;
                }
                lineNumber = linesJsonArray.getJSONObject(position).getString("name");

                JSONArray arrivalsArray = linesJsonArray.getJSONObject(position).getJSONArray("arrivals");

                for(int arrPos=0;arrPos<arrivalsArray.length();arrPos++){
                    String time = arrivalsArray.getJSONObject(arrPos).getString("time");
                    time = time.split(":")[0]+":"+time.split(":")[1];

                    if(arrPos==0){
                        arrivals = time;
                    }else{
                        arrivals = arrivals+", "+time;
                    }
                }

//                lineNumber=rootJSONArray.getJSONObject(position).getString("lineName");
//                lineType = rootJSONArray.getJSONObject(position).getInt("type");
//                arrivals = rootJSONArray.getJSONObject(position).getString("timing");

//                try {
//                    arrivals = "02:08:03,02:56:12,02:58:15";
//                    arrivals = getArrivalsInTimeLeftFormat(arrivals);
//                } catch (ParseException e) {
//                    Log.e("Time Calc",e.getMessage());
//                }

                lines.add(new Line(lineNumber,lineType,arrivals));
            }

        } catch (JSONException e) {
            Log.e("",e.getMessage());
        }

        return lines;
    }

    @Override
    protected void onPostExecute(List<Line> s) {
            iResult.onResult(s);
    }

    public IResult getIResult(){
        return iResult;
    }

    public void setIResult(IResult iResult){
        this.iResult = iResult;
    }

    private String extractJSONfromURL(String urlStr){
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpURLConnection=null;
        try {
            httpURLConnection= (HttpURLConnection) url.openConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader in = null;
        StringBuilder strJSON=new StringBuilder("");
        try {
            in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                strJSON.append(inputLine);
            }
            in.close();

            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return strJSON.toString();
    }

    public String getArrivalsInTimeLeftFormat(String arrivals) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date currentTimeDate = Calendar.getInstance().getTime();
        String currentTime = sdf.format(currentTimeDate);
        ArrayList<String> calculatedTimes = new ArrayList<>();

        String[] arrivalsSplitted = arrivals.split(",");
        int sec,min,hrs;
        int curSec,curMin,curHrs;

        String[] curSecMinHrsArr = currentTime.split(":");
        curSec = Integer.parseInt(curSecMinHrsArr[2]);
        curMin = Integer.parseInt(curSecMinHrsArr[1]);
        curHrs = Integer.parseInt(curSecMinHrsArr[0]);

        for(int pos=0;pos<arrivalsSplitted.length;pos++){

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

            Date parsedDate = formatter.parse(arrivalsSplitted[pos]);

            parsedDate.setYear(currentTimeDate.getYear());
            parsedDate.setMonth(currentTimeDate.getMonth());
            parsedDate.setDate(currentTimeDate.getDate());

            String[] secMinHrsArr = arrivalsSplitted[pos].split(":");
            sec = Integer.parseInt(secMinHrsArr[2]);
            min = Integer.parseInt(secMinHrsArr[1]);
            hrs = Integer.parseInt(secMinHrsArr[0]);

            if(currentTimeDate.compareTo(parsedDate)<=0){
                if(curHrs<hrs){
                    calculatedTimes.add(">1ч");
                }else{
                    int minTemp = min-curMin;
                    if(sec-curSec>40){
                        calculatedTimes.add("~"+(minTemp+1)+"м");
                    }else{
                        calculatedTimes.add("~"+minTemp+"м");
                    }
                }
            }else{
                calculatedTimes.add(arrivalsSplitted[pos]);
            }
        }

        String resultArrivals = "";

        for(int pos=0;pos<calculatedTimes.size();pos++){
            if(pos==0){
                resultArrivals = "" +calculatedTimes.get(pos);
            }else{
                resultArrivals = resultArrivals + ", "+calculatedTimes.get(pos);
            }

        }

        return resultArrivals;
    }


}
