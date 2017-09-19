package com.example.heman.group14_hw09_a;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heman on 4/20/2017.
 */

public class Trip implements Serializable {

    String id, name, location, photoURL, chatRoom;
    ArrayList<String> peopleIDs;
    ArrayList<Location> placesIDs;
    String ownerID, peopleIDsJSON, ownerName, placeIDsJSON;

    public ArrayList<Location> getPlacesIDs() {
        return placesIDs;
    }

    public void setPlacesIDs(ArrayList<Location> placesIDs) {
        this.placesIDs = placesIDs;
    }

    public Trip() {
        peopleIDs = new ArrayList<>();
        placesIDs = new ArrayList<>();
    }

    public String getPlaceIDsJSON() {
        return placeIDsJSON;
    }

    public void setPlaceIDsJSON(String placeIDsJSON) {
        this.placeIDsJSON = placeIDsJSON;
    }

    public Trip(String ownerID) {
        this.ownerID = ownerID;
    }

    public ArrayList<String> getPeopleIDs() {
        return peopleIDs;
    }

    public void removePeopleIDs(String peopleID) {
        peopleIDs.remove(peopleID);
        toJSON();
    }

    public void addPlaceID(Location place) {
        placesIDs.add(place);
    }
    public void addPlaceID(int index, Location place) {
        placesIDs.set(index, place);
    }
    public void removePlaceID(Location place) {
        placesIDs.remove(place);
    }

    public void setPeopleIDs(ArrayList<String> peopleIDs) {
        this.peopleIDs = peopleIDs;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public ArrayList<String> getMembers() {
        ArrayList<String> members = new ArrayList<>();
        members.addAll(peopleIDs);
        members.add(ownerID);
        return  members;
    }

    public String getPeopleIDsJSON() {
        return peopleIDsJSON;
    }

    public void setPeopleIDsJSON(String peopleIDsJSON) {
        this.peopleIDsJSON = peopleIDsJSON;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("photoURL", photoURL);
        map.put("location", location);
        map.put("chatRoom", chatRoom);
        map.put("ownerID", ownerID);
        map.put("ownerName", ownerName);
        toJSON();
        map.put("peopleIDsJSON", peopleIDsJSON);
        map.put("placeIDsJSON", placeIDsJSON);
        return map;
    }

    private void toPeoplesJSON() {
        JSONArray jsonArray = new JSONArray();
        try {
            int i = 0;
            for (String id : peopleIDs) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id" + (i++), id);
                jsonArray.put(jsonObject);
            }
            peopleIDsJSON = jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toPlacesJson() {
        JSONArray jsonArray = new JSONArray();
        try {
            int i = 0;
            for (Location id : placesIDs) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name" + (i), id.getName());
                jsonObject.put("lat" + (i), id.getLat());
                jsonObject.put("lon" + (i++), id.getLng());
                jsonArray.put(jsonObject);
            }
            placeIDsJSON = jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toJSON() {
        toPeoplesJSON();
        toPlacesJson();
    }

    private void fromPeoplseIDsJSON() {
        try {
            JSONArray jsonArray = new JSONArray(peopleIDsJSON);
            if (jsonArray == null)
                return;
            for (int i = 0; i < jsonArray.length(); i++)
                peopleIDs.add(jsonArray.getJSONObject(i).getString("id" + i));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fromPlacesIDsJSON() {
        try {
            JSONArray jsonArray = new JSONArray(placeIDsJSON);
            if (jsonArray == null)
                return;
            for (int i = 0; i < jsonArray.length(); i++)
                placesIDs.add(new Location(jsonArray.getJSONObject(i).getString("name" + i), jsonArray.getJSONObject(i).getDouble("lat" + i), jsonArray.getJSONObject(i).getDouble("lon" + i)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fromJSON() {
        fromPeoplseIDsJSON();
        fromPlacesIDsJSON();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trip trip = (Trip) o;

        return id.equals(trip.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void addPeopleIDs(String id) {
        peopleIDs.add(id);
        toJSON();
    }
}
