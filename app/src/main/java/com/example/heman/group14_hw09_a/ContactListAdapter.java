package com.example.heman.group14_hw09_a;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heman on 4/18/2017.
 */

public class ContactListAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> profiles;
    private List<String> selected;
    private int resource;

    public List<User> getProfiles() {
        return profiles;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    public List<String> getSelected() {
        return selected;
    }

    public ContactListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        this.context = context;
        this.profiles = objects;
        this.resource = resource;
        selected = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final int i = position;
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            if(resource == R.layout.select_laout) {
                convertView.setTag(new ViewHolder(convertView, true));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder holder = (ViewHolder) v.getTag();
                        if(!holder.checkBox.isChecked()) {
                            selected.add(profiles.get(i).getId());
                            holder.checkBox.setChecked(true);
                        } else {
                            selected.remove(profiles.get(i).getId());
                            holder.checkBox.setChecked(false);
                        }
                        Log.d("demo", selected.toString());
                    }
                });
            }
            else
                convertView.setTag(new ViewHolder(convertView, false));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        User user = profiles.get(position);
        holder.name.setText(user.getFirstName()+" "+user.getLastName());
        Picasso.with(context).load(user.getPhotoURL()).into(holder.photo);
        if(holder.checkBox != null) {
            if(selected.contains(user.getId()))
                holder.checkBox.setChecked(true);
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox check = (CheckBox) v;
                    if(!check.isChecked()) {
                        selected.remove(profiles.get(i).getId());
                    } else {
                        selected.add(profiles.get(i).getId());
                    }
                    Log.d("demo", selected.toString());
                }
            });
        }
        return convertView;
    }

    public class ViewHolder {

        ImageView photo;
        TextView name;
        CheckBox checkBox;
        View parent;

        public ViewHolder(View parent, boolean flag) {
            this.parent = parent;
            this.photo = (ImageView) parent.findViewById(R.id.profile_icon);
            this.name = (TextView) parent.findViewById(R.id.profile_name);
            if(flag)
                this.checkBox = (CheckBox) parent.findViewById(R.id.selected);
        }

    }

}
