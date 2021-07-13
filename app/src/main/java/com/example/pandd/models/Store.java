package com.example.pandd.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Store")
public class Store extends ParseObject {
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_MAPID = "mapId";

    public Store(){}

    public String getName(){return getString(KEY_NAME);}
    public void setName(String name){put(KEY_NAME,name);}

    public String getAddress(){return getString(KEY_ADDRESS);}
    public void setAddress(String address){put(KEY_ADDRESS,address);}

    public String getMapId(){return getString(KEY_MAPID);}
    public void setMapId(String mapId){put(KEY_MAPID,mapId);}
}
