package challenge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nishantagarwal on 4/19/17.
 */

/**
 * Tweet Class to mimic tweet table in database
 */
public class Tweet {
    private int id;
    private int person_id;
    List<String> tweets;


    public Tweet() {
        tweets = null;
    }

    public Tweet(int id, List<String> tweets) {
        this();
        this.id = id;
        this.tweets = tweets;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getTweets() {
        return tweets;
    }

    public void setTweets(List<String> tweets) {
        this.tweets = tweets;
    }

    public int getPerson_id() {
        return person_id;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
    }

    public void add(String tweet){
        List<String> old_tweets = getTweets();
        if(old_tweets == null){
            old_tweets = new ArrayList<>();
        }
        old_tweets.add(tweet);
        setTweets(old_tweets);
    }
}
