package com.example.heman.group14_hw09_a;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by heman on 4/29/2017.
 */

public class Location implements Serializable {

    String name;
    double lat, lng;

    public Location() {
    }

    public Location(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
