package com.example.heman.group14_hw09_a;

import com.google.api.client.json.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by heman on 4/18/2017.
 */

public class User implements Serializable {

    private String id, lastName, firstName, photoURL, gender, email;
    private ArrayList<String> friendsList;
    private ArrayList<String> friendRequests;
    private ArrayList<String> ownedTrips;
    private ArrayList<String> joinedTrips;
    private HashMap<String, ArrayList<String>> deletedMessages;
    private String friendsJSON, friendRequestsJSON, ownedTripsJSON, joinedTripsJSON, deletedMessagesJSON;

    public ArrayList<String> getFriendsList() {
        return friendsList;
    }

    public void addOwnedTrip(String groupID) throws JSONException {
        ownedTrips.add(groupID);
        toOwnedTripsJSON();
    }

    public void deleteOwnedTrip(String tripID) {
        ownedTrips.remove(tripID);
        try {
            toOwnedTripsJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getOwnedTrips() {
        return ownedTrips;
    }

    public void addJoinedTrip(String groupID) throws JSONException {
        joinedTrips.add(groupID);
        toJoinedTripsJSON();
    }

    public int getTotalTripCount() {
        return ownedTrips.size() + joinedTrips.size();
    }

    public boolean containsTrip(String tripID) {
        return ownedTrips.contains(tripID) || joinedTrips.contains(tripID);
    }

    public boolean containsJoinedTrip(String tripID) {
        return joinedTrips.contains(tripID);
    }

    public boolean containsOwnedTrip(String tripID) {
        return ownedTrips.contains(tripID);
    }

    public ArrayList<String> getTripsList() {
        ArrayList<String> tripsList = new ArrayList<>(ownedTrips);
        tripsList.addAll(joinedTrips);
        return tripsList;
    }

    public void deleteJoinedTrip(String tripID) {
        joinedTrips.remove(tripID);
        try {
            toJoinedTripsJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFriendRequestsJSON() {
        return friendRequestsJSON;
    }

    public void setFriendsJSON(String friendsJSON) {
        this.friendsJSON = friendsJSON;
    }

    public void addFriendRequest(String recieverID) {
        friendRequests.add(recieverID);
    }

    public void deleteFriendRequest(String recieverID) {
        friendRequests.remove(recieverID);
        toMap();
    }

    public boolean containsFriendRequests(String recieverID) {
        return friendRequests.contains(recieverID);
    }

    public ArrayList<String> getFriendRequests() {
        return friendRequests;
    }

    public String getFriendsJSON() {
        return friendsJSON;
    }

    public User() {
        friendsList = new ArrayList<>(0);
        gender = "Not Specified";
        friendRequests = new ArrayList<>(0);
        ownedTrips = new ArrayList<>();
        joinedTrips =  new ArrayList<>();
        deletedMessages = new HashMap<>();
    }

    public void removeFriend(String id) throws JSONException {
        friendsList.remove(id);
        toFriendsJSON();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getOwnedTripsJSON() {
        return ownedTripsJSON;
    }

    public void setOwnedTripsJSON(String ownedTripsJSON) {
        this.ownedTripsJSON = ownedTripsJSON;
    }

    public String getJoinedTripsJSON() {
        return joinedTripsJSON;
    }

    public void setJoinedTripsJSON(String joinedTripsJSON) {
        this.joinedTripsJSON = joinedTripsJSON;
    }

    public String getDeletedMessagesJSON() {
        return deletedMessagesJSON;
    }

    public void setDeletedMessagesJSON(String deletedMessagesJSON) {
        this.deletedMessagesJSON = deletedMessagesJSON;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void addFriend(String id) {
        friendsList.add(id);
    }

    public void toFriendsJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        int i=0;
        for(String id : friendsList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id"+(i++), id);
            jsonArray.put(jsonObject);
        }
        friendsJSON = jsonArray.toString();
    }

    private void fromFriendsJSON() throws Exception {
        JSONArray jsonArray = new JSONArray(friendsJSON);
        if(jsonArray == null)
            return;
        for(int i = 0;i<jsonArray.length();i++)
            friendsList.add(jsonArray.getJSONObject(i).getString("id"+i));
    }

    public boolean containsFriend(String id) {
        return friendsList.contains(id);
    }

    public void fromJSON() {
        try {
            fromFriendsJSON();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fromFriendRequestsJSON();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fromownedTripsJSON();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fromJoinedTripsJSON();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fromFriendRequestsJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray(friendRequestsJSON);
        if(jsonArray == null)
            return;
        for(int i=0;i<jsonArray.length();i++)
            friendRequests.add(jsonArray.getJSONObject(i).getString("id"+i));
    }

    private void fromownedTripsJSON() throws Exception {
        JSONArray jsonArray = new JSONArray(ownedTripsJSON);
        if(jsonArray == null)
            return;
        for(int i=0;i<jsonArray.length();i++)
            ownedTrips.add(jsonArray.getJSONObject(i).getString("id"+i));
    }

    private void fromJoinedTripsJSON() throws Exception {
        JSONArray jsonArray = new JSONArray(joinedTripsJSON);
        if(jsonArray == null)
            return;
        for(int i=0;i<jsonArray.length();i++)
            joinedTrips.add(jsonArray.getJSONObject(i).getString("id"+i));
    }

    private void toOwnedTripsJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        int i=0;
        for(String id : ownedTrips) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id"+(i++), id);
            jsonArray.put(jsonObject);
        }
        ownedTripsJSON = jsonArray.toString();
    }

    private void toJoinedTripsJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        int i=0;
        for(String id : joinedTrips) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id"+(i++), id);
            jsonArray.put(jsonObject);
        }
        joinedTripsJSON = jsonArray.toString();
    }

    public void toJSON(Map<String, Object> map) {
        try {
            toFriendsJSON();
            toFriendRequestsJSON();
            toOwnedTripsJSON();
            toJoinedTripsJSON();

            map.put("joinedTripsJSON", joinedTripsJSON);
            map.put("ownedTripsJSON", ownedTripsJSON);

            map.put("friendRequestsJSON", friendRequestsJSON);
            map.put("friendsJSON", friendsJSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toFriendRequestsJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        int i=0;
        for(String id : friendRequests) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id"+(i++), id);
            jsonArray.put(jsonObject);
        }
        friendRequestsJSON = jsonArray.toString();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = toTruncMap();
        map.put("id", id);
        map.put("email", email);
        toJSON(map);
        return map;
    }

    public Map<String, Object> toTruncMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("gender", gender);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("photoURL", photoURL);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id.equals(user.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
