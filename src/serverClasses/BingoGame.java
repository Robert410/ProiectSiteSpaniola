package serverClasses;

import javax.websocket.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class BingoUtilities {
    public static String getGameBeginMessage() {
        List <Integer> list = new ArrayList<>();
        List <Integer> list2 = new ArrayList<>();
        for (int i=1; i<=50; ++i) {
            list.add(i);
            list2.add(i);
        }
        Collections.shuffle(list);
        for (int i=0; i<16; ++i) {
            for (int j=0; j<3; ++j)
                list2.add(list.get(i));
        }
        Collections.shuffle(list2);

        StringBuilder beginMessage = new StringBuilder();
        for (int i=0; i<16; ++i) {
            beginMessage.append("" + list.get(i));
            beginMessage.append('#');
        }
        for (int i=0; i<list2.size(); ++i) {
            beginMessage.append("" + list2.get(i));
            beginMessage.append('#');
        }
        return beginMessage.toString();
    }
}

public class BingoGame implements HelperPrimitive {
    BingoGame() { }

    protected static char beginSignal   = 'B';
    protected static char finishSignal  = 'F';
    protected char signal = 'B';

    protected static String beginAnswer   = "QB";
    protected static String finishAnswer  = "QF";

    public void finishGame(int ID, String message) throws IOException {
        GameroomManager.sendInclusiveMessage(ID, finishAnswer + message);
    }

    public boolean isCalled(String str) { return str.charAt(0) == signal; }

    public void open(Session session) throws IOException, EncodeException { }
    public void close(Session session) throws IOException, EncodeException {
        try {
            UserStats stats = UserSession.getStats(session);
            if (stats.isPlayer() && !GameroomManager.checkPlayerGameroomEmpty(stats) && GameroomManager.getGameroomFriends(stats.getID()).size() == 2)
                GameroomManager.sendMessageToMaster(stats.getID(), "Toata lumea a plecat, gameroom-ul se va inchide.");
        }   catch (Exception e) {}
    }

    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        if (message.charAt(0) == beginSignal) {
            UserStats stats = UserSession.getStats(session);

            boolean isValidMaster = (stats.isMaster() && GameroomManager.checkMasterGameroom(stats));
            boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));

            if (stats.isPlayer() && !isValidPlayer) {
                session.getBasicRemote().sendText("PPNu poti folosi acest buton; lasa-l pe profesor sa inceapa.");
            }
            if (stats.isMaster() && !isValidMaster) {
                session.getBasicRemote().sendText("PPAcest buton nu poate fi folosit, fiindca gameroom-ul este gol.");
            }

            UserSession.debugPrint(session,""+isValidMaster + "  " + isValidPlayer);
            if (isValidMaster || isValidPlayer) {
                GameroomManager.toggleGameroomState(session);
                int ID = stats.getID();
                if (isValidPlayer) session.getBasicRemote().sendText(beginAnswer + BingoUtilities.getGameBeginMessage());
                else GameroomManager.sendInclusiveMessage(ID, beginAnswer + BingoUtilities.getGameBeginMessage());
            }
        }   else
        if (message.charAt(0) == finishSignal) {
            GameroomManager.toggleGameroomState(session);
            UserStats stats = UserSession.getStats(session);
            int ID = stats.getID();
            boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));
            if (isValidPlayer) session.getBasicRemote().sendText(finishAnswer);
            else finishGame(ID, "Felicitări " + stats.getName() + "!");
        }
    }
}