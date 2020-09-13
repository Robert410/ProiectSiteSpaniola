const totalQuestions = 17;

let isAlone = true;
let isGameFinished = false;
let job            = false;
let curentQuestion = 0;
let restartDone = false;
let score = 0;
let leaderboardRawData = "";
let locatii = [
    "ES-AN","ES-AR","ES-AS","ES-CA","ES-CB","ES-CL","ES-CM",
    "ES-CT","ES-EX","ES-GA","ES-RI","ES-MD","ES-MC",
    "ES-NC","ES-IB","ES-PV","ES-VC"
];
let fullName = [
    "Andalusia", "Aragon", "Asturias", "Canarias", "Cantabria", "Castilla y Leon", "Castilla-La Mancha",
    "Cantaluna", "Extremadura", "Galicia", "Rioja", "Madrid", "Murcia",
    "Navarra", "Islas Baleares", "Pais Vasco", "Comunidad Valenciana"
];
let fullNameUpdated = [
    "Andalusia", "Aragon", "Asturias", "Canarias", "Cantabria", "Castilla y Leon", "Castilla-La Mancha",
    "Cantaluna", "Extremadura", "Galicia", "Rioja", "Madrid", "Murcia",
    "Navarra", "Islas Baleares", "Pais Vasco", "Comunidad Valenciana"
];

let translated = {
    "Aragon" : "Aragón",
    "Andalusia" : "Andalucía",
    "Asturias" : "Asturia",
    "Cantabria" : "Cantabria",
    "Canarias" : "Canarias",
    "Castilla y Leon" : "Castilla y León",
    "Castilla-La Mancha" : "Castilla-La Mancha",
    "Cantaluna" : "Cantaluna",
    "Extremadura" : "Extremadura",
    "Galicia" : "Galicia",
    "Rioja" : "La Rioja",
    "Madrid" : "Madrid",
    "Murcia" : "Murcia",
    "Navarra" : "Navarra",
    "Islas Baleares" : "Islas Baleares",
    "Pais Vasco" : "País Vasco",
    "Comunidad Valenciana" : "Comunidad Valenciana"
};


function getTime() {
    var clock = new Date();
    return clock.getTime();
}
var lastTime = getTime();


function getLocationName(idx) {
    return translated[fullNameUpdated[idx]];
}

function q_isMaster()  { return job; }
function toggleJob()   { job = !job; updateQuiz(); }
function toggleGame()  { isGameFinished = !isGameFinished; updateQuiz(); }
function toggleAlone() { isAlone = !isAlone; updateQuiz(); }

function afiseazaHarta() {
    document.getElementById("harta")     .style.display="block";
    document.getElementById("butonstart").style.display="none";
}
function ascundeHarta() {
    document.getElementById("harta")     .style.display="none";
}

var isAnimating = false;

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

function updateRanking(rank) {
    document.getElementById("rankingUIText").innerText = rank;
}


function startQuizButtonPress() {
    if (debugWithoutWS) { startQuiz(); }
    else if (wsValidity) { ws.send("MB"); }
}
function restartQuizButtonPress() {
    if (debugWithoutWS) { restartQuiz(); }
    else { ws.send("MR"); }

}


function nextQuestion() {
    ++ curentQuestion;
    if (curentQuestion === totalQuestions) {
        document.getElementById("mapGuessGameQuestionHeader").innerText = "";
        showScore();
    }
    else { document.getElementById("mapGuessGameQuestionHeader").innerText = "Găsește-mă | " + getLocationName(curentQuestion); }
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

function startQuiz() {
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
    curentQuestion = 0;
    afiseazaHarta();
    let leaderboard = document.getElementById("rezultate");
    leaderboard.style.display = "none";
    document.getElementById("butonrestart")  .style.display = "none";
    //  document.getElementById("butonrezultate").style.display = "none";
    document.getElementById("mapGuessGameQuestionHeader").innerText = "Găsește-mă | " + getLocationName(curentQuestion);
    document.getElementById("pergamentRezultate").style.display="none";
    restartDone = true;
}
function restartQuiz() {
    startQuiz();
}

function pressOn(option) {
    if (q_isMaster()) return;
    if(option === locatii[curentQuestion]) {
        corectAnswer();
    }
    else wrongAnswer();

    //  to update the server data
    var timeValue = getTime();
    if (!debugWithoutWS) ws.send("QU" + score + "#" + curentQuestion + "#" +  (timeValue - lastTime)*0.001);
    lastTime = timeValue;
}
function showScore() {
    if (restartDone == false) return;
    document.getElementById("mapGuessGameQuestionHeader").innerText = "";
    updateRanking("");
    ascundeHarta();
    document.getElementById("butonrestart")  .style.display = "block";
    //  document.getElementById("butonrezultate").style.display = "block";
    showLeaderboard();
}

function abortQuiz() {
    showScore();
}
function showLeaderboard() {
    let leaderboard = document.getElementById("rezultate");
    leaderboard.style.display="block";
    leaderboard.innerHTML = createLeaderboardData(leaderboardRawData);
    document.getElementById("pergamentRezultate").style.display="block";
}

//  utility functions
//      rawdata format: {{user_name}: {score} points#} for each user
function setLeaderboardRawData(rawdata) {
    leaderboardRawData = rawdata;
    let leaderboard = document.getElementById("rezultate");
    leaderboard.innerHTML = createLeaderboardData(leaderboardRawData);
}
function createLeaderboardData(rawdata) {
    ans = "<p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p><p>Top Elevi</p>";
    rawdata.toString().split("#").forEach(function (rawline) {
        ans += "<p>" + rawline + "</p>";
    }); return ans;
}
//  rawdata is read from questions.txt; format is: {each line from comunitati.xt + "#"}
function initializeQuestions(rawdata) {
    let order = [
        "ES-AN","ES-AR","ES-AS","ES-CA","ES-CB","ES-CL","ES-CM",
        "ES-CT","ES-EX","ES-GA","ES-RI","ES-MD","ES-MC",
        "ES-NC","ES-IB","ES-PV","ES-VC"
    ];

    locatii = [];
    fullNameUpdated = [];
    rawdata.toString().split("#").forEach(function (rawquestion) {
        for (let i = 0; i < totalQuestions; ++i) {
            if (fullName[i] === rawquestion) {
                locatii.push(order[i]);
                fullNameUpdated.push(fullName[i]);
                return;
            }
        }
    });
}

function updateQuiz() {
    
}

function insulehover(){
    document.getElementById('insulahover').style.display="block";
    
}

function insulehoverback(){
    document.getElementById('insulahover').style.display="none";
    
}

var buttonplayers = 0;

function showplayers(){
    if(buttonplayers % 2 == 0){
    document.getElementById('players').style.opacity = "1";
    document.getElementById('players').style.marginLeft = "0";
    buttonplayers++;
    }
    else{
    document.getElementById('players').styleopacity = "0";
    document.getElementById('players').style.marginLeft = "150vh";
    buttonplayers++;
    }
}