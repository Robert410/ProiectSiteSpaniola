package serverClasses;

import javax.websocket.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

class QuizUtilities {
    /*  class only containing public static methods
        role of the class:
            quiz data computation
            remove a large clutter of functions from QuizGamedata for easier human understanding/debugging
    */

    public static List <String> generateEntries(int max) {
        List <String> entries = new ArrayList<>();
        for (int i=0; i<max; ++i) {
            if (i < 10)
                entries.add("0" + i);
            else
                entries.add("" + i);
        }
        Collections.shuffle(entries);
        return entries;
    };

    public static String createRankingMessage(Integer ID, UserStats exception) {
        String rawdata = "";
        for (UserStats peer : sortGameroomUsers(ID, exception)) {
            if (peer.isMaster()) continue;
            rawdata += createRankingMessage(peer);
        }   return rawdata;
    }

    public static String createRankingMessage(UserStats stats) {
        if (QuizGame.isInvalid(stats)) return "";
        QuizGamedata gamedata = QuizGame.getGamedataOf(stats);
        double timeSum = gamedata.timesList.stream().mapToDouble(a->a).sum();
        int temp = (int)(timeSum * 1000.0);
        timeSum = ((double)temp)/1000.0;
        return stats.getStrID() + "!" + QuizGame.getGamedataOf(stats).getScore() + "!" + gamedata.questionsAnswered + "!" + timeSum +"#";
    }

    public static int userCompare(UserStats x, UserStats y) {
        QuizGamedata gamedata_x = QuizGame.getGamedataOf(x);
        QuizGamedata gamedata_y = QuizGame.getGamedataOf(y);
        Double time_x = gamedata_x.timesList.stream().mapToDouble(a->a).sum();
        Double time_y = gamedata_y.timesList.stream().mapToDouble(a->a).sum();
        int score_x = gamedata_x.score;
        int score_y = gamedata_y.score;
        if (score_x == score_y) {
            if (time_x.equals(time_y)) {
                return 0;
            }   else
            if (time_x > time_y) return 1;
            return -1;
        }   else
        if (score_x < score_y) return 1;
        return -1;
    }

    public static List <UserStats> sortGameroomUsers(Integer ID, UserStats exception) {
        List <UserStats> gameroomUsers = new ArrayList<>();
        for (UserStats peer : GameroomManager.getGameroomFriends(ID)) {
            if (peer.isPlayer() && peer != exception) gameroomUsers.add(peer);
        }
        gameroomUsers.sort(QuizUtilities::userCompare);
        return gameroomUsers;
    }
}

class QuizGamedata {
    /*  class storing game data about a single player
     */

    public QuizGamedata(UserStats user, int maxQuestions, char signal) {
        this.user = user;
        this.maxQuestions = maxQuestions;
        this.signal = signal;
        score = 0;
        questionsAnswered = 0;
    }

    public int questionsAnswered;
    public char signal;

    public void increment(int question, Double time, int score) {
        questionsAnswered = question+1;
        timesList.add(time);
        this.score = score;
    }

    public boolean isFinished() { return questionsAnswered >= maxQuestions; }
    public int getScore() { return score; }

    public boolean shouldUpdateRanking(int ranking) {
        if (isFinished()) return false;
        if (previousRanking == ranking) return false;
        previousRanking = ranking;
        return true;
    }

    protected int previousRanking = -1;
    protected UserStats user;
    protected int score;
    protected int maxQuestions;
    protected List <Double> timesList = new ArrayList<>();
}

public class QuizGame implements HelperPrimitive {
    /*  class responsible for actually managing quiz game sessions
        each QuizGame instance is largely defined by its
            signal (should be a unique identifier, but that is not checked when constructing)
            maxQuestions (how large the pool of questions is)
            and totalQuestions (how many questions will actually be used in a single game session)

        client events that must be handled by this class:
            quiz begin, sending all required information to the client
            quiz update, updating a player's information
                this may potentially trigger the end of the game (when everyone in the same gameroom has finished)
                as well as updates to the ranking and leaderboard
     */
    QuizGame() { }

    protected static char beginSignal   = 'B';
    protected static char updateSignal  = 'U';
    protected char signal = 'Q';

    protected static String beginAnswer     = "QB";     // sent to client, signals the quiz can start
        // the webSocket message will also contains a list of numbers representing the questions (2 digits each; "08" = 8, "18" = 18)
    protected static String abortAnswer     = "QA";     // sent to client, signals the quiz should stop
    protected static String printAnswer     = "QP";     // sent to client, signals an update to the game leaderboard data
        // the webSocket message will contain necessary update data (sorted data about each player, separated by '#')
    protected static String rankingAnswer   = "QK";     // sent to client, signals an update to the current ranking
        // the webSocket message will also contain the string representing the ranking (i.e. "1/3", "4/6")

    //  maxQuestions: represents the whole pool of questions; totalQuestions: represents the actual number of questions in each game
    //  out of all maxQuestions, only totalQuestions will be used each time
    public int maxQuestions = 35;
    public int totalQuestions = 10;

    protected static Map <UserStats, QuizGamedata> gamedataMap = new HashMap<>();

    public boolean isCalled(String str) { return str.charAt(0) == signal; }

    static public boolean isInvalid(UserStats stats)  { return !gamedataMap.containsKey(stats); }
    public static QuizGamedata getGamedataOf(UserStats stats) {
        if (!gamedataMap.containsKey(stats)) return null;
        return gamedataMap.get(stats);
    }

    protected void abortGame(Integer ID, UserStats exception) throws IOException {
        //  parameter exception is needed in cases that session will log off from the site;
        //      sending a message to such session often crashes execution.
        //      such situation is met precisely when we are closing a session and trying to update the room

        GameroomManager.sendExclusiveMessage(ID, exception,"TQ");
        //  GameroomManager.sendExclusiveMessage(ID, exception,printAnswer + QuizUtilities.createRankingMessage(ID, exception));
        GameroomManager.sendMessageToMaster (ID, abortAnswer);
        for (UserStats stats : GameroomManager.getGameroomFriends(ID)) gamedataMap.remove(stats);
    }
    protected void abortGame(Session session) {
        try {
            //  session.getBasicRemote().sendText(printAnswer + QuizUtilities.createRankingMessage(UserSession.getStats(session)));
            session.getBasicRemote().sendText("TQ");
            session.getBasicRemote().sendText(abortAnswer);
        }   catch (IOException e) {}
        gamedataMap.remove(session);
    }

    protected void updateRoom(Integer ID, UserStats exception) throws IOException {
        //  parameter exception is needed in cases that session will log off from the site;
        //      sending a message to such session often crashes execution.
        //      such situation is met precisely when we are closing a session and trying to update the room

        boolean bAbort = true;          //  tells us whether the game should be aborted or not (all players have finished)
        for (UserStats stats : GameroomManager.getGameroomFriends(ID)) {
            if (!gamedataMap.containsKey(stats)) continue;
            QuizGamedata gamedata = gamedataMap.get(stats);
            if (gamedata == null) continue;
            if (stats.isPlayer() && !gamedata.isFinished())
                bAbort = false;
        }

        List <UserStats> arr = QuizUtilities.sortGameroomUsers(ID, exception);
        for (int i=0; i<arr.size(); ++i) {
            UserStats stats = arr.get(i);
            //  updates ranking
            if (stats != exception && gamedataMap.get(stats).shouldUpdateRanking(i+1))
                stats.sendMessage(rankingAnswer + "#" + (i+1));
        }

        if (UserSession.debugMode) GameroomManager.sendExclusiveMessage(ID, exception, "" + bAbort);

        if (bAbort) {
            abortGame(ID, exception);
        }
    }

    public void open(Session session) throws IOException, EncodeException { }

    public void close(Session session) throws IOException, EncodeException {
        if (gamedataMap.containsKey(UserSession.getStats(session))) {
            UserStats stats = UserSession.getStats(session);
            //  formal null pointer check
            if (getGamedataOf(UserSession.getStats(session)) == null) return;
            //  this check is required because the different QuizGamedata instances shouldn't work with gamedata for other instances
            if (!(getGamedataOf(stats).signal == signal)) return;
            //  if the session is a player that plays alone, no further action is required
            if (GameroomManager.checkPlayerGameroomEmpty(stats)) {
                gamedataMap.remove(stats);
            }
            else {
                if (UserSession.debugMode) GameroomManager.sendExclusiveMessage(UserSession.getStats(session).getID(), UserSession.getStats(session), "" + getGamedataOf(UserSession.getStats(session)).signal);
                gamedataMap.remove(UserSession.getStats(session));
                if (UserSession.debugMode) GameroomManager.sendExclusiveMessage(UserSession.getStats(session).getID(), UserSession.getStats(session), "map close task 1 done");
                //  otherwise, we must update the room
                updateRoom(UserSession.getStats(session).getID(), UserSession.getStats(session));
                if (UserSession.debugMode) GameroomManager.sendExclusiveMessage(UserSession.getStats(session).getID(), UserSession.getStats(session), "map close task 2 done");
            }
        }
    }

    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        UserSession.debugPrint(session, "" + "hello");
        if (message.charAt(0) == beginSignal) {
            if (message.length() > 1 && message.charAt(1) == '#') {
                String[] arr = message.split("#");
                maxQuestions = Integer.parseInt(arr[1]);
                totalQuestions = Integer.parseInt(arr[2]);
            }

            UserStats stats = UserSession.getStats(session);

            boolean isValidMaster = (stats.isMaster() && GameroomManager.checkMasterGameroom(stats));
                // master validity: their gameroom contains some players; if their gameroom is empty, there's no point in starting the quiz
            boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));
                // player validity: they are not in any gameroom; if they are in one, the master is supposed to start it

            if (GameroomManager.checkPlayerGameroomEmpty(stats)) UserSession.debugPrint(session,"Gameroom is considered empty");
            if (stats.isPlayer()) UserSession.debugPrint(session, "Session is player");

            if (stats.isPlayer() && !isValidPlayer) {
                //  PP messages trigger the display of some helpful information to the client (see "functii.js" for more)
                session.getBasicRemote().sendText("PP3");
            }
            if (stats.isMaster() && !isValidMaster) {
                session.getBasicRemote().sendText("PP4");
            }

            if (isValidMaster || isValidPlayer) {
                int ID = stats.getID();
                UserSession.debugPrint(session,"Here I am");

                StringBuilder entryCode = new StringBuilder();  // the string we will return containing necessary begin information
                List <String> entries = QuizUtilities.generateEntries(maxQuestions);
                for (int entry = 0; entry < totalQuestions; ++entry) {
                    entryCode.append(entries.get(entry));
                }

                GameroomManager.toggleGameroomState(session);

                if (isValidMaster) {
                    GameroomManager.sendInclusiveMessage(ID, beginAnswer   + entryCode.toString());
                    for (UserStats peerStats : GameroomManager.getGameroomFriends(stats.getID()))
                        gamedataMap.put(peerStats, new QuizGamedata(peerStats, totalQuestions, signal));
                }
                else {
                    session.getBasicRemote().sendText(beginAnswer   + entryCode.toString());
                    gamedataMap.put(stats, new QuizGamedata(stats, totalQuestions, signal));
                }
            }
        }   else
        if (message.charAt(0) == updateSignal) {
            UserStats stats = UserSession.getStats(session);
            QuizGamedata data = gamedataMap.get(stats);
            int ID = stats.getID();
            String[] arr = message.split("#");

            int score = Integer.parseInt(arr[0].substring(1));
            int question = Integer.parseInt(arr[1]);
            Double time = Double.parseDouble(arr[2]);

            UserSession.debugPrint(session, "score: "    + score);
            UserSession.debugPrint(session, "question: " + question);
            UserSession.debugPrint(session, "time: "     + time);

            data.increment(question, time, score);

            if (data.isFinished()) {
                UserSession.debugPrint(session, "finallllllllll");
                UserSession.debugPrint(session, "finalx");
                UserSession.debugPrint(session, "final2");
                UserSession.debugPrint(session, "final3");

                UserSession.getStats(session).toggleGameState();
                UserSession.debugPrint(session, "final5");
                if (ID != 0) {
                    String leaderboardData = QuizUtilities.createRankingMessage(ID, null);
                    stats.sendMessage(printAnswer + leaderboardData);
                    session.getBasicRemote().sendText("PP2");
                }
                else {
                    UserSession.debugPrint(session, "final6");
                    abortGame(session);
                    UserSession.debugPrint(session, "final7");
                }
                UserSession.debugPrint(session, "final4");
            }

            String leaderboardData = QuizUtilities.createRankingMessage(stats);
            for (UserStats peer : GameroomManager.getGameroomFriends(ID)) {
                if (gamedataMap.get(peer).isFinished() || peer.isMaster())
                    peer.sendMessage(printAnswer + leaderboardData);
            }
        }

        UserStats stats = UserSession.getStats(session);
        boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));
        UserSession.debugPrint(session, "" + isValidPlayer);
        if (isValidPlayer) {
            QuizGamedata gamedata = gamedataMap.get(stats);
            if (gamedata != null) {
                UserSession.debugPrint(session, "final2xxxxx");
                if (gamedata.isFinished()) abortGame(session);
            }
        }   else
        if (gamedataMap.containsKey(UserSession.getStats(session))) updateRoom(UserSession.getStats(session).getID(), null);
    }
}