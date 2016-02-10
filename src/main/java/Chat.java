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

public class Chat {
    static List<Trip> subscribedTrips = new ArrayList<Trip>();
    static List<Trip> trips = new ArrayList<Trip>();
    static List<User> users = new ArrayList<User>();
    //static Map<Session, String> userUsernameMap = new HashMap<>();
    static Map<Session, User> userUsernameMap = new HashMap<>();
    static int nextUserNumber = 0; //Assign to username for next connecting user

    public static void main(String[] args) {
        staticFileLocation("public"); //index.html is served at localhost:4567 (default port)
        webSocket("/chat", ChatWebSocketHandler.class);
        init();
        initUsers();
        initTrips();
    }

    private static void initTrips() {
        Trip trip = new Trip();
        trip.setStartingDay(new Date());
        trip.setPrice(15);
        trip.setStartingPlace("Wrocław");
        trip.setDestination("Gdańsk");
        trip.setOwner(users.get(0));
        trip.setFreeSeats(4);
        trips.add(trip);

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
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            ArrayList<User> me = new ArrayList<User>();
            me.add(userUsernameMap.get(session));
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                                .put("userMessage", createHtmlMessageFromSender(sender, message))
                                .put("userlist", userUsernameMap.values())
                                .put("tripList", trips)
                                .put("me", me)
                ));
                System.out.print(me.toString());
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

    public static void addTrip(JSONObject json, User user) throws JSONException {
        Trip trip = new Trip();
        trip.setOwner(user);
        trip.setStartingPlace(json.getString("startingPlace"));
        trip.setDestination(json.getString("destination"));
        trip.setFreeSeats(json.getInt("freeSeats"));
        trip.setPrice(json.getDouble("price"));
        // trip.setStartingDay(json.getString("startingDay"));
        trip.getUsers().add(user);
        trips.add(trip);
    }

    public static void subscribeTrip(JSONObject json, User user) throws JSONException {
        Trip trip = new Trip();
        trip.setOwner(user);
        trip.setStartingPlace(json.getString("subscribeStartingPlace"));
        trip.setDestination(json.getString("subscribeDestination"));
        subscribedTrips.add(trip);
    }

    public static void saveMeForATrip(JSONObject json, User user) throws JSONException {
        int tripNumber = Integer.parseInt(json.getString("tripNumber"));
        System.out.println("Numer przejazdu: " + tripNumber);
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

    private static void sendNotificationToAllTripParticipants(String startingPlace, String destination, User u) {
        for (Trip s : trips) {
            if (s.getStartingPlace().equals(startingPlace) &&
                    s.getDestination().equals(destination)) {
                final List<User> finalUsersToNotificate = s.getUsers();
                System.out.println(finalUsersToNotificate.toString());
                userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
                    ArrayList<User> me = new ArrayList<User>();
                    me.add(userUsernameMap.get(session));
                    for (User user : finalUsersToNotificate) {
                        if (session == user.getUserSession() || session == s.getOwner().getUserSession()) {
                            try {
                                String message = "";
                                if (session == u.getUserSession()) {
                                    message = "Zapisałem się na przejazd";
                                } else {
                                    message = "Dołączył do przejazdu w którym bierzesz udział";
                                }
                                message += "[Miejsce startu: " + startingPlace + "]\n";
                                message += "[Miejsce docelowe: " + destination + "]\n";

                                session.getRemote().sendString(String.valueOf(new JSONObject()
                                                .put("userMessage", createHtmlMessageFromSender(u.getFirstName() + " " + u.getLastName(), message))
                                                .put("userlist", userUsernameMap.values())
                                                .put("tripList", trips)
                                                .put("me", me)
                                ));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

    public static void checkSubscriptionForTripAndSendMessage(JSONObject obj) throws JSONException {
        for (Trip s : subscribedTrips) {
            if (s.getStartingPlace().equals(obj.getString("startingPlace")) &&
                    s.getDestination().equals(obj.getString("destination"))) {

                Session user = s.getOwner().getUserSession();
                sendSubscriptionMessageToSubscribers(user, obj);
            }
        }
        updateAllLists();
    }

    private static void sendSubscriptionMessageToSubscribers(final Session subscriber, JSONObject obj) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            ArrayList<User> me = new ArrayList<User>();
            me.add(userUsernameMap.get(session));
            if (session == subscriber) {
                try {
                    String message = ">>> Dodał przejazd który subskrybujesz <<< ";
                    message += "[Miejsce startu: " + obj.getString("startingPlace") + "]\n";
                    message += "[Miejsce docelowe: " + obj.getString("destination") + "]\n";
                    message += "[Wolne miejsca: " + obj.getString("freeSeats") + "]\n";
                    message += "[Cena: " + obj.getString("price") + "]\n";

                    session.getRemote().sendString(String.valueOf(new JSONObject()
                                    .put("userMessage", createHtmlMessageFromSender(trips.get(trips.size() - 1).getOwner().toString(), message))
                                    .put("userlist", userUsernameMap.values())
                                    .put("tripList", trips)
                                    .put("me", me)
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void updateAllLists() {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            ArrayList<User> me = new ArrayList<User>();
            me.add(userUsernameMap.get(session));
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                                .put("userlist", userUsernameMap.values())
                                .put("tripList", trips)
                                .put("me", me)
                ));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }


}
