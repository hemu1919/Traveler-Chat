package com.example.heman.group14_hw09_a;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class GroupListAdapter extends ArrayAdapter<Trip> {

    private Context context;
    private List<Trip> profiles;
    private List<String> selected;
    private int resource;

    public List<Trip> getProfiles() {
        return profiles;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    public List<String> getSelected() {
        return selected;
    }

    public GroupListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Trip> objects) {
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
                    }
                });
            }
            else
                convertView.setTag(new ViewHolder(convertView, false));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        Trip trip = profiles.get(position);
        holder.name.setText(trip.getName());
        Picasso.with(context).load(trip.getPhotoURL()).into(holder.photo);
        holder.created.setText("Created by: "+trip.getOwnerName());
        if(holder.checkBox != null && selected.contains(trip.getId()))
            holder.checkBox.setChecked(true);
        return convertView;
    }

    public class ViewHolder {

        ImageView photo;
        TextView name;
        TextView created;
        CheckBox checkBox;
        View parent;

        public ViewHolder(View parent, boolean flag) {
            this.parent = parent;
            this.photo = (ImageView) parent.findViewById(R.id.profile_icon);
            this.name = (TextView) parent.findViewById(R.id.profile_name);
            this.created = (TextView) parent.findViewById(R.id.profile_name_cre);
            if(flag)
                this.checkBox = (CheckBox) parent.findViewById(R.id.selected);
        }

    }

}
