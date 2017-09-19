package com.example.heman.group14_hw09_a;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by heman on 4/18/2017.
 */

public class FriendRequests {

    public static int ACCEPTED = 1;
    public static int PENDING = 0;
    public static int CANCELLED = -1;

    private String senderID;
    private int status;

    public FriendRequests() {}

    public FriendRequests(String senderID) {
        this.senderID = senderID;
        this.status = FriendRequests.PENDING;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("senderID", senderID);
        map.put("status", status);
        return map;
    }

}
