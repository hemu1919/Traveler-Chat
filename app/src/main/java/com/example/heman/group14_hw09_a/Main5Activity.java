package com.example.heman.group14_hw09_a;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Main5Activity extends AppCompatActivity implements View.OnClickListener {

    private Menu menu;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference root, users, userRef, requestsRef, removeReqRef, requestsRef1;
    private ValueEventListener listener, listener1;
    private User user;
    private ArrayList<User> friends, unknowns, requestedUsers;
    private ArrayList<FriendRequests> requests;

    private ListView contactList;
    private LinearLayout layout;
    private TextView log_text;
    private Button add, cancel;
    private ArrayList<String> selected;
    private ContactListAdapter contacts;

    private boolean isFriend;
    public final static String KEY4 = "selected_friend";
    private  Intent intent;
    private int read = 0, read1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
        setTitle("Friend Requests");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        user = (User) getIntent().getExtras().getSerializable(MainActivity.KEY1);
        selected = (ArrayList<String>) getIntent().getExtras().getSerializable(Main4Activity.KEY4);


        friends = new ArrayList<>();
        unknowns = new ArrayList<>();
        requestedUsers = new ArrayList<>();
        log_text = (TextView) findViewById(R.id.log_text);
        layout = (LinearLayout) findViewById(R.id.layout_1);
        add = (Button) findViewById(R.id.button2);
        cancel = (Button) findViewById(R.id.button3);
        contactList = (ListView) findViewById(R.id.contact_list);


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
                setListUI(friends);
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

        listener = listener1;
        isFriend = true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_layout2, menu);
//        return true;
        return super.onCreateOptionsMenu(menu);
    }

    public void removeValueListeners() {
        if(listener.equals(listener1))
            userRef.removeEventListener(listener);
    }

    public void addValueListeners() {
        if(listener.equals(listener1))
            userRef.addValueEventListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getTitle().toString()) {
//
//        }
//        return true;
        return super.onOptionsItemSelected(item);
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
            layout.setVisibility(View.INVISIBLE);
        } else {
            log_text.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
            contacts = new ContactListAdapter(getApplicationContext(), R.layout.select_laout, updatedList);
            contacts.setSelected(selected);
            contactList.setAdapter(contacts);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button2) {
            intent = new Intent();
            intent.putExtra(KEY4, (Serializable) contacts.getSelected());
            setResult(RESULT_OK, intent);
        } else
            setResult(RESULT_CANCELED);
        finish();
    }

}
