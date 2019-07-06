package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tweet {

    // list out the attributes
    public String body;
    public long uid; // database ID for the tweet
    public User user;
    public String createdAt;
    public JSONObject entities;
    public JSONArray media;
    public String image;

    // deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        // extract the values from JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.entities= jsonObject.getJSONObject("entities");
        //  validating that media exists
        if(tweet.entities.has("media")){
            tweet.media = tweet.entities.getJSONArray("media");
            tweet.image = tweet.media.getJSONObject(0).getString("media_url_https");
        }
        return tweet;
    }

}
