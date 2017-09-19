package com.example.heman.group14_hw09_a;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private Menu menu;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference root, users, userRef, requestsRef, removeReqRef, requestsRef1;
    private ValueEventListener listener, listener1, listener2, listener3, listener4, listener5;
    private User user;
    private ArrayList<User> friends, unknowns, requestedUsers;
    private ArrayList<FriendRequests> requests;

    private ListView contactList;
    private TextView log_text;
    private ContactListAdapter contacts;

    private boolean isFriend;
    public final static String KEY2 = "friend_flag";
    private  Intent intent;
    private int read = 0, read1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setTitle("Contacts");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        user = (User) getIntent().getExtras().getSerializable(MainActivity.KEY1);

        friends = new ArrayList<>();
        unknowns = new ArrayList<>();
        requestedUsers = new ArrayList<>();
        log_text = (TextView) findViewById(R.id.log_text);
        contactList = (ListView) findViewById(R.id.contact_list);
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<User> currentList = contacts.getProfiles();
                intent = new Intent(getApplicationContext(), Main3Activity.class);
                intent.putExtra(MainActivity.KEY1, currentList.get(position));
                intent.putExtra(KEY2, isFriend);
                startActivity(intent);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        users = root.child("/users").getRef();
        userRef = users.child("/"+user.getId()).getRef();
        requestsRef = root.child("/friend_requests").getRef().child("/"+user.getId()).getRef();
        removeReqRef = root.child("/remove_requests").getRef().child("/"+user.getId()).getRef();

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends.clear();
                if(read != 2) {
                    setListUI(friends);
                    return;
                }
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                final ArrayList<String> friendsList = user.getFriendsList();
                for(int i=0;i<friendsList.size();i++) {
                    users.child("/"+friendsList.get(i)).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User friend = dataSnapshot.getValue(User.class);
                            friend.fromJSON();
                            if(!friends.contains(friend))
                                friends.add(friend);
                            setListUI(friends);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(friendsList.size() == 0)
                    setListUI(friends);
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
            public void onDataChange(final DataSnapshot dataSnapshot1) {
                requestedUsers.clear();
                for(DataSnapshot userSnapshot : dataSnapshot1.getChildren()) {
                    FriendRequests request2 = userSnapshot.getValue(FriendRequests.class);
                    if(request2.getStatus() == FriendRequests.CANCELLED || request2.getStatus() == FriendRequests.ACCEPTED)
                        continue;
                    users.child("/"+userSnapshot.getKey()).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User requestedUser = dataSnapshot.getValue(User.class);
                            requestedUser.fromJSON();
                            requestedUsers.add(requestedUser);
                            if(requestedUsers.size() == dataSnapshot1.getChildrenCount())
                                setListUI(requestedUsers);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(requestedUsers.size() == 0)
                    setListUI(requestedUsers);
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
        getMenuInflater().inflate(R.menu.menu_layout2, menu);
        return true;
    }

    public void removeValueListeners() {
        removeReqRef.removeEventListener(listener4);
        userRef.removeEventListener(listener5);
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
            case "Search New Contacts":
                if(listener.equals(listener2))
                    break;
                read = 0;
                removeValueListeners();
                listener = listener2;
                isFriend = false;
                addValueListeners();
                break;
            case "Contacts":
                if(listener.equals(listener1))
                    break;
                read = 0;
                removeValueListeners();
                isFriend = true;
                listener = listener1;
                addValueListeners();
                break;
            case "New Requests":
                if(listener.equals(listener3))
                    break;
                read = 0;
                removeValueListeners();
                isFriend = false;
                listener = listener3;
                addValueListeners();
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
            log_text.setText("Currently no contact information exists!");
            log_text.setVisibility(View.VISIBLE);
            contactList.setVisibility(View.INVISIBLE);
        } else {
            log_text.setVisibility(View.INVISIBLE);
            contactList.setVisibility(View.VISIBLE);
            contacts = new ContactListAdapter(getApplicationContext(), R.layout.contact_layout, updatedList);
            contactList.setAdapter(contacts);
        }
    }

}
