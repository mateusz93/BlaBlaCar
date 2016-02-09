//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat/");
webSocket.onmessage = function (msg) { updateChat(msg); };
webSocket.onclose = function () { alert("WebSocket connection closed") };

//Send message if "Send" is clicked
id("send").addEventListener("click", function () {
    sendMessage(id("message").value);
});

//Send message if enter is pressed in the input field
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) { sendMessage(e.target.value); }
});

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
        id("message").value = "";
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    var data = JSON.parse(msg.data);
    insert("chat", data.userMessage);
    id("userlist").innerHTML = "";
    data.userlist.forEach(function (user) {
        insert("userlist", "<li>" + user + "</li>");
    });
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
    return document.getElementById("addTrip").style.visibility = 'visible';
}

function showSubscribeTripForm() {
    return document.getElementById("subscribeTrip").style.visibility = 'visible';
}

function showSaveTripForm() {
    return document.getElementById("saveTripButton");
}

id("addTripSend").addEventListener("click", function () {
    var msg = {
        type: "addTrip",
        startingPlace: document.getElementById("startingPlace").value,
        destination: document.getElementById("destination").value,
        startingDay: document.getElementById("startingDay").value,
        price: document.getElementById("price").value,
        freeSeats: document.getElementById("freeSeats").value
    };

    // Send the msg object as a JSON-formatted string.
    webSocket.send(JSON.stringify(msg));

    // Blank the text input element, ready to receive the next line of text from the user.
    document.getElementById("startingPlace").value = "";
    document.getElementById("destination").value = "";
    document.getElementById("startingDay").value = "";
    document.getElementById("price").value = "";
    document.getElementById("freeSeats").value = "";

    document.getElementById("addTrip").style.visibility = 'hidden';
});

id("subscribeTripSend").addEventListener("click", function () {
    var msg = {
        type: "subscribeTrip",
        subscribeStartingPlace: document.getElementById("subscribeStartingPlace").value,
        subscribeDestination: document.getElementById("subscribeDestination").value
    };

    // Send the msg object as a JSON-formatted string.
    webSocket.send(JSON.stringify(msg));

    // Blank the text input element, ready to receive the next line of text from the user.
    document.getElementById("subscribeDestination").value = "";
    document.getElementById("subscribeDestination").value = "";

    document.getElementById("subscribeTrip").style.visibility = 'hidden';
});
