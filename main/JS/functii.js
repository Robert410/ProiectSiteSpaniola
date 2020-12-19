let globalID = "-";
let gobacklink = 0;


function updateGlobal() {
    document.getElementById("gameroomIDUI").style.display = "block";
    document.getElementById("gameroomIDUI").innerHTML = globalID;
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
