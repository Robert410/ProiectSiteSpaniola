let ppArr = [
    "Profesorul a ieșit! Gameroom-ul nu mai este valabil, dar dacă te jucai, vei putea continua în modul single-player.",
    "Ai terminat jocul! Acum așteaptă și pe ceilalți colegi ai tăi să termine.",
    "Nu poți folosi acest buton; lasă-l pe profesor să înceapă.",
    "Acest buton nu poate fi folosit, fiindcă gameroom-ul este gol.",
    "Nu poti folosi acest buton; lasa-l pe profesor sa inceapa.",
    "Acest buton nu poate fi folosit, fiindca gameroom-ul este gol.",
    "Nu poți să te muți din gameroom până nu îți termini jocul!",
    "Gameroom-ul deja se joacă, nu te poți conecta acum :( Așteaptă să se termine jocul!",
    "Nu te afli pe pagina potrivită a gameroom-ului.",
    "Ai intrat în gameroom!",
    "ID-ul introdus nu există. Întreabă-l pe profesor ID-ul."
]

let LONG_MESSAGE_LENGTH = 50;
let intvPersistance = false;
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