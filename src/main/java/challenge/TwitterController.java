package challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by nishantagarwal on 4/19/17.
 */
@RestController
@RequestMapping("twitter")
public class TwitterController {

    @Autowired
    private TwitterDao twitterDao;

    /**
     * Get tweets fro current user along with the users' he/she is following
     * @param name
     * @param keyword
     * @return
     */
    @RequestMapping(value="/{name}",method = RequestMethod.GET)
    public Map<String, Object> persondetails(@PathVariable("name") String name,
                                             @RequestParam(value="search",defaultValue = "",required = false) String keyword){

        try {
            Person p = twitterDao.find(name);

            Map<String, Object> tweets = twitterDao.getDisplayTweets(name, keyword.toLowerCase());

            Map<String, Object> details = new HashMap<>();

            details.put("id", p.getId());
            details.put("name", p.getName());
            details.put("tweets", tweets);

            return details;
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Get circle of the current user
     * @param name
     * @return
     */
    @RequestMapping(value="/{name}/circle",method = RequestMethod.GET)
    public Map<String, Object> circledetails(@PathVariable("name") String name){

        try {
            Person p = twitterDao.find(name);

            Map<String, Object> tweets = twitterDao.getSocialCircle(name);

            Map<String, Object> details = new HashMap<>();

            details.put("id", p.getId());
            details.put("name", p.getName());
            details.put("circle", tweets);

            return details;
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Follow another user
     * @param follower
     * @param followee
     */
    @RequestMapping(value="/{follower}/follow/{followee}")
    public String follow(@PathVariable("follower") String follower,
                                             @PathVariable("followee") String followee){

        try {
            Person follow_p = twitterDao.find(followee);

            Person p = twitterDao.find(follower);

            twitterDao.addFollower(follow_p.getId(), p.getId());

            return "Success";
        }catch(Exception e){
            throw e;
        }

    }

    /**
     * Unfolow another user
     * @param follower
     * @param followee
     */
    @RequestMapping(value="/{follower}/unfollow/{followee}")
    public String unfollow(@PathVariable("follower") String follower,
                       @PathVariable("followee") String followee){

        try {
            Person follow_p = twitterDao.find(followee);

            Person p = twitterDao.find(follower);

            twitterDao.removeFollower(follow_p.getId(), p.getId());

            return "Success";
        }catch(Exception e){
            throw e;
        }

    }

    /**
     * Get user and popular follower list
     * @return
     */
    @RequestMapping(value="/all")
    public List<Map<String,Object>> popularList(){

        try {
            return twitterDao.getPopular();
        }catch(Exception e){
            throw e;
        }

    }

    /**
     * Calculate hop between current user and another user
     * @param first
     * @param second
     * @return
     */
    @RequestMapping(value="/{first}/distance/{second}")
    public Map<String,Object> getDistance(@PathVariable("first") String first,
                            @PathVariable("second") String second){

        try {
            Person source = twitterDao.find(first);

            Person dest = twitterDao.find(second);

            int dist;
            if (source.getId() != dest.getId())
                dist = getDist(source.getId(), dest.getId());
            else
                dist = 0;

            Map<String, Object> details = new HashMap<>();

            details.put("Source", first);
            details.put("Destination", second);
            details.put("Distance", dist == Integer.MAX_VALUE ? "No Path" : dist);

            return details;
        }catch(Exception e){
            throw e;
        }
    }


    /**
     * Calculate hop function
     * @param sourceID
     * @param destID
     * @return
     */
    public int getDist(int sourceID, int destID) {

        Queue<Integer> toVisit = new ArrayDeque<>();

        int level = 0;
        boolean found = false;

        toVisit.add(sourceID);

        Map<Integer, Integer> visited = new HashMap<>();

        while (!found) {

            int nodeCount = toVisit.size();
            if (nodeCount == 0) break;

            while (nodeCount > 0) {
                Integer node = toVisit.poll();

                if (node == destID) {
                    found = true;
                    break;
                }

                visited.put(node, 1);
                List<Integer> neighbors = twitterDao.getFollowers(node);
                for (int i = 0; i < neighbors.size(); i++) {
                    if (!visited.containsKey(neighbors.get(i))) {
                        toVisit.add(neighbors.get(i));
                    }
                }
                nodeCount--;
            }
            level++;
        }

        return found?level-1:Integer.MAX_VALUE;
    }
}
