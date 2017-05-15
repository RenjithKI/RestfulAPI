package challenge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nishantagarwal on 4/19/17.
 */

/**
 * Data Access class to interact with database
 */
@Repository
public class TwitterDaoImpl implements TwitterDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Find the person from person table based on name
     * @param name
     * @return Person
     */
    @Override
    public Person find(String name){
        String query = "SELECT * from person WHERE name=:name";

        SqlParameterSource namedParameters = new MapSqlParameterSource("name", name);

        try {
            Person p = namedParameterJdbcTemplate.queryForObject(query, namedParameters,
                    new BeanPropertyRowMapper<Person>(Person.class));
            return p;
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Get (filtered, if keyword specified) tweets for self and people user is following
     * @param name
     * @param keyword
     * @return Map of self and following tweets
     */
    @Override
    public Map<String,Object> getDisplayTweets(String name,String keyword){
        try {
            //get users tweets
            String query = "SELECT tweet.content from tweet,person WHERE person.name=:name and " +
                    "person.id=tweet.person_id and lower(tweet.content) LIKE :keyword";
            Map namedParameters = new HashMap();
            namedParameters.put("name", name);
            namedParameters.put("keyword", '%' + keyword + '%');

            List<String> selftweets = namedParameterJdbcTemplate.queryForList(query, namedParameters, String.class);

            //get list of people user following
            query = "SELECT followers.person_id from followers,person WHERE (person.name=:name and " +
                    "person.id=followers.follower_person_id)";

            List<Integer> followers_id = namedParameterJdbcTemplate.queryForList(query, namedParameters, Integer.class);

            //get tweets of the users the current user is following
            query = "SELECT content from tweet WHERE person_id IN(:followers_id) and lower(content) LIKE :keyword";

            namedParameters = new HashMap();
            namedParameters.put("followers_id", followers_id);
            namedParameters.put("keyword", '%' + keyword + '%');

            List<String> followers_tweets = namedParameterJdbcTemplate.queryForList(query, namedParameters, String.class);

            Map<String, Object> data = new HashMap<>();
            data.put("self-tweets", selftweets);
            data.put("followers-tweets", followers_tweets);

            return data;
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Get list of people in user's social circle
     * @param name
     * @return
     */
    @Override
    public Map<String,Object> getSocialCircle(String name){

        try {
            //get list of users the current user is following
            String query = "SELECT id,name from person WHERE id IN (SELECT followers.person_id from followers,person WHERE " +
                    "person.name=:name and person.id=followers.follower_person_id)";

            Map namedParameters = new HashMap();
            namedParameters.put("name", name);

            List<Person> following_id = namedParameterJdbcTemplate.query(query, namedParameters,
                    new BeanPropertyRowMapper<Person>(Person.class));

            //get list of users that are following the current user
            query = "SELECT id,name from person WHERE id IN (SELECT followers.follower_person_id from followers,person WHERE " +
                    "person.name=:name and person.id=followers.person_id)";


            List<Person> followers_id = namedParameterJdbcTemplate.query(query, namedParameters,
                    new BeanPropertyRowMapper<Person>(Person.class));

            Map<String, Object> data = new HashMap<>();
            data.put("following", following_id);
            data.put("followers", followers_id);

            return data;
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Add follower to followers table
     * @param followee_id
     * @param follower_id
     */
    @Override
    public void addFollower(int followee_id, int follower_id) {
        try {
            String query = "SELECT * from followers WHERE follower_person_id=:follower_id and person_id = :followee_id";
            Map namedParameters = new HashMap();
            namedParameters.put("follower_id", follower_id);
            namedParameters.put("followee_id", followee_id);

            try {
                Followers f = namedParameterJdbcTemplate.queryForObject(query, namedParameters,
                        new BeanPropertyRowMapper<Followers>(Followers.class));
            } catch (DataAccessException e) {
                query = "INSERT INTO followers(person_id, follower_person_id) VALUES (:followee_id,:follower_id)";
                namedParameterJdbcTemplate.update(query, namedParameters);
            }
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Revome follower from followers table
     * @param followee_id
     * @param follower_id
     */
    @Override
    public void removeFollower(int followee_id, int follower_id){

        try {
            String query = "SELECT * from followers WHERE follower_person_id=:follower_id and person_id = :followee_id";
            Map namedParameters = new HashMap();
            namedParameters.put("follower_id", follower_id);
            namedParameters.put("followee_id", followee_id);

            Followers f;

            try {
                f = namedParameterJdbcTemplate.queryForObject(query, namedParameters,
                        new BeanPropertyRowMapper<Followers>(Followers.class));
            } catch (DataAccessException e) {
                return;
            }
            query = "DELETE FROM followers WHERE person_id=:followee_id and follower_person_id=:follower_id";
            namedParameterJdbcTemplate.update(query, namedParameters);
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Get list of all user and their popular follower
     * @return
     */
    @Override
    public List<Map<String,Object>> getPopular(){
        try {
            String query = "SELECT S.PERSON_NAME, S.FOLLOWER_NAME, S.person_id, S.FOLLOWER_ID, S.COUNT FROM " +
                    "(SELECT table3.name as PERSON_NAME, table4.name as FOLLOWER_NAME, table1.person_id AS PERSON_ID , " +
                    "table2.person_id as FOLLOWER_ID, table2.cnt as COUNT FROM followers AS table1 JOIN " +
                    "(SELECT person_id, COUNT(*) AS cnt FROM followers GROUP BY person_id) AS table2 " +
                    "ON table1.follower_person_id = table2.person_id JOIN Person table3 ON table3.id = table1.person_id " +
                    "JOIN Person table4 ON table4.id = table2.person_id) as S JOIN " +
                    "(SELECT table1.person_id, MAX(CNT) as MAX_COUNT FROM followers AS table1 JOIN " +
                    "(SELECT person_id, COUNT(*) AS cnt FROM followers GROUP BY person_id) AS table2 " +
                    "ON table1.follower_person_id = table2.person_id GROUP BY table1.person_id) AS M ON " +
                    "S.PERSON_ID = M.PERSON_ID AND S.COUNT = M.MAX_COUNT";

            Map namedParameters = new HashMap();
            List<Map<String, Object>> details = namedParameterJdbcTemplate.queryForList(query, namedParameters);

            return details;
        }catch(Exception e){
            throw e;
        }
    }

    /**
     * Get list of users current user is following
     * @param id
     * @return
     */
    @Override
    public List<Integer> getFollowers(int id) {
        try {
            String query = "SELECT followers.person_id from followers,person WHERE " +
                    "person.id=:id and person.id=followers.follower_person_id";

            Map namedParameters = new HashMap();
            namedParameters.put("id", id);

            List<Integer> followers_id = namedParameterJdbcTemplate.queryForList(query, namedParameters, Integer.class);

            return followers_id;
        }catch(Exception e){
            throw e;
        }
    }
}
