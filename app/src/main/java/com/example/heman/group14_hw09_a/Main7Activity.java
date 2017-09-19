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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;

public class Main7Activity extends AppCompatActivity {

    private static final int CODE = 100;
    private ProgressDialog dialog;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;

    private User user;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ImageView photo;
    private TextView name1, name2, gender, email, group_info;
    private Button remove;

    private FirebaseStorage firebaseStorage;
    private StorageReference profilepicRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        setTitle("Update Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Loading..!");
        dialog.show();

        firebaseStorage = FirebaseStorage.getInstance();
        profilepicRef = firebaseStorage.getReference().child("/profilePics");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        photo = (ImageView) findViewById(R.id.profile_icon);
        name1 = (TextView) findViewById(R.id.name1);
        name2 = (TextView) findViewById(R.id.name2);
        gender = (TextView) findViewById(R.id.gender);
        email = (TextView) findViewById(R.id.email_text);
        remove = (Button) findViewById(R.id.rem_but);
        group_info = (TextView) findViewById(R.id.updatePhoto);
        group_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, CODE);
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name1.length() == 0 || name2.length() == 0 || gender.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Invalid entries!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.setTitle("Updating..!");
                dialog.show();
                user.setFirstName(name1.getText().toString());
                user.setLastName(name2.getText().toString());
                user.setGender(gender.getText().toString());

                userRef.setValue(user.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        finish();
                    }
                });
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference().child("/users/"+firebaseUser.getUid()).getRef();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                Picasso.with(Main7Activity.this).load(user.getPhotoURL()).into(photo);
                name1.setText(user.getFirstName());
                name2.setText(user.getLastName());
                gender.setText(user.getGender());
                email.setText(user.getEmail());
                remove.setText("Update");
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout1, menu);
        menu.setGroupVisible(R.id.group1, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                Toast.makeText(getApplicationContext(), "Logged out Successfully", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CODE) {
            if(resultCode == RESULT_OK) {
                remove.setEnabled(false);
                Uri fileURI = data.getData();
                profilepicRef.child("/"+user.getId()+".png").putFile(fileURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        remove.setEnabled(true);
                        user.setPhotoURL(taskSnapshot.getDownloadUrl().toString());
                        Picasso.with(Main7Activity.this).load(user.getPhotoURL()).into(photo);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        remove.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Image Upload Failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        }
    }
}
