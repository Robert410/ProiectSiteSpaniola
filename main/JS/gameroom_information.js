let gameroomNameSet = new Set();


function getGameroomID(userName) {
    for (let name of gameroomNameSet.values()) {
        if (name[0] === userName)
            return name[1];
    }   return null;
}
function getGameroomName(ID) {
    for (let name of gameroomNameSet.values()) {
        if (name[1] === ID)
            return name[0];
    }   return null;
}


function addGRName(string) {
    let arr = string.split("!");
    if (getGameroomID(arr[0]) != null) return;
    gameroomNameSet.add([arr[0], arr[1]]);
}
function removeGRName(string) {
    for (let obj of gameroomNameSet) {
        if (obj[1] === string) {
            gameroomNameSet.delete(obj);
        }
    }
}


function resetGRSet() {
    gameroomNameSet.clear();
    addGRName(getSessionData("username") + "!" + getStrID());
}


function initializeSet(rawstring) {
    gameroomNameSet.clear();
    rawstring.toString().split("#").forEach(function (name) {
        if (name.length > 6)
            addGRName(name);
    });
}


function getGameroomNameList() {
    if (gameroomNameSet.size <= 1) return "Se pare ca esti pe cont propriu!";

    let ans = "Lista de oameni din gameroom: (" + gameroomNameSet.size + ")\n";

    let idx = 1;
    for (let setData of gameroomNameSet.values())  {
        name = setData[0];
        ans += idx + ". " + name;
        if (name === getSessionData("username")) ans += " (asta esti tu!)";
        ans += '\n';
        idx ++;
    }
    return ans;
}


function updateGameroomList() {
    addGRName(getSessionData("username") + "!" + getStrID());
    document.getElementById('players').innerText = getGameroomNameList();
}


var buttonplayersState = false;
function showplayers(){
    if(buttonplayersState === false){
        document.getElementById('players').style.opacity = "1";
        document.getElementById('players').style.marginLeft = "0";
        document.getElementById('gameroomON').style.zIndex = "4";
        updateGameroomList();
        buttonplayersState = true;
    }
    else{
        document.getElementById('players').styleopacity = "0";
        document.getElementById('players').style.marginLeft = "150vh";
        document.getElementById('gameroomON').style.zIndex = "-1";
        buttonplayersState = false;
    }
}