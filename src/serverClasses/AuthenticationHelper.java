package serverClasses;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

public class AuthenticationHelper implements HelperPrimitive {
    /*  helper class responsible for handling messages regarding
            initialization of the client connection,
            setting up UserStats data,
            player connection to a gameroom
     */

    AuthenticationHelper() {}

    private static char nameLegalitySignal = '%';
    private static char IDSignal = 'I';
    private static char playerSignal = 'E';
    private static char masterSignal = 'P';
    private static char signal = 'A';

    private static char queryName = 'N';
    private static char queryJob = 'J';
    private static char queryID = 'I';

    private static String positiveAnswer = "LP";        //  answer to name request, if it is valid
    private static String negativeAnswer = "LN";        //  answer to name request, if it is invalid
    private static String badIDAnswer  = "PP11";        //  this will trigger information display on the client

    public boolean isCalled(String str) { return str.charAt(0) == signal; }

    public void open(Session session) throws IOException, EncodeException { }
    public void close(Session session) throws IOException, EncodeException { }

    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        if (message.charAt(0) == queryName || message.charAt(0) == nameLegalitySignal) {
            if (message.charAt(0) == nameLegalitySignal) {
                String name = message.substring(1);
                UserSession.debugPrint(session, "Checking legality for: " + name + "#");

                if (UserStats.isUsernameIllegal(name)) session.getBasicRemote().sendText(negativeAnswer + name);
                else session.getBasicRemote().sendText(positiveAnswer);
            }
            else {
                message = message.substring(1);
                if (UserStats.isUsernameIllegal(message)) session.getBasicRemote().sendText("XX");
                else {
                    UserSession.getStats(session).setName(message);
                    UserSession.debugPrint(session, "Name was set as " + message + "#");
                }

                UserSession.debugPrint(session, "Query Name");
            }
        }   else
        if (message.charAt(0) == queryID) {
            if (UserSession.getStats(session).isPlayer()) {
                if (message.charAt(0) == IDSignal) {
                    UserStats stats = UserSession.getStats(session);
                    if (stats.getGameState()) {
                        //  PP message, triggering client to show information
                        session.getBasicRemote().sendText("PP7");
                    }
                    else {
                        GameroomManager.onRemovalTasks(session);
                        GameroomManager.remove(session);

                        try {
                            int ID = Integer.parseInt(message.substring(1));
                            if (!message.substring(1).matches("-?\\d+")) {
                                session.getBasicRemote().sendText(badIDAnswer);
                            } else
                            if (!GameroomManager.checkExistence(ID)) {
                                session.getBasicRemote().sendText(badIDAnswer);
                            } else
                            if (GameroomManager.getGameroomState(ID)) {
                                session.getBasicRemote().sendText("PP8");
                            } else
                            if (!stats.currentPage.equals(GameroomManager.getGameroomPage(ID))) {
                                session.getBasicRemote().sendText("PP9");
                            } else
                            if (GameroomManager.add(session, ID)) {
                                session.getBasicRemote().sendText("TA");
                                session.getBasicRemote().sendText("PP10");
                                //  GS - initialization of the gameroom name set on the clientside
                                session.getBasicRemote().sendText("GS" + GameroomManager.getNamesRawString(ID));
                                //  GD - updates the gameroom name set with another game
                                GameroomManager.sendExclusiveMessage(ID, UserSession.getStats(session),"GD" + UserSession.getStats(session).getName());
                            } else  session.getBasicRemote().sendText(badIDAnswer);
                        } catch (RuntimeException e) {
                            session.getBasicRemote().sendText(badIDAnswer);
                        }
                    }
                }
            }

            UserSession.debugPrint(session,"Query ID");
        }   else
        if (message.charAt(0) == queryJob) {
            session.getBasicRemote().sendText("VV");
                //  "VV": message representing that the websocket connection is properly established and all required initial information was sent
            message = message.substring(1);

            if (message.charAt(0) == playerSignal) UserSession.getStats(session).setJob(false);
            else if (message.charAt(0) == masterSignal) {
                session.getBasicRemote().sendText("TJ");

                int ID = GameroomManager.createGameroom(session);
                session.getBasicRemote().sendText("ID" + ID);
                UserSession.getStats(session).setJob(true);
            }

            UserSession.debugPrint(session,"Query Job");
        }
    }
}
