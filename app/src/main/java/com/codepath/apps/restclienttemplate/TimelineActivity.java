package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;

    // a numeric code to identify the edit activity
    public static final int REQUEST_CODE = 20;
    public static final int RESULT_OK = 21;

    // needed for refresh by pull down
    private SwipeRefreshLayout swipeContainer;

    // Instance of the progress action-view
    MenuItem miActionProgressItem;

    // needed for infinite pagination
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;

    // max_id that is updated and tracked for infinite pagenation
    private long max_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // setting up the twitter blue and twitter bird logo in the action bar
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.twitter_blue)));
        bar.setLogo(R.drawable.ic_launcher_twitter);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setIcon(R.drawable.ic_launcher_twitter); //also displays wide logo

        client = TwitterApp.getRestClient(getApplicationContext());

        // find the RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweet);
        rvTweets.addItemDecoration(new DividerItemDecoration(rvTweets.getContext(), DividerItemDecoration.VERTICAL));
        linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);

        // enabling endless pagination

        // setting up the scrollListener
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                fetchTimelineAsync(true);
            }
        };

        rvTweets.addOnScrollListener(scrollListener);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // init the arraylist (data source)
        tweets = new ArrayList<>();
        // construct the adapter from this data source
        tweetAdapter = new TweetAdapter(tweets);
        // RecyclerView setup (layout manager, use adapter)
        rvTweets.setLayoutManager(linearLayoutManager);
        // set the adapter
        rvTweets.setAdapter(tweetAdapter);

        populateTimeline();
    }

    //
    public void fetchTimelineAsync(final boolean isNotRefresh) {

        if(isNotRefresh){
            max_id = tweets.get(tweets.size()-1).uid;
        }
        else{
            max_id = -1;
        }

        showProgressBar();
        // Send the network request to fetch the updated data
        client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // Remember to CLEAR OUT old items before appending in the new ones
                if(!isNotRefresh) {
                    tweetAdapter.clear();
                }
                // ...the data has come back, add new items to your adapter..
                for(int i = 0; i < response.length(); i++){
                    try {
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                tweetAdapter.addAll(tweets);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
                hideProgressBar();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        });
    }


    // creating the compose tweet action item
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handle click here
    public void onComposeAction(MenuItem mi) {
        // configuring the action item to launch ComposeActivity using intents
        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE); // brings up the second activity
    }

    // handle the tweet being sent back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check request code and result code first
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            // Use data parameter
            Tweet tweet = (Tweet) Parcels.unwrap(data.getParcelableExtra("tweet"));
            tweets.add(0, tweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
            rvTweets.scrollToPosition(0);
        }
    }

    // displaying the progress bar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    // toggling the visibility of the progress bar
    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    private void populateTimeline(){
        client.getHomeTimeline(max_id, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // iterate through the JSON array
                // for each entry, deserialize the JSON object

                for(int i = 0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    // add that Tweet model to our data source
                    // notify the adapter that we've added an item
                    try {
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

        });
    }
}
