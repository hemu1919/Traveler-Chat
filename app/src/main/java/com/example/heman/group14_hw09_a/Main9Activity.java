package com.example.heman.group14_hw09_a;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main9Activity extends AppCompatActivity {
    TextView userNameId;
    ImageView imageButton;
    ListView listView;
    RelativeLayout rL;
    RelativeLayout rl1;
    int position;
    Message message;
    AlertDialog alert;
    String flag1 = "ABC", flag2 = "AC";
    private Intent intent1;

    private String chatID;
    private Trip trip;
    String user_id;
    User user;
    private Menu menu;
    public static final String KEY ="TripPlan" ;

    EditText et1;
    ImageView  im1, im2;
    FirebaseDatabase fdb = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    DatabaseReference com;
    DatabaseReference user_ref;
    private ProgressDialog dialog;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser fu = mAuth.getCurrentUser();
    FirebaseStorage fs = FirebaseStorage.getInstance();
    StorageReference ref1;

    CustomAdapter customAdapter;

    ArrayList<Message> messages, comments, m;

    FirebaseAuth.AuthStateListener fa = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if(fu==null){
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main9);

        trip = (Trip) getIntent().getExtras().getSerializable(MainActivity.KEY1);
        setTitle(trip.getName());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        chatID = trip.getChatRoom();
        user_id = fu.getUid();
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Loading...");
        dialog.show();

        fdb = FirebaseDatabase.getInstance();
        ref = fdb.getReference("/chatrooms/"+chatID+"/message");
        com = fdb.getReference("/chatrooms/"+chatID+"/comment");
        user_ref = fdb.getReference("users/"+user_id);

        listView = (ListView) findViewById(R.id.listView);
        rL = (RelativeLayout) findViewById(R.id.rL);

        FirebaseDatabase.getInstance().getReference().child("/chatrooms/"+chatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().contains("true"))
                    attach();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                Log.d("Demo", user.toString());
                dialog.dismiss();
                preProcess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Runnable r = new Runnable() {

            @Override
            public void run() {
                while(true) {
                    while (!(flag2.equals("ABC") && flag1.equals("ABC"))) ;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            set();
                        }
                    });
                    while ((flag2.equals("ABC") && flag1.equals("ABC"))) ;
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public void attach() {
        rl1 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.messagelayout,rL,true);

        et1 = (EditText) rl1.findViewById(R.id.mt1);
        im1 = (ImageView) rl1.findViewById(R.id.addPhoto1);
        im2 = (ImageView) rl1.findViewById(R.id.addMessage1);

        im1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                if(intent.resolveActivity(getPackageManager())!=null)
                    startActivityForResult(intent, 100);
                et1.setText("");
            }
        });

        im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et1!=null && et1.length()>0){
                    String key = ref.push().getKey();
                    String name =user.getFirstName()+" "+user.getLastName();
                    Message message = new Message(key, name,et1.getText().toString(), null, System.currentTimeMillis());
                    Map<String, Object> cu  = new HashMap<String, Object>();
                    cu.put("/"+key, message.toMap());
                    ref.updateChildren(cu);
                    et1.setText("");
                }
            }
        });
    }

    public void preProcess() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flag2="AC";
                messages = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Message m = ds.getValue(Message.class);
                    Log.d("flag", m.containsUser(user.getId())+"");
                    if(!m.containsUser(user.getId()))
                        messages.add(m);
                    Log.d("test", m.toString());
                }
                flag2="ABC";
                Log.d("c","o2");
                /*if (flag1.equals("ABC") && flag2.equals("ABC")){
                    customAdapter = new CustomAdapter(ChatActivity.this, R.layout.messagelayout,messages);
                    listView.setAdapter(customAdapter);
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(fa);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(fa!=null){
            mAuth.removeAuthStateListener(fa);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 100 && resultCode == RESULT_OK && data!=null) {
            Uri uri = data.getData();
            message = new Message(null,fu.getDisplayName(),null, null, System.currentTimeMillis());
            fs.getReference("/images/"+ UUID.randomUUID()).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    message.setImageURL(taskSnapshot.getMetadata().getDownloadUrl().toString());
                    message.setName(user.getFirstName()+" "+user.getLastName());
                    String key = ref.push().getKey();
                    message.setId(key);
                    Map<String, Object> cu  = new HashMap<String, Object>();
                    cu.put("/"+key, message.toMap());
                    ref.updateChildren(cu);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("demo", e.getMessage());
                }
            });
        }
    }

    public void set() {
        m = new ArrayList<>();
        for(Message m1 : messages) {
            m.add(m1);
            if(comments != null)
                for(Message m2 : comments) {
                    if(m2.getId().equals(m1.getId()))
                        m.add(m2);
                }
        }
        customAdapter = new CustomAdapter(Main9Activity.this, R.layout.messagelayout,m, trip,user);
        listView.setAdapter(customAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_layout5, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.plan:
                intent1 = new Intent(getApplicationContext(), Main10Activity.class);
                intent1.putExtra(KEY, trip.getId());
                startActivity(intent1);
                break;
                    }
        return true;
    }

}
