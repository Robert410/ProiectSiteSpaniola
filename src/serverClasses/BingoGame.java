package serverClasses;

import javax.websocket.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class BingoUtilities {
    public static String getGameBeginMessage() {
        /*  returns a string containing numbers
            all numbers are separated by '#'
            first 16 numbers represent the numbers to be found
            the other numbers are numbers from 1 to 50, shuffled randomly
            each number to be found will appear 4 times in the shuffled list
        */

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
    /*  class responsible for actually managing the bingo game

        client events that must be handled by this class:
            bingo begin, sending all required information to the client
                see BingoUtilities::getGameBeginMessage
            bingo, when a player completed first the task or when nobody did, but time ran out
                this may potentially trigger the end of the game (when everyone in the same gameroom has finished)
                as well as updates to the ranking and leaderboard
     */

    BingoGame() { }

    protected static char beginSignal   = 'B';
    protected static char finishSignal  = 'F';
    protected char signal = 'B';

    protected static String beginAnswer   = "QB";     // sent to client, signals the game can start
        //  all other necessary information is also sent (see BingoUtilities::getGameBeginMessage)
    protected static String finishAnswer  = "QF";     // sent to client, signals the game should stop
        //  a message that displays the winner is also sent

    public boolean isCalled(String str) { return str.charAt(0) == signal; }
    public void finishGame(int ID, String message) throws IOException {
        GameroomManager.sendInclusiveMessage(ID, finishAnswer + message);
    }

    public void open(Session session) throws IOException, EncodeException { }
    public void close(Session session) throws IOException, EncodeException { }

    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        if (message.charAt(0) == beginSignal) {
            UserStats stats = UserSession.getStats(session);

            boolean isValidMaster = (stats.isMaster() && GameroomManager.checkMasterGameroom(stats));
            boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));

            if (stats.isPlayer() && !isValidPlayer) {
                //  PP messages trigger the display of some helpful information to the client (see "functii.js" for more)
                session.getBasicRemote().sendText("PP5");
            }
            if (stats.isMaster() && !isValidMaster) {
                session.getBasicRemote().sendText("PP6");
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