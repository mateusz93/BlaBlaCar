//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
webSocket.onmessage = function (msg) { updateChatAndLists(msg); activeSaveForTrip();};
webSocket.onclose = function () { alert("WebSocket connection closed") };


//Update the chat-panel, the list of connected users and trips
function updateChatAndLists(msg) {
    var data = JSON.parse(msg.data);
    if (data.userMessage != null) {
        insert("chat", data.userMessage);
    }
    id("userlist").innerHTML = "";
    data.userlist.forEach(function (user) {
        insert("userlist", "<li>" + user + "</li>");
    });
    id("tripList").innerHTML = "";
    var number = 0;
    data.tripList.forEach(function (trip) {
        insert("tripList", "<li>" + trip +
            "<br><button name=\"trip\" value=\"" + number + "\" >Zapisz sie</button>" +
            "</li><br><br>");
        number = number + 1;
    })

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
    return id("addTrip").style.visibility = 'visible';
}

function showSubscribeTripForm() {
    return id("subscribeTrip").style.visibility = 'visible';
}

function showSaveTripFormAndActivateSaving() {
    activeSaveForTrip();
    return id("tripList").style.visibility = 'visible';
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

id("tripList").addEventListener("click", function () {
    id("tripList").style.visibility = 'visible';
});

function activeSaveForTrip() {
    var trips = document.getElementsByName("trip");

    for(var i = 0; i < trips.length; i++) {
        trips[i].onclick = function() {
            saveMeForTrip(this.value);
        }
    }
}

function saveMeForTrip(number) {
    var msg = {
        type: "saveForTrip",
        tripNumber: number
    };

    // Send the msg object as a JSON-formatted string.
    webSocket.send(JSON.stringify(msg));

    id("tripList").style.visibility = 'hidden';
}
