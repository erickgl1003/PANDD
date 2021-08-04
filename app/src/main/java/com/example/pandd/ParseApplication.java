package com.example.pandd;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.pandd.models.Notify;
import com.example.pandd.models.Post;
import com.example.pandd.models.Store;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //Register parse models
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Store.class);
        ParseObject.registerSubclass(Notify.class);

        //Get ApplicationInfo to get the Parse clientKey
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("ParseApplication",e.getMessage());
            e.printStackTrace();
        }

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("JlbpSngihe4RzVoozEFvFfgQ6T3wbMSpWt51okze")
                .clientKey(ai.metaData.getString("parse_api_key"))
                .server("https://parseapi.back4app.com")
                .build()
        );

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "910053225214");
        installation.saveInBackground();
    }
}
