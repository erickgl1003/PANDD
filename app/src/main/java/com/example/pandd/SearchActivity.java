package com.example.pandd;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pandd.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

public class SearchActivity extends AppCompatActivity {
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    protected RecyclerView rvPosts;
    protected LinearLayout llPosts;
    public static final String TAG = "SearchActivity";
    private SimpleLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        location = new SimpleLocation(this);

        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(this);
        }

        llPosts = findViewById(R.id.llPosts);
        llPosts.animate().translationX(1000);

        rvPosts = findViewById(R.id.rvPosts);

        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(this, allPosts, location.getLatitude(), location.getLongitude());

        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(adapter);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            queryPosts("product", query.toLowerCase());
        } else {
            String field = intent.getStringExtra("field");
            String value = intent.getStringExtra("value");
            queryPosts(field, value);
        }
    }

    protected void queryPosts(String field, String value) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        query.include(Post.KEY_USER);
        query.include(Post.KEY_STORE);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");

        query.whereContains(field, value);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (posts.size() == 0) {
                    Toasty.warning(SearchActivity.this, "No posts contain said product", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                llPosts.animate().translationX(0);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        location.beginUpdates();
    }

    @Override
    public void onPause() {
        location.endUpdates();
        super.onPause();
    }

}

