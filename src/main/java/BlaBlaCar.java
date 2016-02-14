import org.eclipse.jetty.websocket.api.*;
import org.json.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class BlaBlaCar {
    static List<Trip> subscribedTrips = new ArrayList<Trip>();
    static List<Trip> trips = new ArrayList<Trip>();
    static List<User> users = new ArrayList<User>();
    static Map<Session, User> userNamesMap = new HashMap<>();
    static int nextUserNumber = 0; //Assign to username for next connecting user

    public static void main(String[] args) {
        staticFileLocation("public"); //index.html is served at localhost:4567 (default port)
        webSocket("/blablacar", BlaBlaCarWebSocketHandler.class);
        init();
        initUsers();
        initTrips();
    }

    private static void initTrips() {
//        Trip trip = new Trip();
//        trip.setStartingDay(new Date());
//        trip.setPrice(15);
//        trip.setStartingPlace("Wrocław");
//        trip.setDestination("Gdańsk");
//        trip.setOwner(users.get(0));
//        trip.setFreeSeats(4);
//        trips.add(trip);
//        Trip trip2 = new Trip();
//        trip2.setStartingDay(new Date());
//        trip2.setPrice(111);
//        trip2.setStartingPlace("ABC");
//        trip2.setDestination("ZYX");
//        trip2.setOwner(users.get(1));
//        trip2.setFreeSeats(3);
//        trips.add(trip2);
    }

    private static void initUsers() {
        List<String> userList = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get("Users.txt"))) {
            userList = stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String s : userList) {
            String[] userTable = s.split(";");
            User user = new User();
            user.setFirstName(userTable[0]);
            user.setLastName(userTable[1]);
            user.setEmail(userTable[2]);
            user.setPassword(userTable[3]);
            users.add(user);
        }
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userNamesMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                                .put("userMessage", createHtmlMessageFromSender(sender, message))
                                .put("userlist", userNamesMap.values())
                                .put("tripList", trips)
                                .put("myTripList", findMyTripsByUser(userNamesMap.get(session)))
                                .put("myName", userNamesMap.get(session))
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void connectedMessage(String sender, String message, Session user) {
        userNamesMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                if (session == user) {
                    session.getRemote().sendString(String.valueOf(new JSONObject()
                                    .put("userlist", userNamesMap.values())
                                    .put("tripList", trips)
                                    .put("myTripList", findMyTripsByUser(userNamesMap.get(session)))
                                    .put("myName", userNamesMap.get(session))
                    ));
                } else {
                    session.getRemote().sendString(String.valueOf(new JSONObject()
                                    .put("userMessage", createHtmlMessageFromSender(sender, message))
                                    .put("userlist", userNamesMap.values())
                                    .put("tripList", trips)
                                    .put("myTripList", findMyTripsByUser(userNamesMap.get(session)))
                                    .put("myName", userNamesMap.get(session))
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    public static void addTrip(JSONObject json, User user) throws JSONException, ParseException {
        Trip trip = new Trip();
        trip.setOwner(user);
        trip.setStartingPlace(json.getString("startingPlace"));
        trip.setDestination(json.getString("destination"));
        trip.setFreeSeats(json.getInt("freeSeats"));
        trip.setPrice(json.getDouble("price"));
        trip.setStartingDay((new SimpleDateFormat("dd-mm-yyyy")).parse(json.getString("startingDay")));
        trip.getUsers().add(user);
        trips.add(trip);
        checkSubscriptionForTripAndSendMessage(json);
    }

    public static void subscribeTrip(JSONObject json, User user) throws JSONException {
        Trip trip = new Trip();
        trip.setOwner(user);
        trip.setStartingPlace(json.getString("subscribeStartingPlace"));
        trip.setDestination(json.getString("subscribeDestination"));
        subscribedTrips.add(trip);
    }

    public static void saveMeForTheTrip(JSONObject json, User user) throws JSONException {
        int tripNumber = Integer.parseInt(json.getString("tripNumber"));
        if (trips.get(tripNumber).getFreeSeats() > 0) {
            for (User u : trips.get(tripNumber).getUsers()) {
                if (u.getEmail().equals(user.getEmail())) {
                    return;
                }
            }
            trips.get(tripNumber).getUsers().add(user);
            trips.get(tripNumber).setFreeSeats(trips.get(tripNumber).getFreeSeats() - 1);
            sendNotificationToAllTripParticipants(trips.get(tripNumber).getStartingPlace(), trips.get(tripNumber).getDestination(), user);
            updateAllLists();
        }
    }

    public static void cancelTrip(JSONObject json, User user) throws JSONException {
        int tripNumber = Integer.parseInt(json.getString("tripNumber"));

        sendCancelTripMessageToParticipants(trips.get(tripNumber));
        trips.remove(trips.get(tripNumber));

        updateAllLists();
    }

    private static void sendCancelTripMessageToParticipants(Trip trip) {
        userNamesMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            for (User user : trip.getUsers()) {
                if (session == user.getUserSession() || session == trip.getOwner().getUserSession()) {
                    try {
                        String message = prepareCancelTripMessageForParticipant(session, trip, user);
                        user.getUserSession().getRemote().sendString(String.valueOf(new JSONObject()
                                        .put("userMessage", createHtmlMessageFromSender(trip.getOwner().getFirstName() + " " + trip.getOwner().getLastName(), message))
                                        .put("userlist", userNamesMap.values())
                                        .put("tripList", trips)
                                        .put("myTripList", findMyTripsByUser(userNamesMap.get(user.getUserSession())))
                                        .put("myName", userNamesMap.get(user.getUserSession()))
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        });
    }

    private static void sendNotificationToAllTripParticipants(String startingPlace, String destination, User user) {
        for (Trip trip : trips) {
            if (trip.getStartingPlace().equals(startingPlace) && trip.getDestination().equals(destination)) {
                userNamesMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
                    sendNotificationToParticipants(session, trip, user);
                });
            }
        }
    }

    private static void sendNotificationToParticipants(Session session, Trip trip, User user) {
        for (User users : trip.getUsers()) {
            if (session == users.getUserSession() || session == trip.getOwner().getUserSession()) {
                try {
                    String message = prepareSubscriptionMessage(session, trip.getStartingPlace(), trip.getDestination(), user);
                    session.getRemote().sendString(String.valueOf(new JSONObject()
                                    .put("userMessage", createHtmlMessageFromSender(user.getFirstName() + " " + user.getLastName(), message))
                                    .put("userlist", userNamesMap.values())
                                    .put("tripList", trips)
                                    .put("myTripList", findMyTripsByUser(userNamesMap.get(session)))
                                    .put("myName", userNamesMap.get(session))
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private static Trip findTripByStartingPlaceAndDestination(String startingPlace, String destination) {
        for (Trip trip : trips) {
            if (trip.getStartingPlace().equals(startingPlace) && trip.getDestination().equals(destination)) {
                return trip;
            }
        }
        return null;
    }


    public static void checkSubscriptionForTripAndSendMessage(JSONObject obj) throws JSONException {
        for (Trip trip : subscribedTrips) {
            if (trip.getStartingPlace().equals(obj.getString("startingPlace")) &&
                    trip.getDestination().equals(obj.getString("destination"))) {

                Session user = trip.getOwner().getUserSession();
                sendSubscriptionMessageToSubscribers(user, obj);
            }
        }
        updateAllLists();
    }

    private static void sendSubscriptionMessageToSubscribers(final Session subscriber, JSONObject obj) {
        userNamesMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            if (session == subscriber) {
                try {
                    String message = prepareSubscriptionMessageForSubscriber(obj);
                    session.getRemote().sendString(String.valueOf(new JSONObject()
                                    .put("userMessage", createHtmlMessageFromSender(trips.get(trips.size() - 1).getOwner().toString(), message))
                                    .put("userlist", userNamesMap.values())
                                    .put("tripList", trips)
                                    .put("myTripList", findMyTripsByUser(userNamesMap.get(session)))
                                    .put("myName", userNamesMap.get(session))
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static List<Trip> findMyTripsByUser(User user) {
        ArrayList<Trip> myTrips = new ArrayList<>();
        for (Trip t : trips) {
            if (t.getOwner().getEmail().equals(user.getEmail())) {
                myTrips.add(t);
            }
        }
        return myTrips;
    }

    private static void updateAllLists() {
        userNamesMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                                .put("userlist", userNamesMap.values())
                                .put("tripList", trips)
                                .put("myTripList", findMyTripsByUser(userNamesMap.get(session)))
                                .put("myName", userNamesMap.get(session))
                ));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private static String prepareSubscriptionMessage(Session session, String startingPlace, String destination, User user) {
        String message = (session == user.getUserSession()) ? ">>>>>>>> Zapisałem się na przejazd <<<<<<<<" :
                ">Dołączył do przejazdu w którym bierzesz udział<";
        message += " [Miejsce startu: " + startingPlace + "]\n";
        message += "[Miejsce docelowe: " + destination + "]\n";
        message += "[Lista uczestników: ";
        for (User u : findTripByStartingPlaceAndDestination(startingPlace, destination).getUsers()) {
            message += u.getFirstName() + " " + u.getLastName() + ", ";
        }
        message += "]";
        return message;
    }

    private static String prepareSubscriptionMessageForSubscriber(JSONObject obj) throws JSONException {
        String message = ">>>>>> Dodał przejazd który subskrybujesz <<<<<<";
        message += " [Miejsce startu: " + obj.getString("startingPlace") + "]\n";
        message += "[Miejsce docelowe: " + obj.getString("destination") + "]\n";
        message += "[Wolne miejsca: " + obj.getString("freeSeats") + "]\n";
        message += "[Cena: " + obj.getString("price") + "]\n";
        return message;
    }

    private static String prepareCancelTripMessageForParticipant(Session session, Trip trip, User user) {
        String message = (session == trip.getOwner().getUserSession()) ? ">>>>>>>>>>>>> Anulowałeś przejazd <<<<<<<<<<<<" :
                ">>> Anulował przejazd w którym brałeś udział <<<";
        message += " [Miejsce startu: " + trip.getStartingPlace() + "]\n";
        message += "[Miejsce docelowe: " + trip.getDestination() + "]\n";
        message += "[Wolne miejsca: " + trip.getFreeSeats() + "]\n";
        message += "[Cena: " + trip.getPrice() + "]\n";
        message += "[Lista uczestników: ";
        for (User u : trip.getUsers()) {
            message += u.getFirstName() + " " + u.getLastName() + ", ";
        }
        message += "]";
        return message;
    }

}
