import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import java.text.*;
import java.util.*;
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
    }

    private static void initUsers() {
        User user = new User();
        user.setFirstName("Mateusz");
        user.setLastName("Wieczorek");
        user.setPassword("password");
        user.setEmail("mateusz.wieczorek@gmail.com");
        User user2 = new User();
        user2.setFirstName("Kuba");
        user2.setLastName("Clapa");
        user2.setPassword("password");
        user2.setEmail("kuba.clapa@gmail.com");
        User user3 = new User();
        user3.setFirstName("Ewa");
        user3.setLastName("Nowak");
        user3.setPassword("password");
        user3.setEmail("ewa.nowak@gmail.com");
        User user4 = new User();
        user4.setFirstName("Jan");
        user4.setLastName("Kowalski");
        user4.setPassword("password");
        user4.setEmail("jan.kowalski@gmail.com");
        users.add(user);
        users.add(user3);
        users.add(user2);
        users.add(user4);
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                                .put("userMessage", createHtmlMessageFromSender(sender, message))
                                .put("userlist", userUsernameMap.values())
                ));
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
        trips.add(trip);
    }

    public static void subscribeTrip(JSONObject json, User user) throws JSONException {
        Trip trip = new Trip();
        trip.setOwner(user);
        trip.setStartingPlace(json.getString("subscribeStartingPlace"));
        trip.setDestination(json.getString("subscribeDestination"));
        subscribedTrips.add(trip);
    }


    public static void checkSubscriptionForTripAndSendMessage(JSONObject obj) throws JSONException {
        for (Trip s : subscribedTrips) {
            if (s.getStartingPlace().equals(obj.getString("startingPlace")) &&
                    s.getDestination().equals(obj.getString("destination"))) {

                Session user = s.getOwner().getUser();
//                for (Trip t : trips) {
//                    if (obj.getString("startingPlace").equals(t.getStartingPlace()) &&
//                            obj.getString("destination").equals(t.getDestination())) {
//                        user = t.getUser();
//                    }
//                }

                final Session finalUser = user;
                userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
                    if (session == finalUser) {
                        try {
                            String message = ">>> Dodał przejazd który subskrybujesz <<< ";
                            message += "[Miejsce startu: " + obj.getString("startingPlace") + "]\n";
                            message += "[Miejsce docelowe: " + obj.getString("destination") + "]\n";
                            message += "[Wolne miejsca: " + obj.getString("freeSeats") + "]\n";
                            message += "[Cena: " + obj.getString("price") + "]\n";

                            session.getRemote().sendString(String.valueOf(new JSONObject()
                                            .put("userMessage", createHtmlMessageFromSender(trips.get(trips.size() - 1).getOwner().toString(), message))
                                            .put("userlist", userUsernameMap.values())
                            ));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

    }
}
