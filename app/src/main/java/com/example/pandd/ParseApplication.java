package com.example.pandd;

import android.app.Application;

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

        // Register parse models
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Store.class);
        ParseObject.registerSubclass(Notify.class);
        
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("JlbpSngihe4RzVoozEFvFfgQ6T3wbMSpWt51okze")
                .clientKey("UeFQ3EaBK2xrBIpxjd8ItzOP1AcuBehn1TO56Mn4")
                .server("https://parseapi.back4app.com")
                .build()
        );

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "910053225214");
        installation.saveInBackground();
    }
}
