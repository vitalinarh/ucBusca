package Server;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class for user to store its info
 */
public class User implements Serializable {

    /**
     * username
     */
    public String username;
    /**
     * password
     */
    public String password;
    /**
     * boolean for admin verification
     */
    public boolean admin;
    /**
     * port id for RMI client
     */
    public int portId;
    /**
     * ArrayList that stores user's searches
     */
    public ArrayList<String []> searches = new ArrayList<>();

    /**
     * constructor for user
     * @param username
     * @param password
     * @param portId
     */
    public User(String username, String password, int portId) {
        this.username = username;
        this.password = password;
        this.portId = portId;
    }


    public int getPortId() {
        return portId;
    }

    public ArrayList<String[]> getSearches() {
        return searches;
    }
}