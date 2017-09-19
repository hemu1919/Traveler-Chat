package com.example.heman.group14_hw09_a;

/**
 * Created by haris on 4/30/2017.
 */

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DownloadTask extends AsyncTask<String, Void, String> {

    // Downloading data in non-ui thread
    routeListener listener;
    interface routeListener{
        public void sendRoute(PolylineOptions lineOptions);
    }

    public DownloadTask(routeListener listener) {
        this.listener = listener;
    }

    @Override

    protected String doInBackground(String... url) {

        // For storing data from web service
        String data = "";

        try{
            // Fetching the data from web service
            data = downloadUrl(url[0]);
        }catch(Exception e){
            Log.d("Background Task",e.toString());
        }
        return data;
    }
    static String getDirectionsUrl(LatLng origin, LatLng dest,ArrayList<Location> waypoints){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";



        // Output format
        String output = "json";
        String waypoints2="waypoints=optimize:true";
        for(Location loc:waypoints){
            waypoints2+="|"+loc.getLat()+","+loc.getLng();
        }
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+waypoints2+"&"+sensor;

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("demo", e.toString());
        }finally{
            try {
                iStream.close();
                urlConnection.disconnect();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return data;
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        ParserTask parserTask = new ParserTask(listener);

        // Invokes the thread for parsing the JSON data
        parserTask.execute(result);
    }

}

/** A class to parse the Google Places in JSON format */
class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

    // Parsing the data in non-ui thread
    DownloadTask.routeListener listener;

    public ParserTask(DownloadTask.routeListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try{
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            // Starts parsing data
            routes = parser.parse(jObject);
        }catch(Exception e){
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();
        ArrayList<PolylineOptions> lineoptionsList=new ArrayList<PolylineOptions>();
        lineOptions = new PolylineOptions();
        // Traversing through all the routes
        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();


            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(3);
            lineOptions.color(Color.BLUE);
            lineoptionsList.add(lineOptions);
        }

        listener.sendRoute(lineOptions);
    }
}

