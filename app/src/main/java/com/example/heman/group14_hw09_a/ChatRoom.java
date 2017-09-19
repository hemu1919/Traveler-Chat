package com.example.heman.group14_hw09_a;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heman on 4/21/2017.
 */

public class ChatRoom {

    private String id;
    private boolean isAccessible = true;

    public ChatRoom() {}

    public ChatRoom(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("isAccessible", isAccessible);
        return  map;
    }

}
