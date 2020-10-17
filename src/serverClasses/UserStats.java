package serverClasses;

import java.util.Set;
import java.util.HashSet;

import java.io.IOException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.ArrayList;

/*  helps the server track data of an user
        all usernames are be distinct
        until username is set, the class is initialized with a unique guest name with format {"Guest." + {some number}}
        IDs are 0 for players who aren't inside a gameroom
        IDs have a fixed value for master sessions
        provides static function to check the legality of an username
*/

public class UserStats {
    UserStats(Session session) {
        createGuestName(session);
    }

    //  static data
    static private Set <String> usernamesSet = new HashSet <>();
    static public boolean isUsernameIllegal(String user) {
        return usernamesSet.contains(user);
    }

    //  public data
    //  sends a message to the session corresponding to the userStats
    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void remove(Session session) {
        usernamesSet.remove(name);
    }

    //  getters and setters
    public int     getID()             { return ID; }
    public void    setID(int ID)       { this.ID  = ID; }
    public boolean isPlayer()          { return !job; }
    public boolean isMaster()          { return job;  }
    public void    setJob(boolean job) { this.job = job; }

    public void    toggleGameState()   { inGame = !inGame; }
    public boolean getGameState()      { return inGame; }

    public String  getName()           { return name; }
    public void    setName(String name) {
        if (isUsernameIllegal(name)) return;
        this.name = name;
        usernamesSet.add(this.name);
    }

    public String currentPage = "";     // a unique string representing the page the client is currently on;
        // this data is received on initialization of the websockets connection

    //  private data
    Session session = null;             //  provides a direct link from UserStats to Session
    private String  name   = "";        //  the unique username
    private int     ID     = 0;
    private boolean job    = false;     //  false for players, true for master
    private boolean inGame = false;     //  whether currently this user is playing a game or not

    private void createGuestName(Session session) {
        int idx = 0;
        do { ++ idx; } while (isUsernameIllegal("Guest." + idx));
        name = "Guest." + idx;
        usernamesSet.add(name);
        this.session = session;
    }
}
