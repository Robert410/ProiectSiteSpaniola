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

function getTime() {
    var clock = new Date();
    return clock.getTime();
}
var lastTime = getTime();

let questions = [
    {   question:"template1",
        choiceA:"A1",
        choiceB:"B1",
        choiceC:"C1",
        choiceD:"D1",
        corect:"A"
    },
    {   
        question:"template2",
        choiceA:"A2",
        choiceB:"B2",
        choiceC:"C2",
        choiceD:"D2",
        corect:"D"
    },
    {   question:"template3",
        choiceA:"A3",
        choiceB:"B3",
        choiceC:"C3",
        choiceD:"D3",
        corect:"C"
    }   
];

let isAlone = true;
let isGameFinished = false;
let job            = false;
function q_isMaster()  { return job; }
function toggleJob()   { job = !job; updateQuiz(); }
function toggleGame()  { isGameFinished = !isGameFinished; updateQuiz(); }
function toggleAlone() { isAlone = !isAlone; updateQuiz(); }

let finishedMessage       = "Press >>Arata Rezultate<< for leaderboard stats!";
let unfinishedMessage     = "Waiting for everyone to finish the quiz... >>Arata Rezultate<< button is currently unavailable."
let finishedMasterMessage = "Press >>Arata Rezultate<< to show the leaderboard of your gameroom. Press >>Reincearca<< to restart the quiz for all your students."

// totalQuestions has a fixed value X; the servers sends > X number of questions; only X are chosen for diversity
const totalQuestions = 9;
let   curentQuestion = 0;
let   score = 0;
let   restartDone = 0;
let   leaderboardRawData = "";

start.addEventListener("click",startQuizButton);

function startQuizButton() {
    if (debugWithoutWS) startQuiz();
    else if (wsValidity) ws.send("QB");
}
function restartQuizButton() {
    if (debugWithoutWS) restartQuiz();
    else ws.send("QR");
}
function abortQuiz() { showScore();  }


function showQuestion()
{
    let aux=questions[curentQuestion];
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
function startQuiz()
{
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

    //  added because due to gameroom management a player might reach the restart screen but the master might actually press the start button
    if (restartDone == 1) {
        for(let quesIndex=0;quesIndex<=totalQuestions;quesIndex++)  {
            document.getElementById(quesIndex).style.backgroundColor="transparent";
        }
    }

    //  added from restartQuiz() for the same reason
    final.style.display="none";
    curentQuestion = 0;
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
    if(restartDone==0)
    {
        showProgress();
    }
}
function showProgress()
{
    for(let quesIndex=0;quesIndex<=totalQuestions;quesIndex++)
    {
        progress.innerHTML+="<div class='prog' id="+quesIndex+"></div>";
        restartDone = 1;
    }
}
function checkAnswer(answer)
{
    if (q_isMaster()) return;

    if(answer==questions[curentQuestion].corect)
    {
        score++;
        corectAnswer();
    }
    else
        wrongAnswer();
    
    //  to update the server data
    var timeValue = getTime();
    if (!debugWithoutWS) ws.send("QU" + score + "#" + curentQuestion + "#" +  (timeValue - lastTime)*0.001);
    lastTime = timeValue;
    if(curentQuestion<totalQuestions)
    {
        curentQuestion++;
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

function corectAnswer()
{
    document.getElementById(curentQuestion).style.backgroundColor="green";
}
function wrongAnswer()
{
    document.getElementById(curentQuestion).style.backgroundColor="red";
}
function showScore()
{
    start.style.display="none";
    quiz.style.display="none";
    scoreHtml.style.display="block";
    updateQuiz();
    restart.style.display="block";
    //  final.style.display="block";
    final.addEventListener("click",showFinal);
    restart.addEventListener("click",restartQuizButton);

    showFinal();
}
function restartQuiz()
{
    startQuiz();
}
function showFinal()
{
    rankingUI.innerHTML = "";

    mesajFinal.style.display="inline";
    scoreHtml.style.display="none";
    final.style.display="none";

    mesajFinal.innerHTML = createLeaderboardData(leaderboardRawData);
}

//  utility functions
//      rawdata format: {{user_name}: {score} points#} for each user
function setLeaderboardRawData(rawdata) {
    leaderboardRawData = rawdata;
    mesajFinal.innerHTML = createLeaderboardData(leaderboardRawData);
}
function createLeaderboardData(rawdata) {
    ans = "<p>Top Elevi</p>" + "<p> </p>";
    rawdata.toString().split("#").forEach(function (rawline) {
        ans += "<p>" + rawline + "</p>";
    }); return ans;
}
function updateRanking(rank) { rankingUI.innerHTML = "<p>" + rank + "</p>"; }
function updateQuiz() {
    let flag = false;
    if (scoreHtml.style.display="none") flag = true;
    if (!q_isMaster()) {
        if (isGameFinished || isAlone) scoreHtml.innerHTML = finishedMessage;
        else                           scoreHtml.innerHTML = unfinishedMessage;
    }
    else {
        scoreHtml.innerHTML = finishedMasterMessage;
    }
    if (flag) scoreHtml.style.display = "none";
}
//  rawdata is read from questions.txt; format is: {each line from questions.txt + "#"}
function initializeQuestions(rawdata) {
    questions = [];
    rawdata.toString().split("#").forEach(function (rawquestion) {
        arr = rawquestion.split("$");
        if (arr.length != 6) return;
        questions.push({question:arr[0].toString().trim(), choiceA:arr[1].toString().trim(), choiceB:arr[2].toString().trim(),
            choiceC :arr[3].toString().trim(), choiceD:arr[4].toString().trim(), corect :arr[5].toString().trim()});
    });
}
