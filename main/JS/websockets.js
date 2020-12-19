let wsString = "ws://localhost:8080/proiect_site_spaniola_war_exploded/ws";


let strID = "";
function getStrID() {
    return strID;
}


let debugWithoutWS = false;
let consoleMode = false;
let wsInitializationInterval = setInterval(function(){},100000);
let wsInitializationInterval2 = setInterval(function(){},100000);

var wsValidity = false;


function wsStartQuiz(maxQuestions, totalQuestions) {
    ws.send("QB#" + maxQuestions + "#" + totalQuestions);
}
function wsUpdateQuiz(score, currentQuestion, time) {
    ws.send("QU" + score + "#" + currentQuestion + "#" +  time);
}


function wsStartMap(maxQuestions, totalQuestions) {
    ws.send("MB#" + maxQuestions + "#" + totalQuestions);
}
function wsRestartMap(maxQuestions, totalQuestions) {
    ws.send("MR#" + maxQuestions + "#" + totalQuestions);
}
function wsUpdateMap(score, currentQuestion, time) {
    ws.send("QU" + score + "#" + currentQuestion + "#" + time);
}


function onloadGeneralTasks() {
    wsValidity = false;
    try {
        retryWSInitialization();
    }
    catch (err) { }

    wsInitializationInterval2 = setInterval(function(){retryWSInitialization();},500);
    wsInitializationInterval = setInterval(function(){retryWSInitialization();},4000);
    if (getSessionData("username").length < 4) {
        redirectToLogin();
    }
    if (consoleMode) {
        document.getElementById("log").style.display = "inline-block";
        document.getElementById("input").style.display = "inline-block";
        document.getElementById("log").value += getSessionKeyData() + isMaster() + getSessionData("job") + "#" + "\n";
    }
    else {
        document.getElementById("log").style.display = "none";
        document.getElementById("input").style.display = "none";
    }
    if (isMaster()) {
        document.getElementById("GRinput").style.display = "none";
    }
}


function checkGameroomEvents(code, value) {
    if (code === "GS") {            // entering a new gameroom
        initializeSet(value);
        updateGameroomList();
    }   else
    if (code === "GD") {            // gameroom friend welcome
        addGRName(value);
        showInformation(value.split("!")[0] + " a intrat în gameroom!");
        updateGameroomList();
    }   else
    if (code === "GM") {
        resetGRSet();
        updateGameroomList();
    }   else
    if (code === "GX") {            // gameroom friend exit
        showInformation(getGameroomName(value) + " a ieșit din gameroom!");
        removeGRName(value);
        updateGameroomList();
    }
}


function checkGeneralEvents(code, value) {
    if (code === "ID") {            // updating ID
        globalID = value;
        updateGlobal();
    }   else
    if (code === "VV") {            // succeeded in ws handshake and data initialization
        wsValidity = true;
        strID = value;
        wsInitializationInterval = clearInterval(wsInitializationInterval);
        wsInitializationInterval2 = clearInterval(wsInitializationInterval2);
    }   else
    if (code === "XX") {            // duplicate client: redirecting to login
        redirectToLogin();
    }   else
    if (code === "PP") {            // request of showing an information message with a given index from ppArr
        showInformation(ppArr[parseInt(value)-1]);
    }
}