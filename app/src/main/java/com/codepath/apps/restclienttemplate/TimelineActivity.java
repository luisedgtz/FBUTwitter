package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    //Logging
    public static final String TAG = "TimelineActivity";

    private final int REQUEST_CODE = 20;

    private SwipeRefreshLayout swipeContainer;


    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    private MenuItem miActionProgressItem;

    //Max_id and since_id are used to reduce redundancy on twitter API calls made to serve the infinite scroll feature
    //For more info check this link: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/timelines/guides/working-with-timelines
    private long maxId = Long.MAX_VALUE;
    private long sinceId = 1;

    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;

    //keeps track of the number of items received since last API call
    //this is done because calls subsequent to the first getHomeTimeline() call vary in number
    int newItemsSinceLastCall = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        //Add logo and remove title from action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setElevation(2);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        client = TwitterApp.getRestClient(this);

        //Find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Init the list of tweets
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        //Recycler view setup: layout manager and the adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMoreTweets(tweets.get(tweets.size()-1).id_str);
            }
        };

        rvTweets.addOnScrollListener(scrollListener);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        //Refresh listener
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                maxId = Long.MAX_VALUE;
                sinceId = 1;
                fetchTimeLineAsync();
                adapter.notifyDataSetChanged();
            }
        });

        fetchTimeLineAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        return true;
    }

    public void showProgressBar() {
        // Show progress item
        if (miActionProgressItem != null) {
            miActionProgressItem.setVisible(true);
        }
    }

    public void hideProgressBar() {
        // Hide progress item
        if (miActionProgressItem != null) {
            miActionProgressItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose){
            //Compose icon has been selected
            //Navigate to the compose activity
            Intent intent =  new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        } else if(item.getItemId() == R.id.btnLogout){
            //btnLogout icon has been selected
            //Logout
            onLogoutButton();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            //Get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            //Update the recycler view with the tweet
            //Modify data source of tweets
            tweets.add(0, tweet);
            //Update the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchTimeLineAsync(){
        showProgressBar();
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                adapter.clear();
                JSONArray jsonArray =  json.jsonArray;

                Log.i(TAG, "onSuccess" + json.toString());
                try {
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception" , e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure:" + response , throwable);
            }
        });
        hideProgressBar();
    }

    private void loadMoreTweets(long maxId) {
        showProgressBar();
        client.getLatestTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> newTweets = Tweet.fromJsonArray(jsonArray);
                    tweets.addAll(newTweets);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure:" + response , throwable);
            }
        }, maxId);
        hideProgressBar();
    }

    public void onLogoutButton(){
        //Forget who's logged in
        TwitterApp.getRestClient(this).clearAccessToken();
        //Navigate backwards to login screen
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}