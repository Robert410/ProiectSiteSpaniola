package serverClasses;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

public class AuthenticationHelper implements HelperPrimitive {
    AuthenticationHelper() {}

    private static String taskMapString  = "T";

    private static char nameLegalitySignal = '%';
    private static char IDSignal = 'I';
    private static char playerSignal = 'E';
    private static char masterSignal = 'P';
    private static char signal = 'A';

    private static char queryName = 'N';
    private static char queryJob = 'J';
    private static char queryID = 'I';

    private static String positiveAnswer = "LP";
    private static String negativeAnswer = "LN";
    private static String badIDAnswer  = "PPID-ul introdus nu există. Întreabă-l pe profesor ID-ul.";

    enum Task { QUERY_NAME, QUERY_JOB, QUERY_ID }
    static public Task getTask(Session session) {
        return (Task) session.getUserProperties().get(taskMapString);
    }
    static public void setTask(Session session, Task task) {
        session.getUserProperties().put(taskMapString, task);
    }

    public boolean isCalled(String str) { return str.charAt(0) == signal; }

    public void open(Session session) throws IOException, EncodeException {
        session.getUserProperties().put(taskMapString, Task.QUERY_NAME);
    }
    public void close(Session session) throws IOException, EncodeException {}
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
                if (UserStats.isUsernameIllegal(message)) session.getBasicRemote().sendText("XXUsername is illegal!");
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
                        session.getBasicRemote().sendText("PPNu poți să te muți din gameroom până nu îți termini jocul!");
                    }
                    else {
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
                                session.getBasicRemote().sendText("PPGameroom-ul deja se joacă, nu te poți conecta acum :( Așteaptă să se termine jocul!");
                            } else
                            if (!stats.currentPage.equals(GameroomManager.getGameroomPage(ID))) {
                                session.getBasicRemote().sendText("PPNu te afli pe pagina potrivită a gameroom-ului.");
                            } else
                            if (GameroomManager.add(session, ID)) {
                                session.getBasicRemote().sendText("TA");
                                session.getBasicRemote().sendText("PPAi intrat în gameroom!");
                                session.getBasicRemote().sendText(GameroomManager.joinMessage(session));
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
