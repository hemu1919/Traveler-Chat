package com.example.heman.group14_hw09_a;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
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

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.heman.group14_hw09_a.R.id.selected;

/**
 * Created by heman on 4/18/2017.
 */

public class PlacesAdapter extends ArrayAdapter<Location> {

    private Context context;
    private List<Location> places;
    private int resource;


    public PlacesAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Location> places) {
        super(context, resource, places);
        this.context = context;
        this.places = places;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final int i = position;
        Location loc = places.get(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        holder.pname.setText(places.get(i).getName());
        Log.d("demo","Position Value"+position);
        Log.d("demo","Location Value"+loc.toString());
        try {
            addresses = gcd.getFromLocation(loc.getLat(), loc.getLng(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            Log.d("demo","Address: "+addresses.get(0).toString());
            holder.paddress.setText(addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getAddressLine(1));
        }
        return convertView;
    }

    public class ViewHolder {
        TextView pname;
        TextView paddress;
        View parent;

        public ViewHolder(View parent) {
            this.parent = parent;
            this.pname = (TextView) parent.findViewById(R.id.place_name);
            this.paddress = (TextView) parent.findViewById(R.id.place_address);
        }

    }

}
