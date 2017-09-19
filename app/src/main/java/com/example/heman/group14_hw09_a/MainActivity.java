package com.example.heman.group14_hw09_a;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth2.PasswordTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.people.v1.PeopleScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        PeopleAsync.IProfie {

    private static final int RC_SIGN_IN = 1000;
    public static final String KEY1 ="Profile" ;
    public final static String KEY5 = "trip";
    private SignInButton signInButton;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInAccount googleSignInAccount;
    private AuthCredential authCredential;
    private GoogleApiClient googleApiClient;
    private GoogleSignInResult googleSignInResult;
    private GroupListAdapter groups;

    private Menu menu;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference root, users, tripsRef;
    private ValueEventListener listener1;
    private User user_details;
    private ArrayList<Trip> trips;
    DatabaseReference user_ref;

    private EditText email, passwd, editText;
    private TextView no_chat;
    private RelativeLayout login_layout;
    private Button login;
    private ListView chat_list;
    private ProgressDialog dialog;

    private Intent intent;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Signing In");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        firebaseDatabase = FirebaseDatabase.getInstance();
        root = firebaseDatabase.getReference();
        users = root.child("/users").getRef();
        tripsRef = root.child("/trips").getRef();


        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser == null) {
                    if(menu!=null)
                        menu.setGroupVisible(R.id.group1, false);
                    login_layout.setVisibility(View.VISIBLE);
                    chat_list.setVisibility(View.INVISIBLE);
                } else
                {
                    displayChat();
                }
            }
        };



        no_chat = (TextView) findViewById(R.id.no_chats);
        chat_list= (ListView) findViewById(R.id.chat_list);
        chat_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Trip> currentList = groups.getProfiles();
                intent = new Intent(getApplicationContext(), Main9Activity.class);
                intent.putExtra(MainActivity.KEY1, currentList.get(position));
                startActivity(intent);
            }
        });
        signInButton = (SignInButton) findViewById(R.id.signinbut);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.clientID))
                .requestServerAuthCode(getString(R.string.clientID))
                .requestEmail()
                .requestScopes(new Scope(PeopleScopes.USERINFO_PROFILE))
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .addOnConnectionFailedListener(this)
                .build();

        login_layout = (RelativeLayout) findViewById(R.id.login_layout);
        email = (EditText) findViewById(R.id.emailtext);
        passwd = (EditText) findViewById(R.id.passwordText);
        login = (Button) findViewById(R.id.login_but);
        login.setOnClickListener(this);

    }

    private void displayChat() {
        user_ref = firebaseDatabase.getReference("users/"+firebaseUser.getUid());
        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                user_details = dataSnapshot.getValue(User.class);
                user_details.fromJSON();
                dialog.dismiss();
                preProcess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void preProcess() {
        Log.d("demo", "Started preprocess");
        trips = new ArrayList<>();
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trips.clear();

                user = dataSnapshot.getValue(User.class);
                user.fromJSON();
                final ArrayList<String> tripsList = user.getTripsList();
                Log.d("demo", "Trips List size"+tripsList.size());
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
        user_ref.addListenerForSingleValueEvent(listener1);
    }

    public void authenticate() {
        authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        firebaseAuth.fetchProvidersForEmail(googleSignInAccount.getEmail()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                List<String> providers = task.getResult().getProviders();
                Log.d("demo", providers.toString());
                if(providers.size() == 0) {
                    editText = new EditText(MainActivity.this);
                    editText.setInputType(passwd.getInputType());
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Please Choose your password!")
                            .setView(editText)
                            .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    if(editText.length() == 0) {
                                        Toast.makeText(getApplicationContext(), "SignIn failed: Invalid Password!",Toast.LENGTH_SHORT).show();
                                        Auth.GoogleSignInApi.signOut(googleApiClient);
                                        return;
                                    }
                                    firebaseAuth.createUserWithEmailAndPassword(googleSignInAccount.getEmail(), editText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(!task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "SignIn failed: "+task.getException().getMessage()+"!",Toast.LENGTH_SHORT).show();
                                                Auth.GoogleSignInApi.signOut(googleApiClient);
                                                return;
                                            }
                                            MainActivity.this.dialog.show();
                                            setAppUI(true);
                                            new PeopleAsync(getApplicationContext(), MainActivity.this).execute(googleSignInAccount);
                                            firebaseUser.linkWithCredential(authCredential);
                                            Auth.GoogleSignInApi.signOut(googleApiClient);
                                        }
                                    });
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "SignIn failed: Please Provide Password!",Toast.LENGTH_SHORT).show();
                                    Auth.GoogleSignInApi.signOut(googleApiClient);
                                }
                            }).setCancelable(false);
                    builder.show();
                } else {
                    firebaseAuth.signInWithCredential(authCredential)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    setAppUI(true);
                                    new PeopleAsync(getApplicationContext(), MainActivity.this).execute(googleSignInAccount);
                                    Auth.GoogleSignInApi.signOut(googleApiClient);
                                }
                            });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_layout1, menu);
        if(firebaseUser != null) {
            setAppUI(true);
            dialog.setTitle("Loading..!");
            dialog.show();
            users.child("/"+firebaseUser.getUid()).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    MainActivity.this.user = dataSnapshot.getValue(User.class);
                    MainActivity.this.user.fromJSON();
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            dialog.setTitle("Signing In");
            setAppUI(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                Toast.makeText(getApplicationContext(), "Logged out Successfully", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                chat_list.setVisibility(View.INVISIBLE);
                no_chat.setVisibility(View.INVISIBLE);
                setAppUI(false);
                break;
            case R.id.settings:
                intent = new Intent(getApplicationContext(), Main7Activity.class);
                intent.putExtra(KEY1, this.user);
                startActivity(intent);
                break;
            case R.id.contacts:
                intent = new Intent(getApplicationContext(), Main2Activity.class);
                intent.putExtra(KEY1, this.user);
                startActivity(intent);
                break;
            case R.id.groups:
                intent = new Intent(getApplicationContext(), Main4Activity.class);
                intent.putExtra(KEY1, this.user);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_SIGN_IN) {
            dialog.dismiss();
            googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()) {
                googleSignInAccount = googleSignInResult.getSignInAccount();
                authenticate();
            }
            else
                Toast.makeText(getApplicationContext(),"Error Code: "+googleSignInResult.getStatus().getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.signinbut:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                dialog.show();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.login_but:
                if(email.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please fill all the fields!",Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), passwd.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Login Failed: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                setAppUI(true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Login Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }

    public void setAppUI(boolean flag) {
        menu.setGroupVisible(R.id.group2, false);
        if(flag) {
            menu.setGroupVisible(R.id.group1, true);
            login_layout.setVisibility(View.INVISIBLE);
        } else {
            menu.setGroupVisible(R.id.group1, false);
            login_layout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = mGoogleApiAvailability.getErrorDialog(this, connectionResult.getErrorCode(), RC_SIGN_IN);
        dialog.show();
    }

    @Override
    public void setUserProfile(final User user) {
        final String key = firebaseUser.getUid();
        user.setId(key);
        email.setText("");
        passwd.setText("");
        users.child("/"+key).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    users.child("/" + key).getRef().setValue(user.toMap());
                    MainActivity.this.user = user;
                } else {
                    User user1 = dataSnapshot.getValue(User.class);
                    user1.fromJSON();
                    MainActivity.this.user = user1;
                }
                displayChat();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public void setListUI(List updatedList) {
        Log.d("demo", "list size"+updatedList.size());
        if(updatedList.size() == 0) {
            no_chat.setText("Currently no Trip information exists!");
            no_chat.setVisibility(View.VISIBLE);
            chat_list.setVisibility(View.INVISIBLE);
        } else {
            no_chat.setVisibility(View.INVISIBLE);
            chat_list.setVisibility(View.VISIBLE);
            groups = new GroupListAdapter(getApplicationContext(), R.layout.group_layout, updatedList);
            chat_list.setAdapter(groups);
        }
    }
}
