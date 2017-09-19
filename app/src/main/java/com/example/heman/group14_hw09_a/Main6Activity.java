package com.example.heman.group14_hw09_a;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main6Activity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference currentUser, root, requestsRef, requestRef1, requestRef2, tripsRef, friendRef;
    private ValueEventListener listener1, listener2, listener3 = null;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private Intent intent;

    private User friend, user;
    private ImageView photo;
    private TextView name1, name2, gender, email, group_info;
    private ListView groupsList;
    private ContactListAdapter contacts;
    private Button remove, accept;
    private ArrayList<User> users;
    private Trip trip;

    private boolean isFriend, isJoined;
    private boolean isRequested = false;
    private Menu menu;
    private FriendRequests request = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);
        setTitle("Trip Details");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        currentUser = root.child("/users/"+firebaseUser.getUid()).getRef();
        requestsRef = root.child("/friend_requests").getRef();

        users = new ArrayList<>();
        
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                preProcessRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                trip = dataSnapshot.getValue(Trip.class);
                trip.fromJSON();
                final ArrayList<String> members = trip.getMembers();
                Log.d("demo", members.toString());
                for(String id : members) {
                    root.child("/users/" + id).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            User temp = dataSnapshot1.getValue(User.class);
                            temp.fromJSON();
                            users.add(temp);
                            if (users.size() == members.size())
                                setListUI(users);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(members.size() == 0) {
                    setListUI(users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                request = null;
                request = dataSnapshot.getValue(FriendRequests.class);
                if(request == null) {
                    isRequested = false;
                    accept.setVisibility(View.GONE);
                    return;
                }
                if(request.getStatus() == FriendRequests.PENDING) {
                    isRequested = true;
                    request = dataSnapshot.getValue(FriendRequests.class);
                    remove.setText("Cancel Request");
                    group_info.setText("Groups Related: Groups' Info is not disclosed!");
                    groupsList.setVisibility(View.INVISIBLE);
                    friendRef.removeEventListener(listener3);
                    accept.setVisibility(View.VISIBLE);
                } else if(request.getStatus() == FriendRequests.CANCELLED) {
                    remove.setText("Send Request");
                    group_info.setText("Groups Related: Groups' Info is not disclosed!");
                    groupsList.setVisibility(View.INVISIBLE);
                    friendRef.removeEventListener(listener3);
                    accept.setVisibility(View.GONE);
                } else if(request.getStatus() == FriendRequests.ACCEPTED) {
                    remove.setText("Remove");
                    accept.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        trip = (Trip) getIntent().getExtras().getSerializable(Main4Activity.KEY5);
        //friendRef = root.child("/users/"+friend.getId()).getRef();
        tripsRef = root.child("/trips/"+trip.getId()).getRef();
        isJoined = getIntent().getExtras().getBoolean(Main2Activity.KEY2);
        photo = (ImageView) findViewById(R.id.profile_icon);
        name1 = (TextView) findViewById(R.id.name1);
        name2 = (TextView) findViewById(R.id.name2);
        gender = (TextView) findViewById(R.id.gender);
        remove = (Button) findViewById(R.id.rem_but);
        remove.setOnClickListener(this);

        groupsList = (ListView) findViewById(R.id.groups_list);
        group_info = (TextView) findViewById(R.id.group_info);

        Picasso.with(this).load(trip.getPhotoURL()).into(photo);
        name1.setText(trip.getName());
        name2.setText(trip.getLocation());
        gender.setText(trip.getOwnerName());

    }

    private void preProcessRequests() {
//        requestRef2 = requestsRef.child("/"+user.getId()).getRef().child("/"+friend.getId()).getRef();
//        requestRef2.addValueEventListener(listener2);
//        listener3 = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                trips.clear();
//                friend = dataSnapshot.getValue(User.class);
//                friend.fromJSON();
//                final ArrayList<String> tripsList = friend.getOwnedTrips();
//                for(int i=0;i<tripsList.size();i++) {
//                    tripsRef.child("/"+tripsList.get(i)).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            Trip trip = dataSnapshot.getValue(Trip.class);
//                            trip.fromJSON();
//                            if(!trips.contains(trip))
//                                trips.add(trip);
//                            if(trips.size() == tripsList.size())
//                                setListUI(trips);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//                if(tripsList.size() == 0)
//                    setListUI(trips);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
        tripsRef.addValueEventListener(listener1);
        isJoined = user.containsTrip(trip.getId());
        if(isJoined) {
            if(user.containsOwnedTrip(trip.getId())) {
//                requestRef1 = requestsRef.child("/"+friend.getId()).getRef().child("/"+user.getId()).getRef();
//                requestRef1.addValueEventListener(listener1);
                remove.setText("Delete");
            }
            else if(user.containsJoinedTrip(trip.getId())){
                remove.setText("Exit");
//                group_info.setText("Groups Related: Groups' Info is not disclosed!");
//                groupsList.setVisibility(View.INVISIBLE);
//                friendRef.removeEventListener(listener3);
            }
        }
        else {
//            friendRef.addValueEventListener(listener3);
            remove.setText("Join");
        }
    }

    @Override
    public void onClick(View v) {
        switch (((Button) v).getText().toString()) {
            case "Exit":
                user.deleteJoinedTrip(trip.getId());
                trip.removePeopleIDs(user.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("/users/"+user.getId()+"/joinedTripsJSON",user.getJoinedTripsJSON());
                map.put("/trips/"+trip.getId()+"/peopleIDsJSON",trip.getPeopleIDsJSON());
                root.updateChildren(map);
                finish();
                break;
            case "Delete":
                trip.setOwnerID("");
                user.deleteOwnedTrip(trip.getId());
                map = new HashMap<>();
                map.put("/trips/"+trip.getId(), trip.toMap());
                map.put("/chatrooms/"+trip.getChatRoom()+"/isAccessible", false);
                map.put("/users/"+user.getId()+"/ownedTripsJSON", user.getOwnedTrips());
                root.updateChildren(map);
                finish();
                break;
            case "Join":
                try {
                    user.addJoinedTrip(trip.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                trip.addPeopleIDs(user.getId());
                map = new HashMap<>();
                map.put("/users/"+user.getId()+"/joinedTripsJSON",user.getJoinedTripsJSON());
                map.put("/trips/"+trip.getId()+"/peopleIDsJSON",trip.getPeopleIDsJSON());
                root.updateChildren(map);
                remove.setText("Exit");
                break;
        }
    }

//    @Override
//    protected void onResume() {
////        if(photo.getTag() != null)
////            friend = (User) photo.getTag();
//        if(listener3!=null)
//            currentUser.addValueEventListener(listener3);
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
////        if(!friend.equals(user))
////            photo.setTag(friend);
//        if(listener3!=null)
//            currentUser.removeEventListener(listener3);
//        super.onPause();
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_layout4, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat:
                if(user.containsTrip(trip.getId())) {
                    intent = new Intent(getApplicationContext(), Main9Activity.class);
                    intent.putExtra(MainActivity.KEY1, trip);
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), "Chatroom can't be accessed!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tripplan:
                if(user.containsTrip(trip.getId())) {
                    Intent intent1 = new Intent(getApplicationContext(), Main10Activity.class);
                    intent1.putExtra(Main9Activity.KEY, trip.getId());
                    startActivity(intent1);
                } else
                    Toast.makeText(getApplicationContext(), "Trip Plan can't be accessed!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.settings:
                if(user.containsOwnedTrip(trip.getId())) {
                    intent = new Intent(getApplicationContext(), Main8Activity.class);
                    intent.putExtra(Main8Activity.KEY1, trip);
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), "You do not have permission!", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    public void setListUI(List updatedList) {
        group_info.setText(getString(R.string.frnd_label));
        if(updatedList.size() == 0) {
            group_info.setText(group_info.getText().toString() + " No Groups yet created!");
        }
        groupsList.setVisibility(View.VISIBLE);
        contacts = new ContactListAdapter(getApplicationContext(), R.layout.contact_layout, updatedList);
        groupsList.setAdapter(contacts);
    }

//    @Override
//    protected void onResume() {
//        tripsRef.addValueEventListener(listener1);
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        tripsRef.removeEventListener(listener1);
//        super.onPause();
//    }
}
