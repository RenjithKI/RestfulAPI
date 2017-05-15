package challenge;

import challenge.Person;

import java.util.List;
import java.util.Map;

/**
 * Created by nishantagarwal on 4/19/17.
 */

/**
 * Data Access Interface
 */
public interface TwitterDao {

    public Person find(String name);
    public Map<String,Object> getDisplayTweets(String name,String keyword);
    public Map<String,Object> getSocialCircle(String name);

    public void addFollower(int followee_id,int follower_id);
    public void removeFollower(int followee_id,int follower_id);

    public List<Map<String,Object>> getPopular();

    public List<Integer> getFollowers(int id);
}
