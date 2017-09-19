package com.example.heman.group14_hw09_a;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main8Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int CODE = 100;
    private static final int CODE1 = 101;
    public static final String KEY1 = "trip";
    private ProgressDialog dialog;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef, root;

    private User user;
    private Trip trip;
    private Intent intent;
    private Uri fileURI;
    private ArrayList<String> selected;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private FirebaseStorage firebaseStorage;
    private StorageReference profilepicRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);
        setTitle("Update Trip Details");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Loading..!");
        dialog.show();

        trip = (Trip) getIntent().getExtras().getSerializable(KEY1);
        selected = trip.getPeopleIDs();

        firebaseStorage = FirebaseStorage.getInstance();
        profilepicRef = firebaseStorage.getReference().child("/profilePics");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        userRef = firebaseDatabase.getReference().child("/users/"+firebaseUser.getUid()).getRef();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                EditText et1, et2;
                et1 = (EditText) findViewById(R.id.trip_name);
                et2 = (EditText) findViewById(R.id.location);
                et1.setText(trip.getName());
                et2.setText(trip.getLocation());
                ((Button)findViewById(R.id.cre_but)).setText("Update");
                ((Button)findViewById(R.id.upload)).setText("Change Photo");
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_frien:
                intent = new Intent(Main8Activity.this, Main5Activity.class);
                intent.putExtra(MainActivity.KEY1, user);
                intent.putExtra(Main4Activity.KEY4, selected);
                startActivityForResult(intent, CODE);
                break;
            case R.id.upload:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, CODE1);
                break;
            case R.id.cre_but:
                EditText et1, et2;
                et1 = (EditText) findViewById(R.id.trip_name);
                et2 = (EditText) findViewById(R.id.location);
                if(et1.length() == 0 || et2.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Invalid entries!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.show();
                trip.setName(et1.getText().toString());
                trip.setLocation(et2.getText().toString());
                trip.setPeopleIDs(selected);
                for(String receiverID : selected)
                    root.child("/join_confirmations/"+receiverID+"/"+trip.getId()).getRef().setValue(0);
                String chatID = root.child("/chatrooms").getRef().push().getKey();
                if(fileURI != null) {
                    profilepicRef.child("/" + trip.getId() + ".png").putFile(fileURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            trip.setPhotoURL(taskSnapshot.getDownloadUrl().toString());
                            createTrip(trip);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            createTrip(trip);
                        }
                    });
                } else
                    createTrip(trip);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE && resultCode == RESULT_OK) {
            selected = (ArrayList<String>) data.getExtras().getSerializable(Main5Activity.KEY4);
            Log.d("demo", selected.toString());
        } else if(requestCode == CODE1) {
            if(resultCode == RESULT_OK) {
                fileURI = data.getData();
                ((Button)findViewById(R.id.upload)).setText("Cahnge Photo");
            }
        }
    }

    private void createTrip(Trip trip) {
        Map<String, Object> map = new HashMap<>();
        map.put("/trips/"+trip.getId(), trip.toMap());
        root.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.show();
                finish();
            }
        });
    }

}
