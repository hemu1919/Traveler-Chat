package com.example.heman.group14_hw09_a;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vipul on 4/10/2017.
 */

public class Message {
    String id, name, text, imageURL;
    long date;
    ArrayList<String> deletedUsers;

    public Message() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Message(String id, String name, String text, String imageURL, long date) {
        this.name = name;
        this.id = id;
        this.text = text;
        this.imageURL = imageURL;
        this.date = date;
        deletedUsers = new ArrayList();
    }

    public ArrayList<String> getDeletedUsers() {
        return deletedUsers;
    }

    public void setDeletedUsers(ArrayList<String> deletedUsers) {
        if(deletedUsers != null)
            this.deletedUsers = deletedUsers;
        else
            initial();
    }

    @Override
    public String toString() {
        return "Message{" +
                "name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", date=" + date +
                '}';
    }

    public Map<String, Object> toMap(){
        Map<String, Object> m1 = new HashMap<>();
        Map<String, Object> m2 = new HashMap<>();
        m1.put("name",name);
        m1.put("id",id);
        m1.put("text",text);
        m1.put("imageURL",imageURL);
        m1.put("date",date);

        int i=0;
        for(String m: deletedUsers){
            m2.put("user"+(i++),m);
        }

        m1.put("deletedUsers",m2);

        return m1;
    }

    public void addDeletedUser(String userID) {
        initial();
        deletedUsers.add(userID);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void initial() {
        if(deletedUsers == null)
            deletedUsers = new ArrayList<>();
    }

    public boolean containsUser(String userID) {
        if(deletedUsers == null)
            return false;
        return deletedUsers.contains(userID);
    }

    public static Message fromJSON(JSONObject obj) throws JSONException {
        Message m = new Message();
        m.setId(obj.getString("id"));
        m.setName(obj.getString("name"));
        if(obj.has("imageURL"))
            m.setImageURL(obj.getString("imageURL"));
        m.setDate(obj.getLong("date"));
        if(obj.has("text"))
            m.setText(obj.getString("text"));
        if(obj.has("deletedUsers")) {
            m.initial();
            ArrayList<String> ms = new ArrayList<>();
            JSONArray array = obj.getJSONArray("deletedUsers");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj1 = array.getJSONObject(i);
                ms.add(obj1.getString("user"));
            }
            m.setDeletedUsers(ms);
        }
        return m;
    }

}
