package com.example.heman.group14_hw09_a;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference currentUser, root, requestsRef, requestRef1, requestRef2, tripsRef, friendRef;
    private ValueEventListener listener1, listener2, listener3 = null;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private User friend, user;
    private ImageView photo;
    private TextView name1, name2, gender, email, group_info;
    private ListView groupsList;
    private GroupListAdapter groups;
    private Button remove, accept;
    private ArrayList<Trip> trips;

    private boolean isFriend;
    private boolean isRequested = false;
    private FriendRequests request = null;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setTitle("User Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        currentUser = root.child("/users/"+firebaseUser.getUid()).getRef();
        requestsRef = root.child("/friend_requests").getRef();
        tripsRef = root.child("/trips").getRef();

        trips = new ArrayList<>();
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
                FriendRequests request1 = dataSnapshot.getValue(FriendRequests.class);
                if(request1 == null)
                    return;
                if(request1.getStatus() == FriendRequests.PENDING)
                    remove.setText("Cancel Request");
                else if(request1.getStatus() == FriendRequests.CANCELLED) {
                    requestRef1.removeValue();
                    user.deleteFriendRequest(friend.getId());
                    friendRef.child("/friendRequestsJSON").getRef().setValue(user.getFriendRequestsJSON());
                    remove.setText("Send Request");
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
                    group_info.setText("Groups Related:\n\nGroups' Info is not disclosed!");
                    groupsList.setVisibility(View.INVISIBLE);
                    friendRef.removeEventListener(listener3);
                    accept.setVisibility(View.VISIBLE);
                } else if(request.getStatus() == FriendRequests.CANCELLED) {
                    remove.setText("Send Request");
                    group_info.setText("Groups Related:\n\nGroups' Info is not disclosed!");
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

        friend = (User) getIntent().getExtras().getSerializable(MainActivity.KEY1);
        friendRef = root.child("/users/"+friend.getId()).getRef();
        isFriend = getIntent().getExtras().getBoolean(Main2Activity.KEY2);
        photo = (ImageView) findViewById(R.id.profile_icon);
        name1 = (TextView) findViewById(R.id.name1);
        name2 = (TextView) findViewById(R.id.name2);
        gender = (TextView) findViewById(R.id.gender);
        email = (TextView) findViewById(R.id.email_text);
        remove = (Button) findViewById(R.id.rem_but);
        remove.setOnClickListener(this);
        accept = (Button) findViewById(R.id.acc_but);
        accept.setOnClickListener(this);

        groupsList = (ListView) findViewById(R.id.groups_list);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Trip> currentList = groups.getProfiles();
                intent = new Intent(getApplicationContext(), Main6Activity.class);
                intent.putExtra(Main4Activity.KEY5, currentList.get(position));
                intent.putExtra(Main2Activity.KEY2, user.containsTrip(currentList.get(position).getId()));
                startActivity(intent);
            }
        });
        group_info = (TextView) findViewById(R.id.group_info);

        Picasso.with(this).load(friend.getPhotoURL()).into(photo);
        name1.setText(friend.getFirstName());
        name2.setText(friend.getLastName());
        gender.setText(friend.getGender());
        email.setText(friend.getEmail());

    }

    private void preProcessRequests() {
        requestRef2 = requestsRef.child("/"+user.getId()).getRef().child("/"+friend.getId()).getRef();
        requestRef2.addValueEventListener(listener2);
        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trips.clear();
                friend = dataSnapshot.getValue(User.class);
                friend.fromJSON();
                final ArrayList<String> tripsList = friend.getOwnedTrips();
                for(int i=0;i<tripsList.size();i++) {
                    tripsRef.child("/"+tripsList.get(i)).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            trip.fromJSON();
                            if(!trips.contains(trip))
                                trips.add(trip);
                            if(trips.size() == tripsList.size())
                                setListUI(trips);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(tripsList.size() == 0)
                    setListUI(trips);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        if(!isFriend && !user.containsFriend(friend.getId())) {
            if(user.containsFriendRequests(friend.getId())) {
                requestRef1 = requestsRef.child("/"+friend.getId()).getRef().child("/"+user.getId()).getRef();
                requestRef1.addValueEventListener(listener1);
            }
            else {
                remove.setText("Send Request");
                group_info.setText("Groups Related:\n\nGroups' Info is not disclosed!");
                groupsList.setVisibility(View.INVISIBLE);
                friendRef.removeEventListener(listener3);
            }
        }
        else {
            friendRef.addValueEventListener(listener3);
            remove.setText("Remove");
        }
    }

    @Override
    public void onClick(View v) {
        switch (((Button) v).getText().toString()) {
            case "Remove":
                try {
                    user.removeFriend(friend.getId());
                    currentUser.child("/friendsJSON").getRef().setValue(user.getFriendsJSON()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            root.child("/remove_requests/"+friend.getId()+"/"+user.getId()).setValue(1);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Send Request":
                FriendRequests request1 = new FriendRequests(user.getId());
                user.addFriendRequest(friend.getId());
                user.toMap();
                Map<String, Object> map = new HashMap<>();
                map.put("/friend_requests/"+friend.getId()+"/"+user.getId(), request1.toMap());
                map.put("/users/"+user.getId()+"/friendRequestsJSON", user.getFriendRequestsJSON());
                root.updateChildren(map);
                requestRef1 = requestsRef.child("/"+friend.getId()).getRef().child("/"+user.getId()).getRef();
                group_info.setText("Groups Related:\n\nGroups' Info is not disclosed!");
                groupsList.setVisibility(View.INVISIBLE);
                friendRef.removeEventListener(listener3);
                remove.setText("Cancel Request");
                break;
            case "Cancel Request":
                if(isRequested) {
                    isRequested = false;
                    request.setStatus(FriendRequests.CANCELLED);
                    requestRef2.setValue(request);
                }
                else {
                    requestRef1.removeValue();
                    user.deleteFriendRequest(friend.getId());
                    currentUser.child("/friendRequestsJSON").getRef().setValue(user.getFriendRequestsJSON());
                }
                remove.setText("Send Request");
                break;
            case "Accept Request":
                if(isRequested) {
                    isRequested = false;
                    user.addFriend(friend.getId());
                    user.toMap();
                    request.setStatus(FriendRequests.ACCEPTED);
                    requestRef2.setValue(request);
                    currentUser.child("/friendsJSON").getRef().setValue(user.getFriendsJSON());
                }
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

    public void setListUI(List updatedList) {
        group_info.setText("Groups Related:");
        if(updatedList.size() == 0) {
            group_info.setText(group_info.getText().toString() + "\n\nNo Groups yet created!");
        }
        groupsList.setVisibility(View.VISIBLE);
        groups = new GroupListAdapter(getApplicationContext(), R.layout.group_layout, updatedList);
        groupsList.setAdapter(groups);
    }
}
