package com.example.heman.group14_hw09_a;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class Main10Activity extends AppCompatActivity {

    TextView mt;
    ListView places_list;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference root,tripsRef;
    String tripID;
    Trip trip;
    ArrayList<Location> places;
    public static final int PLACE_REQ =100 ;
    public static final int EDIT_PLACE_REQ =101 ;
    PlacesAdapter adapter;
    private Menu menu;
    int pos;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main10);
        setTitle("Planned Places");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        tripID = getIntent().getExtras().getString(Main9Activity.KEY);
        mt = (TextView) findViewById(R.id.messageText);
        places_list = (ListView) findViewById(R.id.placesListView);
        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        tripsRef = root.child("/trips").getRef();
        tripsRef.child("/"+tripID).getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trip trip1 = dataSnapshot.getValue(Trip.class);
                //Log.d("demo", "Trip Details"+trip1.toString());
                trip1.fromJSON();
                trip = trip1;
                loadPlaces();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        places_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos =position;
                Location loc = trip.getPlacesIDs().get(pos);
                LatLng latlng = new LatLng(loc.getLat(),loc.getLng());
                LatLngBounds llb = toBounds(latlng,500);
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                builder.setLatLngBounds(llb);

                try {
                    startActivityForResult(builder.build(Main10Activity.this), EDIT_PLACE_REQ);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });


        places_list.setLongClickable(true);
        places_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                Location loc = trip.getPlacesIDs().get(pos);
                trip.removePlaceID(loc);
                updatePlaces();
                return false;
            }
        });

    }

    private void loadPlaces() {
        Log.d("demo", "Display Place");
        if (trip.getPlacesIDs().isEmpty()){
            mt.setVisibility(View.VISIBLE);
            places_list.setVisibility(View.GONE);
        }
        else{
            mt.setVisibility(View.GONE);
            places_list.setVisibility(View.VISIBLE);
            adapter = new PlacesAdapter(this,R.layout.place_layout,trip.getPlacesIDs());
            places_list.setAdapter(adapter);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_REQ) {
            //Log.d("demo", "why here?3"+data.toString());
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Log.d("demo", "I am coming here");
                String toastMsg = String.format("Place: %s", place.getName(), " added to Trip");
                LatLng latlng = place.getLatLng();
                Log.d("demo", "Latlng" + latlng);
                String name = (String) place.getName();
                Location location = new Location(name, latlng.latitude, latlng.longitude);
                trip.addPlaceID(location);
                updatePlaces();
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }

        }
        if (requestCode == EDIT_PLACE_REQ) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Log.d("demo", "I am coming here");
                String toastMsg = String.format("Place: %s", place.getName(), " added to Trip");
                LatLng latlng = place.getLatLng();
                Log.d("demo", "Latlng" + latlng);
                String name = (String) place.getName();
                Location location = new Location(name, latlng.latitude, latlng.longitude);
                trip.addPlaceID(pos,location);
                updatePlaces();
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }

        }
    }

    private void updatePlaces() {
        Log.d("demo", "Calling Update");

        trip.toPlacesJson();
        tripsRef.child("/"+tripID).child("/placeIDsJSON").setValue(trip.getPlaceIDsJSON());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_layout6, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(Main10Activity.this), PLACE_REQ);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.route:
                intent = new Intent(getApplicationContext(), Main12Activity.class);
                intent.putExtra(Main9Activity.KEY, trip.getPlacesIDs());
                startActivity(intent);
                break;
        }
        return true;
    }


    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}

