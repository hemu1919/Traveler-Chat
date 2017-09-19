package com.example.heman.group14_hw09_a;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by vipul on 4/10/2017.
 */

public class CustomAdapter extends ArrayAdapter {
    ArrayList<Message> messages;
    Context context;
    Trip trip;
    User user;
    int r1, r2;

    public CustomAdapter(Context context, int resource, ArrayList<Message> objects, Trip trip, User user) {
        super(context, resource, objects);
        messages = objects;
        this.context = context;
        r1 = R.layout.textlayout;
        r2 = R.layout.imagelayout;
        this.trip = trip;
        this.user = user;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Message m = messages.get(position);
        PrettyTime pt = new PrettyTime();
        String name = user.getFirstName()+" "+user.getLastName();

        if(m.getImageURL()==null){
            convertView = LayoutInflater.from(context).inflate(r1, parent, false);
            if(m.getName().equalsIgnoreCase(name)){
                convertView.setPadding(75,0,0,0);
            } else
                convertView.setPadding(0,0,75,0);

            ((TextView)convertView.findViewById(R.id.messageTextId)).setText(m.getText());
            ((TextView)convertView.findViewById(R.id.userName)).setText(m.getName());
            ((TextView)convertView.findViewById(R.id.chattime)).setText(pt.format(new Date(m.getDate())));
        }else {
            convertView = LayoutInflater.from(context).inflate(r2, parent, false);
            if(m.getName().equalsIgnoreCase(name)){
                convertView.setPadding(50,0,0,0);
            } else
                convertView.setPadding(0,0,50,0);
            ((TextView) convertView.findViewById(R.id.name2)).setText(m.getName());
            ((TextView) convertView.findViewById(R.id.time2)).setText(pt.format(new Date(m.getDate())));

            final StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(m.getImageURL());
            ImageView v = ((ImageView) convertView.findViewById(R.id.chatImage));
            Glide.with(context).using(new FirebaseImageLoader()).load(ref).into(v);
        }

        convertView.setLongClickable(true);
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("/chatrooms/"+trip.getChatRoom()+"/message/"+m.getId());
                m.addDeletedUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.setValue(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "Message deleted successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });

        return  convertView;
    }
}
