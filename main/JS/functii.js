let globalID = "-";
let gobacklink = 0;

let LONG_MESSAGE_LENGTH = 50;
let intvPersistance = false;

let gameroomNameSet = new Set();
function addGRName(string) {
    gameroomNameSet.add(string);
}
function removeGRName(string) {
    gameroomNameSet.delete(string);
}
function resetGRSet(string) {
    gameroomNameSet.clear();
    addGRName(getSessionData("username"));
}
function initializeSet(rawstring) {
    gameroomNameSet.clear();
    rawstring.toString().split("#").forEach(function (name) {
        if (name.length > 4)
            gameroomNameSet.add(name);
    });
}
function getGameroomNameList() {
    if (gameroomNameSet.size === 1) return "Se pare ca esti pe cont propriu!";

    let ans = "Lista de oameni din gameroom: (" + gameroomNameSet.size + ")\n";

    let idx = 1;
    for (let name of gameroomNameSet.values())  {
        ans += idx + ". " + name;
        if (name === getSessionData("username")) ans += " (asta esti tu!)";
        ans += '\n';
        idx ++;
    }
    return ans;
}
function updateGameroomList() {
    addGRName(getSessionData("username"));
    document.getElementById('players').innerText = getGameroomNameList();
}

let wsString = "ws://localhost:8080/proiect_site_spaniola_war_exploded/ws";

//  sometimes messages aren't probably sent through webscokets; this is a variable used as a last resort
let wsInitializationInterval = setInterval(function(){},100000);
function onloadGeneralTasks() {
    wsValidity = false;
    wsInitializationInterval = setInterval(function(){retryWSInitialization();},5000);
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

function updateGlobal() {
    document.getElementById("gameroomIDUI").style.display = "block";
    document.getElementById("gameroomIDUI").innerHTML = globalID;
}

let printInterval = setInterval(function() {}, 30000);
function showComentariu(message) {
    document.getElementById("comentarii").style.display = "flex";
    document.getElementById("comentariitext").innerText = message;
}
function hideComentariu(message) {
    document.getElementById("comentarii").style.display = "none";
}

function showInformation(message, persistance = false) {
    if (intvPersistance) return;
    intvPersistance = persistance;
    clearInterval(printInterval);
    showComentariu(message);
    var timeout = 4000;
    if (message.toString().length >= LONG_MESSAGE_LENGTH) timeout *= 2;
    printInterval = setInterval(function() { hideComentariu(); intvPersistance = false; clearInterval(printInterval);}, timeout);
}
function setCustomInfoClean() {
    clearInterval(printInterval);
}

function openForm() {
    document.getElementById("myForm").style.display = "block";
    document.getElementById("removee").style.display ="none";
    if (isMaster()) {
        updateGlobal();
        document.getElementById("GRinput").style.display = "none";
    }
    else {
        document.getElementById("GRinput").style.display = "block";
        document.getElementById("GRinput").innerText = globalID;
    }
}
function closeForm() {
    if (!isMaster()) {
        globalID = document.getElementById("GRinput").value.toString().trim();
        if (!debugWithoutWS) ws.send("AI" + globalID);
    }
    document.getElementById("myForm").style.display = "none";
    document.getElementById("removee").style.display = "block";
}
function color() {
    document.getElementById("harta").style.color="white";
}


function redirectToLogin() {
    window.location.replace("login.html");
}


function isMaster() {
    return getSessionData("job") === "master";
}
function setMaster() {
    if (!debugWithoutWS) ws.send("AJP");
    setSessionData("job", "master");
}
function setPlayer() {
    if (!debugWithoutWS) ws.send("AEJ");
     setSessionData("job", "player");
}
function setMasterName(name) {
    if (!debugWithoutWS) ws.send("AN" + name);
    addGRName(name);
    setSessionData("username", name)
}
function setPlayerName(name) {
    if (!debugWithoutWS) ws.send("AN" + name);
    addGRName(name);
    setSessionData("username", name);
}

function getSessionKeyData() {
    return getSessionData("username") + "#" + getSessionData("job");
}

var wsValidity = false;
function isSessionInvalid() {
    return wsValidity;
}

//  functions replacing the cookie functions pre-update
function setSessionData(cname, cvalue) {
    window.sessionStorage.setItem(cname, cvalue);
}
function getSessionData(cname) {
    let ptr = window.sessionStorage.getItem(cname);
    if (ptr === null) return "";
    return ptr;
}

function boom(){
    document.getElementById("okk").style.display ="block";
}

function nolink(){
    gobacklink++;
}

function goback(){
    window.history.go(-gobacklink-1);
}



var buttonplayersState = false;
function showplayers(){
    if(buttonplayersState === false){
        document.getElementById('players').style.opacity = "1";
        document.getElementById('players').style.marginLeft = "0";
        updateGameroomList();
        buttonplayersState = true;
    }
    else{
        document.getElementById('players').styleopacity = "0";
        document.getElementById('players').style.marginLeft = "150vh";
        buttonplayersState = false;
    }
}