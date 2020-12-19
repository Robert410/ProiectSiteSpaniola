//  ========== DATA DEFINITION ==========
let isAnimating = false;
let selected_map = "basic";

let totalQuestions = 17;
let time_value = 0;

let isAlone = true;
let isGameFinished = false;
let job            = false;
let currentQuestion = 0;
let restartDone = false;
let score = 0;
let leaderboardRawData = "";
let locatii = [
    "ES-AN","ES-AR","ES-AS","ES-CA","ES-CB","ES-CL","ES-CM",
    "ES-CT","ES-EX","ES-GA","ES-RI","ES-MD","ES-MC",
    "ES-NC","ES-IB","ES-PV","ES-VC"
];
let fullNameUpdated = [
    "Andalucía", "Aragón", "Asturia", "Canarias", "Cantabria", "Castilla y León", "Castilla-La Mancha",
    "Cantaluna", "Extremadura", "Galicia", "La Rioja", "Madrid", "Murcia",
    "Navarra", "Islas Baleares", "Pais Vasco", "Comunidad Valenciana"
];
//  ^^^^^^^^^^ DATA DEFINITION ^^^^^^^^^^


//  ========== TIME FUNCTIONS ==========
let timeLimitInterval = setInterval(function() {}, 30000);
let lastTime = getTime();


    //  if too much time is spent on the quiz, the quiz will be forcefully closed by sending appropriate data to the backend
    //  and displaying a message to the frontend client
function timeAbort() {
    for (let i=currentQuestion; i<=totalQuestions; ++i)
        nextQuestion();
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
function startQuizButtonPress() {
    let n = json_mapData[selected_map]["data"].length;
    totalQuestions = n;
    if (debugWithoutWS) { startQuiz(); }
    else if (wsValidity) wsStartMap(n, n);
}
function restartQuizButtonPress() {
    let n = json_mapData[selected_map]["data"].length;
    if (debugWithoutWS) { restartQuiz(); }
    else wsRestartMap(n, n);

}
//  ^^^^^^^^^^ BUTTON ONCLICK FNS ^^^^^^^^^^


//  ========== QUIZ FUNCTIONS ==========
function color() {
    document.getElementById("harta").style.color="white";
}

function insulehover() { document.getElementById('insulahover').style.display="block"; }
function insulehoverback() { document.getElementById('insulahover').style.display="none"; }

function getLocationName(idx) { return fullNameUpdated[idx]; }
function updateRanking(rank) { document.getElementById("rankingUIText").innerText = rank; }

function q_isMaster()  { return job; }
function q_toggleJob()   { job = !job; updateQuiz(); }
function q_toggleGame()  { isGameFinished = !isGameFinished; updateQuiz(); }
function q_toggleAlone() { isAlone = !isAlone; updateQuiz(); }


function afiseazaHarta() {
    document.getElementById("harta").style.display="block";
    document.getElementById("butonstart").style.display="none";
}
function ascundeHarta() {
    document.getElementById("harta").style.display="none";
}


function startanimation(element) {
    element.classList.add("animateDescriptor");
    element.addEventListener( "animationend",  function() {
        element.classList.remove("animateDescriptor");
    } );
    if (isAnimating) {
        return;
    }

    isAnimating = true;

    // Start animation. Add a css class or do this by javascript or whatever

    // Assuming the animation duration is 2 seconds
    setTimeout(function() {
        isAnimating = false;
    }, 2000);
}
function startanimation1(element) {
    element.classList.add("animateDescriptor1");
    element.addEventListener( "animationend",  function() {
        element.classList.remove("animateDescriptor1");
    } );
    if (isAnimating) {
        return;
    }

    isAnimating = true;

    // Start animation. Add a css class or do this by javascript or whatever

    // Assuming the animation duration is 2 seconds
    setTimeout(function() {
        isAnimating = false;
    }, 2000);
}


function startQuiz() {
    time_value = 0;
    updateGameroomQuizData();

    if (!q_isMaster())
        timeLimitInterval = setInterval(timeAbort, 1000*60*3);
    hideComentariu();
    intvPersistance = false; clearInterval(printInterval);
    leaderboardRawData = "";
    if (q_isMaster()) {
        restartDone = true;
        document.getElementById("mapGuessGameQuestionHeader").innerText = "";
        showScore();
        document.getElementById("butonstart").style.display="none";
        document.getElementById("butonrestart").style.display = "none";
        document.getElementById("score").style.display = "none";
        return;
    }

    updateRanking("");
    score = 0;
    document.getElementById("scorIDprogres").innerHTML=score;
    currentQuestion = 0;
    afiseazaHarta();
    let leaderboard = document.getElementById("rezultate");
    leaderboard.style.display = "none";
    document.getElementById("butonrestart")  .style.display = "none";
    //  document.getElementById("butonrezultate").style.display = "none";
    document.getElementById("mapGuessGameQuestionHeader").innerText = "Găsește-mă | " + getLocationName(currentQuestion);
    document.getElementById("pergamentRezultate").style.display="none";
    restartDone = true;
}
function restartQuiz() { startQuiz(); }


function nextQuestion() {
    //  to update the server data
    let timeValue = getTime();
    if (!debugWithoutWS) wsUpdateMap(score, currentQuestion, (timeValue - lastTime)*0.001);
    time_value += (timeValue - lastTime)*0.001;
    lastTime = timeValue;

    ++ currentQuestion;
    if (currentQuestion === totalQuestions) {
        document.getElementById("mapGuessGameQuestionHeader").innerText = "";
        showScore();
    }
    else { document.getElementById("mapGuessGameQuestionHeader").innerText = "Găsește-mă | " + getLocationName(currentQuestion); }
}


function pressOn(option) {
    if (q_isMaster()) return;
    if(option === locatii[currentQuestion]) {
        corectAnswer();
    }
    else wrongAnswer();
}


function corectAnswer() {
    startanimation( document.getElementById('okk'));
    score += 1;
    document.getElementById("scorIDprogres").innerHTML=score;
    nextQuestion();
}
function wrongAnswer() {
    startanimation1( document.getElementById('okkfalse') );
    score -= 1;
    document.getElementById("scorIDprogres").innerHTML=score;
}


function showScore() {
    if (restartDone === false) return;
    clearInterval(timeLimitInterval);
    document.getElementById("mapGuessGameQuestionHeader").innerText = "";
    updateRanking("");
    ascundeHarta();
    document.getElementById("butonrestart")  .style.display = "block";
    showLeaderboard();
}


function showLeaderboard() {
    let leaderboard = document.getElementById("rezultate");
    leaderboard.style.display="block";
    updateLeaderboardHTML();
    document.getElementById("pergamentRezultate").style.display="block";
}
//  ^^^^^^^^^^ QUIZ FUNCTIONS ^^^^^^^^^^


//  ========== BACKEND ==========
function initializeQuestions(rawdata) {
    let order = [];
    let fullName = [];

    for (let region_data of json_mapData[selected_map]["data"]) {
        order.push(region_data["id"]);
        fullName.push(region_data["verbose"]);
    }

    locatii = [];
    fullNameUpdated = [];
    for (let i=0; i<rawdata.toString().length; i += 2) {
        let str_index = "";
        if (i+1 < rawdata.toString().length)
            str_index = rawdata.toString().substr(i, 2);
        if (str_index.length !== 0) {
            if (str_index[0] === '0')
                str_index = str_index.substr(1);
            let index = parseInt(str_index);
            fullNameUpdated.push(fullName[index]);
            locatii.push(order[index]);
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
    let leaderboard = document.getElementById("rezultate");

    if (gameroomNameSet.size <= 1) {
        let output = "<p>Top Elevi</p>" + "<p> </p>";
        output += "<p>" + getSessionData("username") + " - " + score + " points, " + (totalQuestions+1) + "/" + (totalQuestions+1) + ", " + time_value + "s</p>";
        leaderboard.innerHTML = output;
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

    leaderboard.innerHTML = output;
}


function updateGameroomQuizData() {
    q_GameroomScores.clear();
    q_GameroomTimes.clear();
    q_GameroomCheckpoint.clear();
}
//  ^^^^^^^^^^ LEADERBOARD UTIL ^^^^^^^^^^