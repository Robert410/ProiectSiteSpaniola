package serverClasses;

import javax.websocket.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

class QuizUtilities {
    public static List <String> generateEntries() {
        List <String> entries = new ArrayList<>(QuizGame.rawQuestions);
        Collections.shuffle(entries);
        return entries;
    };

    public static String createRankingMessage(Integer ID) {
        String rawdata = "";
        for (UserStats peer : QuizGame.sortGameroomUsers(ID)) {
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
        return stats.getName() + ": " + QuizGame.getGamedataOf(stats).getScore() + " points, " + gamedata.questionsAnswered + "/" + gamedata.maxQuestions + ", " + timeSum +"s#";
    }
}

class QuizGamedata {
    public QuizGamedata() { }
    public QuizGamedata(UserStats user, int maxQuestions, char signal) { this.user = user; this.maxQuestions = maxQuestions; this.signal = signal; score = 0; questionsAnswered = 0; }

    public void increment(int question, Double time, int score) {
        questionsAnswered = question+1;
        timesList.add(time);
        this.score = score;
    }
    public String answerString() {
        String answer = user.getName() + " has answered question number " + questionsAnswered + "! Total score: " + score + ".";
        return answer;
    }
    public String finishString() {
        String answer = user.getName() + " has finished! Total score: " + score + ".";
        return answer;
    }

    public boolean isFinished() { return questionsAnswered >= maxQuestions; }

    public int getScore() { return score; }

    protected UserStats user;
    protected int score = 0;
    public int questionsAnswered  = 0;
    protected List <Double> timesList = new ArrayList<>();
    protected int maxQuestions;

    public char signal;
}

public class QuizGame implements HelperPrimitive {
    QuizGame() { initialized = false; }

    public static QuizGamedata getGamedataOf(UserStats stats) {
        if (!gamedataMap.containsKey(stats)) return null;
        return gamedataMap.get(stats);
    }

    public static int userCompare(UserStats x, UserStats y) {
        QuizGamedata gamedata_x = getGamedataOf(x);
        QuizGamedata gamedata_y = getGamedataOf(y);
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

    public static List <UserStats> sortGameroomUsers(Integer ID) {
        List <UserStats> gameroomUsers = new ArrayList<>();
        for (UserStats peer : GameroomManager.getGameroomFriends(ID)) {
            if (peer.isPlayer()) gameroomUsers.add(peer);
        }
        gameroomUsers.sort(QuizGame::userCompare);
        return gameroomUsers;
    }

    public int maxQuestions = 10;
    protected static Map <UserStats, QuizGamedata> gamedataMap = new HashMap<>();
    public static List <String> rawQuestions = new ArrayList<>();

    protected String readPath = "questions.txt";

    protected AsyncParser parser = null;
    protected static char beginSignal   = 'B';
    protected static char restartSignal = 'R';
    protected static char finishSignal  = 'F';
    protected static char updateSignal  = 'U';
    protected char signal = 'Q';

    protected static String beginAnswer   = "QB";
    protected static String restartAnswer = "QR";
    protected static String updateAnswer  = "QU";
    protected static String selfUpdateAnswer  = "QS";
    protected static String finishAnswer  = "QF";
    protected static String selfFinishAnswer  = "QT";
    protected static String abortAnswer  = "QA";
    protected static String printAnswer  = "QP";
    protected static String rankingAnswer  = "QK";

    public Boolean initialized;
    protected void initializeClass(Session session) throws IOException {
        parser = new AsyncParser(readPath);
    }

    static public boolean isInvalid(UserStats stats)  { return !gamedataMap.containsKey(stats); }

    protected void abortGame(Integer ID, UserStats exception) throws IOException {
        GameroomManager.sendExclusiveMessage(ID, exception,"TQ");
        GameroomManager.sendExclusiveMessage(ID, exception,printAnswer + QuizUtilities.createRankingMessage(ID));
        GameroomManager.sendMessageToMaster (ID, abortAnswer);
        for (UserStats stats : GameroomManager.getGameroomFriends(ID)) gamedataMap.remove(stats);
    }
    protected void abortGame(Session session) {
        try {
            session.getBasicRemote().sendText(printAnswer + QuizUtilities.createRankingMessage(UserSession.getStats(session)));
            session.getBasicRemote().sendText("TQ");
            session.getBasicRemote().sendText(abortAnswer);
        }   catch (IOException e) {}
        gamedataMap.remove(session);
    }

    protected void updateRoom(Integer ID, UserStats exception) throws IOException {
        boolean bAbort = true;
        for (UserStats stats : GameroomManager.getGameroomFriends(ID)) {
            if (!gamedataMap.containsKey(stats)) continue;
            QuizGamedata gamedata = gamedataMap.get(stats);
            if (gamedata == null) continue;
            if (stats.isPlayer() && !gamedata.isFinished())
                bAbort = false;
        }

        List <UserStats> arr = sortGameroomUsers(ID);
        for (int i=0; i<arr.size(); ++i) {
            UserStats stats = arr.get(i);
            if (stats != exception) stats.sendMessageToSessions(rankingAnswer + "#" + (i+1) + "/" + arr.size());
        }

        GameroomManager.sendExclusiveMessage(ID, exception, "" + bAbort);

        String leaderboardData = QuizUtilities.createRankingMessage(ID);
        for (UserStats peer : GameroomManager.getGameroomFriends(ID)) {
            if (peer == exception) continue;
            peer.sendMessageToSessions(printAnswer + leaderboardData);
        }

        if (bAbort) {
            GameroomManager.sendExclusiveMessage(ID, exception, "PPJocul s-a terminat!");
            abortGame(ID, exception);
        }
    }

    public boolean isCalled(String str) { return str.charAt(0) == signal; }

    public void open(Session session) throws IOException, EncodeException {
        if (!initialized) initializeClass(session);
    }
    public void close(Session session) throws IOException, EncodeException {
        if (gamedataMap.containsKey(UserSession.getStats(session))) {
            UserStats stats = UserSession.getStats(session);
            if (getGamedataOf(UserSession.getStats(session)) == null) return;
            if (!(getGamedataOf(stats).signal == signal)) return;
            if (GameroomManager.checkPlayerGameroomEmpty(stats)) {
                gamedataMap.remove(stats);
            }
            else {
                GameroomManager.sendExclusiveMessage(UserSession.getStats(session).getID(), UserSession.getStats(session), "" + getGamedataOf(UserSession.getStats(session)).signal);
                gamedataMap.remove(UserSession.getStats(session));
                GameroomManager.sendExclusiveMessage(UserSession.getStats(session).getID(), UserSession.getStats(session), "map close task 1 done");
                updateRoom(UserSession.getStats(session).getID(), UserSession.getStats(session));
                GameroomManager.sendExclusiveMessage(UserSession.getStats(session).getID(), UserSession.getStats(session), "map close task 2 done");
            }
        }
    }

    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        if (parser != null) {
            if (parser.isFinished) {
                initialized = true;
                for (String line:parser.readData.split("\n"))
                { rawQuestions.add(line.trim()); }
                parser = null;
            }
        }

        if (message.charAt(0) == beginSignal || message.charAt(0) == restartSignal) {
            UserStats stats = UserSession.getStats(session);

            boolean isValidMaster = (stats.isMaster() && GameroomManager.checkMasterGameroom(stats));
            boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));
            if (GameroomManager.checkPlayerGameroomEmpty(stats)) UserSession.debugPrint(session,"Gameroom is considered empty");
            if (stats.isPlayer()) UserSession.debugPrint(session, "Session is player");

            if (stats.isPlayer() && !isValidPlayer) {
                session.getBasicRemote().sendText("PPNu poți folosi acest buton; lasă-l pe profesor să înceapă.");
            }
            if (stats.isMaster() && !isValidMaster) {
                session.getBasicRemote().sendText("PPAcest buton nu poate fi folosit, fiindcă gameroom-ul este gol.");
            }

            if (isValidMaster || isValidPlayer) {
                int ID = stats.getID();
                UserSession.debugPrint(session,"Here I am");

                StringBuilder entryCode = new StringBuilder();
                for (String entry:QuizUtilities.generateEntries())
                { entryCode.append(entry); entryCode.append('#'); }

                GameroomManager.toggleGameroomState(session);

                if (isValidMaster) {
                    if (message.charAt(0) == beginSignal) GameroomManager.sendInclusiveMessage(ID, beginAnswer   + entryCode.toString());
                    else                                  GameroomManager.sendInclusiveMessage(ID, restartAnswer + entryCode.toString());
                    for (UserStats peerStats : GameroomManager.getGameroomFriends(stats.getID()))
                        gamedataMap.put(peerStats, new QuizGamedata(peerStats, maxQuestions, signal));
                }
                else {
                    if (message.charAt(0) == beginSignal) session.getBasicRemote().sendText(beginAnswer   + entryCode.toString());
                    else                                  session.getBasicRemote().sendText(restartAnswer + entryCode.toString());
                    gamedataMap.put(stats, new QuizGamedata(stats, maxQuestions, signal));
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
            session.getBasicRemote().sendText(selfUpdateAnswer + data.answerString());
            GameroomManager.sendExclusiveMessage(ID, stats,updateAnswer + data.answerString());

            if (data.isFinished()) {
                UserSession.getStats(session).toggleGameState();
                if (ID != 0)
                    session.getBasicRemote().sendText("PPAi terminat jocul! Acum așteaptă și pe ceilalți colegi ai tăi să termine.");
                session.getBasicRemote().sendText(selfFinishAnswer + data.answerString());
                GameroomManager.sendExclusiveMessage(ID, stats,finishAnswer + data.finishString());
            }
        }

        UserStats stats = UserSession.getStats(session);
        boolean isValidPlayer = (stats.isPlayer() && GameroomManager.checkPlayerGameroomEmpty(stats));
        if (isValidPlayer) {
            QuizGamedata gamedata = gamedataMap.get(stats);
            if (gamedata != null) {
                if (gamedata.isFinished()) abortGame(session);
            }
        }   else
        if (gamedataMap.containsKey(UserSession.getStats(session))) updateRoom(UserSession.getStats(session).getID(), null);
    }
}