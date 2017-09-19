package com.example.heman.group14_hw09_a;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Main11Activity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    int alSize =0;

    ArrayList<com.example.heman.group14_hw09_a.Location> latLngArrayList = new ArrayList<>();
    private LocationManager mLocationMngr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main11);
        latLngArrayList = (ArrayList<com.example.heman.group14_hw09_a.Location>) getIntent().getExtras().getSerializable(Main9Activity.KEY);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setTitle("Trip Route");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.app);
        mLocationMngr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        verifyconn();

    }

    public void verifyconn() {
        if (ActivityCompat.checkSelfPermission(Main11Activity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(Main11Activity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            Location l = mLocationMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            com.example.heman.group14_hw09_a.Location loc = new com.example.heman.group14_hw09_a.Location("", l.getLatitude(), l.getLongitude());
            latLngArrayList.add(0, loc);
            latLngArrayList.add(loc);
            drawLine();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            verifyconn();

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * 30);
        mLocationRequest.setFastestInterval(1000 * 30);
        mLocationRequest.setSmallestDisplacement(0.25f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }


    }

    private void drawLine() {

        if (alSize == latLngArrayList.size())
            return;
        Log.d("demo", latLngArrayList.size()+"");
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < latLngArrayList.size(); i++) {
            LatLng point = new LatLng(latLngArrayList.get(i).getLat(), latLngArrayList.get(i).getLng());
            boundsBuilder.include(point);
            options.add(point);
        }
        alSize = latLngArrayList.size();
        LatLngBounds bounds = boundsBuilder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

        // mMap.animateCamera(CameraUpdateFactory.zoomTo(19));

        mMap.addPolyline(options);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mLocationMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS not Enabled").setMessage("Woould you like to enable GPS ?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}


