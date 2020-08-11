package serverClasses;

public class MapGuessGame extends QuizGame {
    MapGuessGame() {
        maxQuestions = 17;
        readPath = "comunitati_autonome.txt";
        signal = 'M';
    }
}