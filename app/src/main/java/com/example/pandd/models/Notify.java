package com.example.pandd.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Notify")
public class Notify extends ParseObject {
    public static final String KEY_USERID = "userid";
    public static final String KEY_STOREID = "storeid";


    public Notify(){}

    public ParseUser getUserId() {
        return getParseUser(KEY_USERID);
    }
    public void setUserId(ParseUser userId) {
        put(KEY_USERID, userId);
    }

    public String getStoreId() {
        return getString(KEY_STOREID);
    }
    public void setStoreId(String storeId) {
        put(KEY_STOREID, storeId);
    }

}
