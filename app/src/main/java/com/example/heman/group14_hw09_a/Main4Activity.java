package com.example.heman.group14_hw09_a;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.net.URI;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main4Activity extends AppCompatActivity implements View.OnClickListener{

    public static final String KEY4 = "selected";
    private static final short CODE = 100;
    private Menu menu;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference root, users, userRef, requestsRef, removeReqRef, requestsRef1, tripsRef, join_confiRef;
    private ValueEventListener listener, listener1, listener2, listener3, listener4, listener5;
    private User user;
    private ArrayList<Trip> trips;
    private ArrayList<User> unknowns, requestedUsers;
    private ArrayList<FriendRequests> requests;

    private FirebaseStorage firebaseStorage;
    private StorageReference profilepicRef;

    private ListView contactList;
    private TextView log_text;
    private GroupListAdapter groups;
    private ContactListAdapter contacts;
    private LinearLayout create_layout;
    private ProgressDialog dialog;

    private boolean isFriend;
    public final static String KEY5 = "trip";
    private  Intent intent;
    private int read = 0, read1 = 0;
    private ArrayList<String> selected;

    private Uri fileURI;



    private final int CODE1 = 1000, CODE2 = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        setTitle("Trips");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        user = (User) getIntent().getExtras().getSerializable(MainActivity.KEY1);
        dialog = new ProgressDialog(this);

        firebaseStorage = FirebaseStorage.getInstance();
        profilepicRef = firebaseStorage.getReference().child("/profilePics");

        trips = new ArrayList<>();
        unknowns = new ArrayList<>();
        requestedUsers = new ArrayList<>();
        log_text = (TextView) findViewById(R.id.log_text);
        contactList = (ListView) findViewById(R.id.contact_list);
        create_layout = (LinearLayout) findViewById(R.id.cre_layout);
        selected = new ArrayList<>();
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Trip> currentList = groups.getProfiles();
                intent = new Intent(getApplicationContext(), Main6Activity.class);
                intent.putExtra(KEY5, currentList.get(position));
                intent.putExtra(Main2Activity.KEY2, user.containsTrip(currentList.get(position).getId()));
                startActivity(intent);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        users = root.child("/users").getRef();
        userRef = users.child("/"+user.getId()).getRef();
        tripsRef = root.child("/trips").getRef();
        join_confiRef = root.child("/join_confirmations/"+user.getId()).getRef();
        requestsRef = root.child("/friend_requests").getRef().child("/"+user.getId()).getRef();
        removeReqRef = root.child("/remove_requests").getRef().child("/"+user.getId()).getRef();

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trips.clear();
                if(read != 2) {
                    setListUI(trips);
                    return;
                }
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                final ArrayList<String> tripsList = user.getTripsList();
                for(int i=0;i<tripsList.size();i++) {
                    tripsRef.child("/"+tripsList.get(i)).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            trip.fromJSON();
                            if(!trips.contains(trip))
                                trips.add(trip);
                            if(tripsList.size() == trips.size())
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

        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unknowns.clear();
                for(DataSnapshot userDataSnapshot : dataSnapshot.getChildren())  {
                    if(userDataSnapshot.getKey().equals(user.getId()) || user.containsFriend(userDataSnapshot.getKey()))
                        continue;
                    User user = userDataSnapshot.getValue(User.class);
                    user.fromJSON();
                    unknowns.add(user);
                }
                setListUI(unknowns);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    read++;
                    return;
                }
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        user.addJoinedTrip(userSnapshot.getKey());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                join_confiRef.removeValue();
                userRef.child("/joinedTripsJSON").getRef().setValue(user.getJoinedTripsJSON());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    read++;
                    return;
                }
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        user.removeFriend(userSnapshot.getKey());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                userRef.child("/friendsJSON").getRef().setValue(user.getFriendsJSON());
                removeReqRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        read++;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        read++;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listener5 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                final ArrayList<String> friendRequestsList = user.getFriendRequests();
                for(int i=0;i<friendRequestsList.size();i++) {
                    String key = friendRequestsList.get(i);
                    requestsRef1 = root.child("/friend_requests/"+key+"/"+user.getId()).getRef();
                    requestsRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            FriendRequests request2 = dataSnapshot.getValue(FriendRequests.class);
                            if(request2 == null)
                                return;
                            if(request2.getStatus() == FriendRequests.ACCEPTED) {
                                String key2 = dataSnapshot.getRef().getParent().getKey();
                                root.child("/friend_requests/"+key2+"/"+user.getId()).getRef().removeValue();
                                if(!user.containsFriend(key2))
                                    user.addFriend(key2);
                                user.deleteFriendRequest(key2);
                                read1--;
                                userRef.child("/friendRequestsJSON").setValue(user.getFriendRequestsJSON()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        read1++;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        read1++;
                                    }
                                });
                                read1--;
                                userRef.child("/friendsJSON").setValue(user.getFriendsJSON()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        read1++;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        read1++;
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                while(read1 != 0);
                read++;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listener = listener1;
        isFriend = true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout3, menu);
        return true;
    }

    public void removeValueListeners() {
        removeReqRef.removeEventListener(listener4);
        userRef.removeEventListener(listener5);
        join_confiRef.removeEventListener(listener3);
        if(listener == null) {
            return;
        }
        if(listener.equals(listener1))
            userRef.removeEventListener(listener);
        else if(listener.equals(listener2))
            users.removeEventListener(listener);
        else if(listener.equals(listener3))
            requestsRef.removeEventListener(listener);
    }

    public void addValueListeners() {
        removeReqRef.addValueEventListener(listener4);
        userRef.addListenerForSingleValueEvent(listener5);
        join_confiRef.addListenerForSingleValueEvent(listener3);
        if(listener == null) {
            return;
        }
        if(listener.equals(listener1))
            userRef.addValueEventListener(listener);
        else if(listener.equals(listener2))
            users.addValueEventListener(listener);
        else if(listener.equals(listener3))
            requestsRef.addValueEventListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "Search New Trips":
//                if(listener == null) {
//                    listener = listener2;
//                    create_layout.setVisibility(View.INVISIBLE);
//                }
//                if(listener.equals(listener2))
//                    break;
//                read = 0;
//                removeValueListeners();
//                listener = listener2;
//                isFriend = false;
//                addValueListeners();
                break;
            case "Trips":
                if(listener == null) {
                    listener = listener1;
                    create_layout.setVisibility(View.INVISIBLE);
                }
                else if(listener.equals(listener1))
                    break;
                read = 0;
                removeValueListeners();
                isFriend = true;
                listener = listener1;
                addValueListeners();
                break;
            case "Create Trips":
                contactList.setVisibility(View.INVISIBLE);
                log_text.setVisibility(View.INVISIBLE);
                create_layout.setVisibility(View.VISIBLE);
                removeValueListeners();
                listener = null;
//                read = 0;
//                removeValueListeners();
//                isFriend = false;
//                listener = listener3;
//                addValueListeners();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        addValueListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        read = 0;
        removeValueListeners();
    }

    public void setListUI(List updatedList) {
        if(updatedList.size() == 0) {
            log_text.setText("Currently no Trip information exists!");
            log_text.setVisibility(View.VISIBLE);
            contactList.setVisibility(View.INVISIBLE);
        } else {
            log_text.setVisibility(View.INVISIBLE);
            contactList.setVisibility(View.VISIBLE);
            groups = new GroupListAdapter(getApplicationContext(), R.layout.group_layout, updatedList);
            contactList.setAdapter(groups);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.add_frien:
                intent = new Intent(Main4Activity.this, Main5Activity.class);
                intent.putExtra(MainActivity.KEY1, user);
                intent.putExtra(KEY4, selected);
                startActivityForResult(intent, CODE1);
                break;
            case R.id.upload:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, CODE);
                break;
            case R.id.cre_but:
                EditText et1, et2;
                et1 = (EditText) findViewById(R.id.trip_name);
                et2 = (EditText) findViewById(R.id.location);
                if(et1.length() == 0 || et2.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Invalid entries!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.setTitle("Creating..!");
                dialog.setCancelable(false);
                dialog.show();
                final Trip trip = new Trip(user.getId());
                trip.setOwnerName(user.getFirstName()+" "+user.getLastName());
                trip.setName(et1.getText().toString());
                trip.setLocation(et2.getText().toString());
                trip.setPeopleIDs(selected);
                String key = tripsRef.push().getKey();
                try {
                    user.addOwnedTrip(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for(String receiverID : selected)
                    root.child("/join_confirmations/"+receiverID+"/"+key).getRef().setValue(0);
                String chatID = root.child("/chatrooms").getRef().push().getKey();
                final ChatRoom chat = new ChatRoom(chatID);
                trip.setChatRoom(chatID);
                trip.setId(key);
                if(fileURI != null) {
                    profilepicRef.child("/" + trip.getId() + ".png").putFile(fileURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            trip.setPhotoURL(taskSnapshot.getDownloadUrl().toString());
                            createTrip(trip, chat);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            createTrip(trip, chat);
                        }
                    });
                } else
                    createTrip(trip, chat);
                break;
            case R.id.can_but:
                selected.clear();
                create_layout.setVisibility(View.INVISIBLE);
                read=0;
                listener = listener1;
                addValueListeners();
                break;
        }
    }

    private void createTrip(Trip trip, ChatRoom chat) {
        Map<String, Object> map = new HashMap<>();
        map.put("/trips/"+trip.getId(), trip.toMap());
        map.put("/users/"+user.getId()+"/ownedTripsJSON", user.getOwnedTripsJSON());
        map.put("/chatrooms/"+chat.getId(), chat.toMap());
        root.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                selected.clear();
                dialog.dismiss();
                create_layout.setVisibility(View.INVISIBLE);
                read = 0;
                listener = listener1;
                addValueListeners();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE1 && resultCode == RESULT_OK) {
            selected = (ArrayList<String>) data.getExtras().getSerializable(Main5Activity.KEY4);
            Log.d("demo", selected.toString());
        } else if(requestCode == CODE) {
            if(resultCode == RESULT_OK) {
                fileURI = data.getData();
                ((Button)findViewById(R.id.upload)).setText("Cahnge Photo");
            }
        }
    }

}
