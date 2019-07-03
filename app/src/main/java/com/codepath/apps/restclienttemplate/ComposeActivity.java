package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    // a numeric code to identify the edit activity
    public static final int RESULT_OK = 21;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.twitter_blue)));
    }

    public void composeTweet(View v){
        client = new TwitterClient(getApplicationContext());

        // getting the contents of the new tweet
        EditText etTweet = (EditText) findViewById(R.id.etTweet);
        String itemText = etTweet.getText().toString();

        client.sendTweet(itemText, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Tweet tweet = Tweet.fromJSON(response);
                    // might need to send back a user as well?
                    Intent i = new Intent();
                    i.putExtra(Tweet.class.getSimpleName(), (Parcelable) tweet);
                    setResult(RESULT_OK, i);
                    finish();
                } catch (JSONException e) {
                    Log.e("ComposeActivity", "Couldn't compose new tweet");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ComposeActivity", errorResponse.toString());
                throwable.printStackTrace();
            }
        });

    }

}
