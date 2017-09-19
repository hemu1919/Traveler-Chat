package com.example.heman.group14_hw09_a;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Main12Activity extends FragmentActivity implements OnMapReadyCallback,DownloadTask.routeListener {

    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    private LocationManager mLocationMngr;
    LatLng latlng;
    private ProgressDialog dialog;

    ArrayList<com.example.heman.group14_hw09_a.Location> latLngArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main12);
        latLngArrayList = (ArrayList<com.example.heman.group14_hw09_a.Location>) getIntent().getExtras().getSerializable(Main9Activity.KEY);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Loading..!");
        dialog.show();

        // Initializing
        markerPoints = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        fm.getMapAsync(this);

        setTitle("Trip Route");
        mLocationMngr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        processing();

    }

    private void processing() {

        if (map != null) {

             if (ActivityCompat.checkSelfPermission(Main12Activity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(Main12Activity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                android.location.Location l = mLocationMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //LatLng latlng = new LatLng(l.getLatitude(), l.getLongitude());
                 //android.location.Location l1 = map.getMyLocation();
                 //latlng = new LatLng(l1.getLatitude(),l1.getLongitude());
                 latlng = new LatLng(35.3059849,-80.730865);
                 map.setMyLocationEnabled(true);
                String url = DownloadTask.getDirectionsUrl(latlng, latlng, latLngArrayList);
                DownloadTask downloadTask = new DownloadTask(this);
                downloadTask.execute(url);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);

            }

        }

    }


    @Override
    public void sendRoute(PolylineOptions lineOptions) {
        if (map != null && lineOptions != null) {
            //PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(latlng);
            MarkerOptions options = new MarkerOptions();
            options.title("Your Location");
            options.position(latlng);
            map.addMarker(options);
            for (int i = 0; i < latLngArrayList.size(); i++) {
                Location loc = latLngArrayList.get(i);
                LatLng point = new LatLng(loc.getLat(),loc.getLng());
                boundsBuilder.include(point);
                options.title(loc.getName());
                options.position(point);
                map.addMarker(options);

            }
            LatLngBounds bounds = boundsBuilder.build();

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));

            map.addPolyline(lineOptions);
            dialog.dismiss();
        }

    }
}
