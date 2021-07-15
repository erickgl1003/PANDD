package com.example.pandd.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Store")
public class Store extends ParseObject {
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_MAPID = "mapId";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LONG = "long";

    public Store(){}

    public Double getLat() {
        return getDouble(KEY_LAT);
    }
    public void setLat(Double lat) {
        put(KEY_LAT, lat);
    }

    public Double getLong() {
        return getDouble(KEY_LONG);
    }
    public void setLong(Double longi) {
        put(KEY_LONG, longi);
    }

    public String getName(){return getString(KEY_NAME);}
    public void setName(String name){put(KEY_NAME,name);}

    public String getAddress(){return getString(KEY_ADDRESS);}
    public void setAddress(String address){put(KEY_ADDRESS,address);}

    public String getMapId(){return getString(KEY_MAPID);}
    public void setMapId(String mapId){put(KEY_MAPID,mapId);}
}
