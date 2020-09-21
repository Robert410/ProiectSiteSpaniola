function startBingoButtonPress() {
    if (!debugWithoutWS) {
        if (wsValidity) ws.send("BB");
    }
    else startBingo();
}

let interval = setInterval(1000000, function() {});
function endBingo() {
    clearInterval(interval);
    document.getElementById('poll').innerHTML = "";
    document.getElementById("Buton-Bingo").style.display = "none";
    cnt = randomizer.length;
    document.getElementById('startB').disabled=false;
}

var aux, cnt=0;
var anum=[],num=[];
var numere=new Array("uno","dos","tres","cuatro","cinco",
    "seis","siete","ocho","nueve","diez","once","doce","trece",
    "catorce","quince","dieciséis","diecisiete","dieciocho",
    "diecinueve","veinte","veintiuno","veintidós","veintitrés",
    "veinticuatro","veinticinco","veintiséis","veintisiete",
    "veintiocho","veintinueve","treinta","treinta y uno",
    "treinta y dos","treinta y tres","treinta y cuatro",
    "treinta y cinco","treinta y seis","treinta y siete",
    "treinta y ocho","treinta y nueve","cuarenta","cuarenta y uno",
    "cuarenta y dos","cuarenta y tres","cuarenta y cuatro",
    "cuarenta y cinco","cuarenta y seis","cuarenta y siete",
    "cuarenta y ocho","cuarenta y nueve","cincuenta"
);
var randomizer = new Array();
function isBingo()
{
    for(var i=1;i<=16;i++)
        num[i]=document.getElementById('num'+i);
    let over = 0;
    for(var i=1;i<=16;i++)
        if(num[i].value!="1")
            over=1;
    if(over==0) {
        if (!debugWithoutWS) ws.send("BF");
        document.getElementById('NObingo').style.display="none";
        document.getElementById('YESbingo').style.display="block";
    }
    else{
        document.getElementById('NObingo').style.display="block";
        document.getElementById('YESbingo').style.display="none";
    }
}
function startBingo()
{
    hideComentariu();
    intvPersistance = false; clearInterval(printInterval);

    document.getElementById('NObingo').style.display="none";
    document.getElementById('YESbingo').style.display="none";
    document.getElementById('poll').innerHTML = "";
    for (let i=1; i<=16; ++i) {
        num[i].value = "0";
        document.getElementById('num'+i).style.background = "#ece4e6";
    }

    cnt = 0;
    if (!isMaster()) document.getElementById("Buton-Bingo").style.display = "block";
    for (let i=1; i<=16; ++i)
        num[i].innerHTML = anum[i];

    if (debugWithoutWS) {
        randomizer = new Array();

        for (let i = 0; i < numere.length; ++i) {
            randomizer.push(numere[i]);
        }
        for (let i = 1; i <= 16; ++i) {
            randomizer.push(numere[anum[i] - 1]);
            randomizer.push(numere[anum[i] - 1]);
            randomizer.push(numere[anum[i] - 1]);
        }

        //  algoritmul Fisher-Yates
        for (let i = randomizer.length - 1; i > 0; --i) {
            let j = Math.floor(Math.random() * (i + 1));
            let x = randomizer[i];
            randomizer[i] = randomizer[j];
            randomizer[j] = x;
        }
    }

    document.getElementById('startB').disabled="disabled";
    gen();
    interval = setInterval(function(){gen();},2500);
    //apelarea functiei gen din 2.5 in 2.5 secunde (2500 milisecunde)
}
function gen()
{
    var nrGen;
    if(cnt+1<randomizer.length)
    {
        nrGen=document.getElementById('poll');
        nrGen.innerHTML += "<div class='num-style'>"+randomizer[cnt]+"</div>";
        verificare(randomizer[cnt]);
        cnt ++;
    }
    else {
        endBingo();
        document.getElementById('poll').innerHTML = "Jocul s-a terminat fără câștigător. :( ";
    }
}
function verificare(g1)
{
    for(var i=1;i<=16;i++)
    {
        num[i].onmousedown=false;
        if(g1 == numere[anum[i]-1])
        {
            num[i]=document.getElementById('num'+i);
            num[i].onmousedown=verificat;
        }
    }
}
function verificat(e)
{
    aux=e.target;
    aux.value='1';
    aux.style.background="green";
}
function initiere()
{
    for(var i=1;i<=16;i++)
    {
        anum[i]=Math.floor((Math.random()*50)+1);
        num[i]=document.getElementById('num'+i);
        num[i].innerHTML=" ";
    }
}
window.addEventListener('load',initiere, false);
