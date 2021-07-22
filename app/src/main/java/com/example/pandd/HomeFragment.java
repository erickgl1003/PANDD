package com.example.pandd;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.pandd.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import im.delight.android.location.SimpleLocation;

public class HomeFragment extends Fragment {
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    protected RecyclerView rvPosts;
    public static final String TAG = "HomeFragment";
    protected SwipeRefreshLayout swipeContainer;
    private SimpleLocation location;
    FragmentManager fragmentManager;
    BottomNavigationView bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();

        //Initialize SimpleLocation and request permissions to get the user actual location
        location = new SimpleLocation(context);
        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(context);
        }

        //Set navigation item when fragments loads by swiping
        bottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);


        //Set up the recyclerView to show the posts
        rvPosts = view.findViewById(R.id.rvPosts);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(context, allPosts,location.getLatitude(),location.getLongitude());
        rvPosts.setLayoutManager(new LinearLayoutManager(context));
        rvPosts.setAdapter(adapter);
        rvPosts.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        queryPosts();

        //Set up fragment swipes
        fragmentManager = getParentFragmentManager();
        rvPosts.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                leftSwipe();
            }
            @Override
            public void onSwipeRight() {
                rightSwipe();
            }
        });

        //Set up the swipeContainer that allows the user to refresh the posts.
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryPosts();
                adapter.addAll(allPosts,location.getLatitude(),location.getLongitude());
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_red_dark,
                android.R.color.holo_red_light);
    }

    private void rightSwipe() {
        bottomNavigationView.setSelectedItemId(R.id.post);
    }

    private void leftSwipe() {
        bottomNavigationView.setSelectedItemId(R.id.map);
    }

    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);

        query.include(Post.KEY_USER);
        query.include(Post.KEY_STORE);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting posts", e);
                    return;
                }
                
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
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
