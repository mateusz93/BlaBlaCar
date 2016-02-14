//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/blablacar/");
webSocket.onmessage = function (msg) { updateChatAndLists(msg); };
webSocket.onclose = function () { alert("WebSocket connection closed") };


//Update the chat-panel and all lists
function updateChatAndLists(msg) {
    var numberOfTrip = 0, numberOfMyTrip = 0;
    var data = JSON.parse(msg.data);

    id("myName").innerHTML = "";
    insert("myName", "<li>" + data.myName + "</li>");

    if (data.userMessage != null) {
        insert("chat", data.userMessage);
    }

    id("userlist").innerHTML = "";
    data.userlist.forEach(function (user) {
        insert("userlist", "<li>" + user + "</li>");
    });

    id("tripList").innerHTML = "";
    data.tripList.forEach(function (trip) {
        insert("tripList", "<li>" + trip +
            "<br><button name=\"trip\" value=\"" + numberOfTrip++ + "\" >Zapisz sie</button>" +
            "</li><br><br>");
    })

    id("myTripList").innerHTML = "";
    data.myTripList.forEach(function (trip) {
        insert("myTripList", "<li>" + trip +
            "<br><button name=\"myTrip\" value=\"" + numberOfMyTrip++ + "\" >Anuluj przejazd</button>" +
            "</li><br><br>");
    })

    activeSaveForTrip();
    activeCancelForMyTrip();
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}

function showAddTripForm() {
    id("subscribeTrip").style.visibility = 'hidden';
    id("addTrip").style.visibility = 'visible';
}

function showSubscribeTripForm() {
    id("addTrip").style.visibility = 'hidden';
    id("subscribeTrip").style.visibility = 'visible';
}

function showSaveTripFormAndActivateSaving() {
    activeSaveForTrip();
    id("myTripListDiv").style.visibility = 'hidden';
    id("tripListDiv").style.visibility = 'visible';
}

function showCancelTripForm() {
    activeCancelForMyTrip();
    id("tripListDiv").style.visibility = 'hidden';
    id("myTripListDiv").style.visibility = 'visible';
}

id("addTripSend").addEventListener("click", function () {
    var msg = {
        type: "addTrip",
        startingPlace: id("startingPlace").value,
        destination: id("destination").value,
        startingDay: id("startingDay").value,
        price: id("price").value,
        freeSeats: id("freeSeats").value
    };

    // Send the msg object as a JSON-formatted string.
    webSocket.send(JSON.stringify(msg));

    // Blank the text input element, ready to receive the next line of text from the user.
    id("startingPlace").value = "";
    id("destination").value = "";
    id("startingDay").value = "";
    id("price").value = "";
    id("freeSeats").value = "";

    id("addTrip").style.visibility = 'hidden';
});

id("subscribeTripSend").addEventListener("click", function () {
    var msg = {
        type: "subscribeTrip",
        subscribeStartingPlace: id("subscribeStartingPlace").value,
        subscribeDestination: id("subscribeDestination").value
    };

    // Send the msg object as a JSON-formatted string.
    webSocket.send(JSON.stringify(msg));

    // Blank the text input element, ready to receive the next line of text from the user.
    id("subscribeDestination").value = "";
    id("subscribeDestination").value = "";

    id("subscribeTrip").style.visibility = 'hidden';
});

id("tripListDiv").addEventListener("click", function () {
    id("tripListDiv").style.visibility = 'visible';
});

id("myTripListDiv").addEventListener("click", function () {
    id("myTripListDiv").style.visibility = 'visible';
});

function activeSaveForTrip() {
    var trips = document.getElementsByName("trip");

    for(var i = 0; i < trips.length; i++) {
        trips[i].onclick = function() {
            saveMeForTrip(this.value);
        }
    }
}

function activeCancelForMyTrip() {
    var myTrips = document.getElementsByName("myTrip");

    for(var i = 0; i < myTrips.length; i++) {
        myTrips[i].onclick = function() {
            cancelMyTrip(this.value);
        }
    }
}

function saveMeForTrip(number) {
    var msg = {
        type: "saveForTrip",
        tripNumber: number
    };

    webSocket.send(JSON.stringify(msg));

    id("tripListDiv").style.visibility = 'hidden';
}

function cancelMyTrip(number) {
    var msg = {
        type: "cancelMyTrip",
        tripNumber: number
    };

    webSocket.send(JSON.stringify(msg));

    id("myTripListDiv").style.visibility = 'hidden';
}
