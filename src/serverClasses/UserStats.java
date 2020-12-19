package serverClasses;

import java.util.*;

import java.io.IOException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/*  helps the server track data of an user
        all usernames are be distinct
        until username is set, the class is initialized with a unique guest name with format {"Guest." + {some number}}
        IDs are 0 for players who aren't inside a gameroom
        IDs have a fixed value for master sessions
        provides static function to check the legality of an username
*/

public class UserStats {
    private static final char[] IDcharacters = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };
    private static boolean bInitialized = false;
    private static void initialize() {
        if (bInitialized) return;
        bInitialized = true;
        for (char ch1 : IDcharacters) {
            for (char ch2 : IDcharacters) {
                final char[] masterCharArray = {'^', ch1, ch2};
                masterIDSet.add(String.valueOf(masterCharArray));
                for (char ch3: IDcharacters) {
                    final char[] charArray = {ch1, ch2, ch3};
                    IDSet.add(String.valueOf(charArray));
                }
            }
        }
    }

    private String strID = "";
    private void createID() {
        if (!strID.equals("")) {
            if (strID.charAt(0) == '^') {
                masterIDSet.add(strID);
            }
            else {
                IDSet.add(strID);
            }
        }

        if (isPlayer()) {
            strID = IDSet.stream().skip(new Random().nextInt(IDSet.size())).findFirst().orElse(null);
            IDSet.remove(strID);
        }
        else {
            strID = masterIDSet.stream().skip(new Random().nextInt(masterIDSet.size())).findFirst().orElse(null);
            masterIDSet.remove(strID);
        }
    }
    public String getStrID() {
        return strID;
    }

    UserStats(Session session) {
        createGuestName(session);
        initialize();
        createID();
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
        if (isPlayer()) IDSet.add(strID);
        else masterIDSet.add(strID);
    }

    //  getters and setters
    public int     getID()             { return ID; }
    public void    setID(int ID)       { this.ID  = ID; }
    public boolean isPlayer()          { return !job; }
    public boolean isMaster()          { return job;  }
    public void    setJob(boolean job) { this.job = job; createID(); }

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

    private static Set <String> IDSet = new HashSet<>();
    private static Set <String> masterIDSet = new HashSet<>();
}
