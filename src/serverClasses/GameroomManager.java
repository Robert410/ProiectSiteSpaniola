package serverClasses;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

// static class responsible for handling queries regarding gamerooms
//      maps all legal gameroom IDs to a list of UserStats
//      gameroom IDs are legal once they are linked to a master session (only one master per gameroom)

public class GameroomManager {
    //  returns all players and masters in a gameroom
    public static List <UserStats> getGameroomFriends(Integer ID) { return gameroomMap.get(ID); }

    //  creates new gameroom data and returns the newly-created gameroom ID
    public static int createGameroom(Session master) throws IOException {
        int ID = GameroomManager.generateGameroomID();
        GameroomManager.gameroomMap.put(ID, new ArrayList<>());
        GameroomManager.add(master, ID);
        return ID;
    }

    //  sends a message to the players inside a gameroom
    public static void sendMessage(int ID,  String message) throws IOException {
        if (!gameroomMap.containsKey(ID)) return;
        for (UserStats user:gameroomMap.get(ID))
            if (user.isPlayer()) user.sendMessage(message);
    }
    //  sends a message to the players AND the master inside a gameroom
    public static void sendInclusiveMessage(int ID,  String message) throws IOException {
        if (!gameroomMap.containsKey(ID)) return;
        for (UserStats user:gameroomMap.get(ID))
            user.sendMessage(message);
    }
    //  sends message to the player AND the master EXCEPT a given user inside a gameroom
    //      needed in the case we're working with a session that we're about to close
    //      sending a message to a session that's about to close often crashes execution
    public static void sendExclusiveMessage(int ID, UserStats excluded, String message) throws IOException {
        if (!gameroomMap.containsKey(ID)) return;
        for (UserStats user:gameroomMap.get(ID))
            if (user != excluded) user.sendMessage(message);
    }

    //  sends a message to the master of the gameroom, so long as it exists
    public static void sendMessageToMaster(Integer ID, String message) throws IOException {
        if (!gameroomMap.containsKey(ID)) return;
        for (UserStats peer : gameroomMap.get(ID)) {
            if (peer.isMaster()) peer.sendMessage(message);
        }
    }

    //  adds a session to a gameroom represented by the ID; returns false if the ID is illegal, true if otherwise it was a success
    public static boolean add(Session session, int ID) {
        if (!gameroomMap.keySet().contains(ID)) return false;
        gameroomMap.get(ID).add(UserSession.getStats(session));
        UserSession.getStats(session).setID(ID);
        return true;
    }

    //  removes a session from his current gameroom
    //      a message that updates the single-player state to the session is also sent
    public static void remove(Session session) {
        UserStats stats = UserSession.getStats(session);
        if (stats.getID() != 0) {
            try {
                session.getBasicRemote().sendText("TA");
            }   catch (IOException e) {}
            gameroomMap.get(stats.getID()).remove(stats);
            stats.setID(0); //  !!! new line, untested; might bring problems
        }
    }

    // removes a session from his current gameroom, without sending the additional update message
    public static void removeWithoutMessage(Session session) {
        UserStats stats = UserSession.getStats(session);
        if (stats.getID() != 0) {
            gameroomMap.get(stats.getID()).remove(stats);
        }
    }

    //  deletes a room with given ID, if it exists
    //      all sessions in the gameroom will have their single-player state updated, as well as their additional information
    public static void deleteRoom(int ID) {
        int previousID = ID;
        for (UserStats peer : GameroomManager.gameroomMap.get(ID)) {
            try {
                if (peer.isPlayer()) peer.sendMessage("TA");
            }   catch (IOException e) {}
            peer.setID(0);
        }   GameroomManager.gameroomMap.remove(previousID);
        if (ID >= 1000 && ID <= 9999) numbersList.add(ID);
    }

    //  checks if gameroom of an user exists and contains a master
    public static boolean checkGameroomMaster(UserStats stats) {
        if (!gameroomMap.containsKey(stats.getID())) return false;
        for (UserStats peer : gameroomMap.get(stats.getID())) {
            if (peer.isMaster()) return true;
        }   return false;
    }

    //  checks whether given gameroom is currently playing a game or not
    public static boolean getGameroomState(Integer ID) {
        if (!gameroomMap.containsKey(ID)) return true;
        for (UserStats peer:gameroomMap.get(ID)) {
            if (peer.isPlayer()) return peer.getGameState();
        }   return false;
    }

    //  toggles the gamestate of a session AND his gameroom, if it exists (playing a game or not playing a game)
    public static void toggleGameroomState(Session session) {
        UserStats stats = UserSession.getStats(session);
        if (!gameroomMap.containsKey(stats.getID())) {
            UserSession.getStats(session).toggleGameState();
        }   else toggleGameroomState(stats.getID());
    }

    //   toggles the gamestate of a gameroom (playing a game or not playing a game)
    public static void toggleGameroomState(Integer ID) {
        for (UserStats peer:gameroomMap.get(ID)) {
            if (peer.isPlayer()) peer.toggleGameState();
        }
    }

    //  must be used when a session leaves the gameroom to notify the other gameroom users' client about it
    public static void onRemovalTasks(Session session) throws IOException {
        int prevID = UserSession.getStats(session).getID();
        if (prevID != 0) {
            //  GD - remove a name from the gameroom name set
            sendExclusiveMessage(prevID, UserSession.getStats(session), "GX" + UserSession.getStats(session).getStrID());
        }
    }

    //  returns all names of the users inside a gameroom, separated by '#'
    public static String getNamesRawString(Integer ID) {
        String ans = "";
        for (UserStats peer : gameroomMap.get(ID)) {
            ans += peer.getName() + "!" + peer.getStrID() + "#";
        }   return ans;
    }

    //  checks if a gameroom with given ID exists
    public static boolean checkExistence(Integer ID) {
        return gameroomMap.containsKey(ID);
    }

    //  returns whether a gameroom is a master's gameroom, but empty otherwise
    public static boolean checkMasterGameroom(UserStats stats) {
        if (!gameroomMap.containsKey(stats.getID())) return false;
        if (stats.isPlayer())   return false;
        if (!gameroomMap.containsKey(stats.getID())) return false;
        if (gameroomMap.get(stats.getID()).size() > 1) return true;
        return false;
    }

    //  returns whether a player is actually in a gameroom or not
    public static boolean checkPlayerGameroomEmpty(UserStats stats) {
        if (!stats.isPlayer())  return false;
        if (stats.getID() == 0) return true;
        if (!gameroomMap.containsKey(stats.getID()))    return true;
        if (gameroomMap.get(stats.getID()).size() == 1) return true;
        if (checkGameroomMaster(stats)) return false;
        return true;
    }

    //  returns the page current gameroom users are inside (stored inside the UserStats class)
    public static String getGameroomPage(int ID) {
        for (UserStats peer : getGameroomFriends(ID)) {
            if (peer.isMaster()) return peer.currentPage;
        }   return "";
    }

    //  private data
    private static Map<Integer, List<UserStats>> gameroomMap = new HashMap<>();
    private static boolean        initialized = false;
    private static Random         rng         = new Random();
    private static List <Integer> numbersList = new ArrayList<>();

    private static void initializeNumbersList() {
        initialized = true;
        for (int num = 1000; num <= 9999; ++ num)
            numbersList.add(num);
    }

    //  generates a gameroom ID not used in the present
    private static int generateGameroomID() {
        if (!initialized) initializeNumbersList();
        if (numbersList.size() <= 5) {
            int num;
            do {
                num = 10000 + rng.nextInt(200000000);
            }   while (gameroomMap.keySet().contains(num));
            return num;
        }
        int index = rng.nextInt(numbersList.size()-5);
        int ans = numbersList.get(index);
        numbersList.remove(index);
        return ans;
    }
}