//  ========== DATA DEFINITION ==========
let selected_quiz = "custom";

let time_value = 0;

const start      = document.getElementById("start");
const quiz       = document.getElementById("quiz");
const question   = document.getElementById("question");
const choiceA    = document.getElementById("A");
const choiceB    = document.getElementById("B");
const choiceC    = document.getElementById("C");
const choiceD    = document.getElementById("D");
const progress   = document.getElementById("progress");
const scoreHtml  = document.getElementById("score");
const restart    = document.getElementById("restart");
const final      = document.getElementById("showLeaderboard");
const mesajFinal = document.getElementById("finish");
const rankingUI  = document.getElementById("rankingUIText");

let isAlone = true;
let isGameFinished = false;
let job            = false;

let   totalQuestions = 9;
let   currentQuestion = 0;
let   score = 0;
let   restartDone = 0;
let   leaderboardRawData = "";

let finishedMessage       = "Press >>Arata Rezultate<< for leaderboard stats!";
let unfinishedMessage     = "Waiting for everyone to finish the quiz... >>Arata Rezultate<< button is currently unavailable.";
let finishedMasterMessage = "Press >>Arata Rezultate<< to show the leaderboard of your gameroom. Press >>Reincearca<< to restart the quiz for all your students.";

let questions = [{question:"template1", choiceA:"A1", choiceB:"B1", choiceC:"C1", choiceD:"D1", corect:"A"}];
//  ^^^^^^^^^^ DATA DEFINITION ^^^^^^^^^^


//  ========== TIME FUNCTIONS ==========
let timeLimitInterval = setInterval(function() {}, 30000);
let lastTime = getTime();


    //  if too much time is spent on the quiz, the quiz will be forcefully closed by sending appropriate data to the backend
    //  and displaying a message to the frontend client
function timeAbort() {
    for (let i=currentQuestion; i<=totalQuestions; ++i)
        checkAnswer("Z");
    showScore();
    showInformation("Au trecut 3 minute - quiz-ul s-a oprit automat.", true)
    clearInterval(timeLimitInterval);
}


function getTime() {
    let clock = new Date();
    return clock.getTime();
}
//  ^^^^^^^^^^ TIME FUNCTIONS ^^^^^^^^^^


//  ========== BUTTON ONCLICK FNS ==========
function startQuizButton() {
    totalQuestions = quizStrings[selected_quiz]["cnt_select"] - 1;
    if (debugWithoutWS) startQuiz();
    else if (wsValidity) wsStartQuiz(quizStrings[selected_quiz]["questions"].length, quizStrings[selected_quiz]["cnt_select"]);
}
function restartQuizButton() {
    if (debugWithoutWS) restartQuiz();
    else wsStartQuiz(quizStrings[selected_quiz]["questions"].length, quizStrings[selected_quiz]["cnt_select"]);
}
//  ^^^^^^^^^^ BUTTON ONCLICK FNS ^^^^^^^^^^


//  ========== QUIZ FUNCTIONS ==========
function q_isMaster()        { return job; }
function q_toggleJob()       { job = !job; updateQuiz(); }
function q_toggleGame()      { isGameFinished = !isGameFinished; updateQuiz(); }
function q_toggleAlone()     { isAlone = !isAlone; updateQuiz(); }
function updateRanking(rank) { rankingUI.innerHTML = "<p>" + rank + "/" + (gameroomNameSet.size-1) + "</p>"; }


function startQuiz()
{
    time_value = 0;
    updateGameroomQuizData();

    if (!q_isMaster())
        timeLimitInterval = setInterval(timeAbort, 1000*60*3);

    hideComentariu();
    intvPersistance = false; clearInterval(printInterval);
    lastTime = getTime();

    leaderboardRawData = "";
    if (q_isMaster()) {
        showScore();
        restart.style.display="none";
        return;
    }

    isGameFinished = false;

    mesajFinal.style.display = "none";
    rankingUI.innerHTML = "";

    if (restartDone === 1) {
        for(let quesIndex=0;quesIndex<=totalQuestions;quesIndex++)  {
            document.getElementById(quesIndex).style.backgroundColor="transparent";
        }
    }

    final.style.display="none";
    currentQuestion = 0;
    score = 0;
    restart.style.display="none";
    start.style.display="none";
    scoreHtml.style.display="none";
    showQuestion();
    quiz.style.display="block";

    question.classList.add('questioon');
    choiceA.classList.add('answer');
    choiceB.classList.add('answer');
    choiceC.classList.add('answer');
    choiceD.classList.add('answer');
    void question.offsetWidth;
    void choiceA.offsetWidth;
    void choiceB.offsetWidth;
    void choiceC.offsetWidth;
    void choiceD.offsetWidth;
    if(restartDone === 0)
    {
        showProgress();
    }
}


function updateQuiz() {
    let flag = false;
    if (scoreHtml.style.display === "none") flag = true;
    if (!q_isMaster()) {
        if (isGameFinished || isAlone) scoreHtml.innerHTML = finishedMessage;
        else                           scoreHtml.innerHTML = unfinishedMessage;
    }
    else {
        scoreHtml.innerHTML = finishedMasterMessage;
    }
    if (flag) scoreHtml.style.display = "none";
}


    //  called everytime the next questions should appear
function showQuestion()
{
    let aux=questions[currentQuestion];
    question.innerHTML="<p>"+aux.question+"</p>";
    choiceA.innerHTML=aux.choiceA;
    choiceB.innerHTML=aux.choiceB;
    choiceC.innerHTML=aux.choiceC;
    choiceD.innerHTML=aux.choiceD;

    if (aux.choiceC === '-') {
        choiceC.style.display = "none";
    }
    else {
        choiceC.style.display = "flex";
    }
    if (aux.choiceD === '-') {
        choiceD.style.display = "none";
    }
    else {
        choiceD.style.display = "flex";
    }

    question.classList.remove('questioon');
    choiceA.classList.remove('answer');
    choiceB.classList.remove('answer');
    choiceC.classList.remove('answer');
    choiceD.classList.remove('answer');

    void question.offsetWidth;
    void choiceA.offsetWidth;
    void choiceB.offsetWidth;
    void choiceC.offsetWidth;
    void choiceD.offsetWidth;
}


function showProgress()
{
    for(let quesIndex=0;quesIndex<=totalQuestions;quesIndex++)
    {
        progress.innerHTML+="<div class='prog' id="+quesIndex+"></div>";
        restartDone = 1;
    }
}


    //  called everytime the user clicks on any question possible answer
function checkAnswer(answer)
{
    if (q_isMaster()) return;

    if(answer === questions[currentQuestion].corect)
    {
        score++;
        corectAnswer();
    }
    else
        wrongAnswer();

    let timeValue = getTime();
    if (!debugWithoutWS) wsUpdateQuiz(score, currentQuestion, (timeValue - lastTime)*0.001);
    time_value += (timeValue - lastTime)*0.001;
    lastTime = timeValue;

    if(currentQuestion<totalQuestions)
    {
        currentQuestion++;
        showQuestion();
    }
    else
        showScore();

    question.classList.add('questioon');
    choiceA.classList.add('answer');
    choiceB.classList.add('answer');
    choiceC.classList.add('answer');
    choiceD.classList.add('answer');

    void question.offsetWidth;
    void choiceA.offsetWidth;
    void choiceB.offsetWidth;
    void choiceC.offsetWidth;
    void choiceD.offsetWidth;
}


    //  called by checkAnswer() if the answer was good
function corectAnswer() { document.getElementById(currentQuestion).style.backgroundColor="green"; }
    //  called by checkAnswer() if the answer was bad
function wrongAnswer() { document.getElementById(currentQuestion).style.backgroundColor="red"; }


    //  aborts the quiz and shows the final results
function showScore()
{
    clearInterval(timeLimitInterval);
    start.style.display="none";
    quiz.style.display="none";
    scoreHtml.style.display="block";

    updateQuiz();

    restart.style.display="block";
    restart.addEventListener("click",restartQuizButton);

    rankingUI.innerHTML = "";

    mesajFinal.style.display="inline";
    scoreHtml.style.display="none";
    final.style.display="none";
    updateLeaderboardHTML();
}
//  ^^^^^^^^^^ QUIZ FUNCTIONS ^^^^^^^^^^


//  ========== BACKEND ==========
    // rawdata: format "{str}${str}$...", where each str represents a number (0-indexed permutation of questions)
function initializeQuestions(rawdata) {
    questions = [];
    for (let i=0; i<rawdata.toString().length; i += 2) {
        str = "";
        if (i+1 < rawdata.toString().length)
            str = rawdata.toString().substr(i, 2);
        if (str.length !== 0) {
            qdata = quizStrings[selected_quiz]["questions"][parseInt(str)];
            arr = [qdata["text"], qdata["A"], qdata["B"], qdata["C"], qdata["D"], qdata["correct"]];
            if (arr.length !== 6) return;
            questions.push({question:arr[0].toString().trim(), choiceA:arr[1].toString().trim(), choiceB:arr[2].toString().trim(),
                choiceC :arr[3].toString().trim(), choiceD:arr[4].toString().trim(), corect :arr[5].toString().trim()});
        }
    }
}
//  ^^^^^^^^^^ BACKEND ^^^^^^^^^^

//  ========== LEADERBOARD UTIL ==========
let q_GameroomScores = new Map();
let q_GameroomTimes = new Map();
let q_GameroomCheckpoint = new Map();


function getScoreOf(strID) {
    if (!(strID in q_GameroomScores))
        return 0;
    return q_GameroomScores[strID];
}
function getTimeOf(strID) {
    if (!(strID in q_GameroomScores))
        return 0;
    return q_GameroomTimes[strID];
}
function getCheckpointOf(strID) {
    if (!(strID in q_GameroomScores))
        return 0;
    return q_GameroomCheckpoint[strID];
}


function setScore(strID, value) {
    q_GameroomScores[strID] = parseInt(value);
}
function setTime(strID, value) {
    q_GameroomTimes[strID] = parseFloat(value);
}
function setCheckpoint(strID, value) {
    q_GameroomCheckpoint[strID] = parseInt(value);
}


function updateLeaderboardData(leaderboardData) {
    addGRName(getSessionData("username") + "!" + getStrID());
    leaderboardData.toString().split("#").forEach(function (rawData) {
        if (rawData.length < 3)
            return;
        let arr = rawData.split("!");

        setScore(arr[0], arr[1]);
        setCheckpoint(arr[0], arr[2]);
        setTime(arr[0], arr[3]);
    });
}


function updateLeaderboardHTML() {
    if (gameroomNameSet.size <= 1) {
        let output = "<p>Top Elevi</p>" + "<p> </p>";
        output += "<p>" + getSessionData("username") + " - " + score + " points, " + (totalQuestions+1) + "/" + (totalQuestions+1) + ", " + time_value + "s</p>";
        mesajFinal.innerHTML = output;
        return;
    }

    let boardData = []
    for (let gameroomData of gameroomNameSet) {
        if (gameroomData[1].toString().substr(0, 1) !== "^")
            boardData.push([gameroomData[0], getScoreOf(gameroomData[1]), getCheckpointOf(gameroomData[1]), getTimeOf(gameroomData[1])]);
    }

    boardData.sort(function (a, b) {
        if (a[1] === b[1]) {
            return Math.round(a[3]*100000) - Math.round(b[3]*100000);
        }   return b[1] - a[1];
    });

    let output = "<p>Top Elevi</p>" + "<p> </p>";
    for (let player of boardData) {
        output += "<p>" + player[0] + " - " + player[1] + " points, " + player[2] + "/" + (totalQuestions+1) + ", " + player[3] + "s</p>";
    }

    mesajFinal.innerHTML = output;
}


function updateGameroomQuizData() {
    q_GameroomScores.clear();
    q_GameroomTimes.clear();
    q_GameroomCheckpoint.clear();
}
//  ^^^^^^^^^^ LEADERBOARD UTIL ^^^^^^^^^^