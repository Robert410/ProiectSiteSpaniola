package serverClasses;

import java.util.Set;
import java.util.HashSet;

import java.io.IOException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.ArrayList;

//  helps the server track data of an user

//  the nice stuff :)
//      all usernames will be distinct
//      until username is set, the class is initialized with a unique guest name with format {"Guest." + {some number}}
//      IDs should be 0 for players who aren't inside a gameroom
//      IDs should have a fixed value for master sessions
//      provides static function to check the legality of an username

//  the sad stuff :(
//      multiple sessions with the same UserStats functionality is currently missing (aka there should be one UserStats for each Session)
//      names suddenly dont work anymore?

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
    public boolean isSingle() { return sessions.size() == 1; }
    public void sendMessageToSessions(String message) throws IOException {
        for (Session session:sessions)
            session.getBasicRemote().sendText(message);
    }
    public void remove(Session session) {
        usernamesSet.remove(name);
        sessions.remove(session);
    }

    //  getters and setter
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

    public String currentPage = "";

    //  private data
    private List <Session> sessions = new ArrayList <>();
    private String  name   = "";
    private int     ID     = 0;
    private boolean job    = false;
    private boolean inGame = false;

    private void createGuestName(Session session) {
        int idx = 0;
        do { ++ idx; } while (isUsernameIllegal("Guest." + idx));
        name = "Guest." + idx;
        usernamesSet.add(name);
        sessions.add(session);
    }
}
