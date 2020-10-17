package serverClasses;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@ServerEndpoint("/ws")
public class UserSession {
    //  static data
    public static UserStats getStats(Session session) {
        return (UserStats) session.getUserProperties().get(statsMapString);
    }

    private static List <HelperPrimitive> helpers = new ArrayList<>();

    //  helper classes functionality
    private void openHelpers(Session session) throws IOException, EncodeException {
        if (helpers.isEmpty()) initializeHelpers();
        for (HelperPrimitive helper : helpers) helper.open(session);
    }
    private void closeHelpers(Session session) throws IOException, EncodeException {
        for (HelperPrimitive helper : helpers) helper.close(session);
    }
    private static void addHelper(HelperPrimitive helper) {
        helpers.add(helper);
    }
    private static void initializeHelpers() {
        addHelper(new AuthenticationHelper());
        addHelper(new QuizGame());
        addHelper(new MapGuessGame());
        addHelper(new BingoGame());
    }


    //  private data
    private HelperPrimitive request = null;

    private static String statsMapString = "S";

    //  close functions
    private void closePlayerSession(Session session) throws IOException {
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"hey1");
        GameroomManager.onRemovalTasks(session);
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"hey2");
        GameroomManager.removeWithoutMessage(session);
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"hey3");
    }
    private void closeMasterSession(Session session) {
        GameroomManager.deleteRoom(getStats(session).getID());
    }

    //  websockets functions
    @OnOpen
    public void open(Session session) throws IOException, EncodeException {
        session.getUserProperties().put(statsMapString, new UserStats(session));
        openHelpers(session);
    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        if (getStats(session).isMaster() && GameroomManager.checkMasterGameroom(getStats(session))) {
            GameroomManager.sendMessage(getStats(session).getID(), "PP1");
            //  GM - resets the gameroom name set
            GameroomManager.sendMessage(getStats(session).getID(), "GM");
        }
        closeHelpers(session);
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"close task 1 done");
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),getStats(session).getName() + " has left.");
        if (getStats(session).isMaster()) closeMasterSession(session);
        else                              closePlayerSession(session);
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"close task 2 done");
        getStats(session).remove(session);
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"close task 3 done");
        if (session.getUserProperties() != null) session.getUserProperties().clear();
        if (debugMode) GameroomManager.sendExclusiveMessage(getStats(session).getID(), getStats(session),"close task 4 done");
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        if (message.charAt(0) == '%') {         //  client page update
            if (getStats(session) == null) return;
            getStats(session).currentPage = message.substring(1);
            return;
        }

        if (message.charAt(0) == '*') {         //  websockets initialization message
            String[] arr = message.substring(1).split("#");
            handleMessage("AN" + arr[0], session);
            if (arr[1].equals("player")) handleMessage("AJE", session);
            else                         handleMessage("AJP", session);
            return;
        }

        request = null;
        debugPrint(session, "Handling message...");
        debugPrint(session, message);
        for (HelperPrimitive helper : helpers) {
            if (helper.isCalled(message)) {
                UserSession.debugPrint(session, "Helper found!");
                request = helper;
            }
        }

        if (request != null) {
            debugPrint(session, "Message sent: " + message.substring(1));
            request.handleMessage(message.substring(1), session);
            request = null;
        }
    }

    //  debugging purposes
    public static boolean debugMode = false;
    public static void debugPrint(Session session, String message) {
        if (!debugMode) return;
        try {
            session.getBasicRemote().sendText("DEBUG: " + message);
        }   catch (IOException e) {}
    }
}
