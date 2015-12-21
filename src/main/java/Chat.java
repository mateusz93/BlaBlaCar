import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {

    static List<Trip> trips = new ArrayList<>();
    static List<User> users = new ArrayList<User>();
    static Map<Session, String> userUsernameMap = new HashMap<>();
    static int nextUserNumber = 1; //Assign to username for next connecting user

    public static void main(String[] args) {
        staticFileLocation("public"); //index.html is served at localhost:4567 (default port)
        webSocket("/chat", ChatWebSocketHandler.class);

        users.add(new User("Jan", "Nowak", "j@nowak.pl", "jnowak"));
        users.add(new User("Mateusz", "Wieczorek", "mwieczorek@wp.pl", "mawieczo"));
        users.add(new User("Jakub", "Clapa", "jc@gmail.com", "jc"));
        init();
    }

    //Sends a message from server to user
    public static void registerMessage(String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(
                        new JSONObject().put("registrationInfo", createHtmlMessageFromServer(message))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    //Builds a HTML element with a message
    private static String createHtmlMessageFromServer(String message) {
        return article().with(p(message)).render();
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    public static void register(JSONObject json) throws JSONException {
        User u = new User();
        u.setEmail(json.getString("email"));
        u.setFirstName(json.getString("firstName"));
        u.setLastName(json.getString("lastName"));
        u.setPassword(json.getString("password"));
        users.add(u);
    }


    public static void login(JSONObject json) {

    }

    public static void addTrip(JSONObject json) throws JSONException {
        Trip t = new Trip();
        t.setFreeSeats(json.getInt("freeSeats"));
        t.setDestination(json.getString("destination"));
        t.setOwner(new User());
        t.setPrice(json.getInt("price"));
        t.setStartingDay(json.getString("startingDay"));
        t.setStartingPlace(json.getString("startingPlace"));

    }
}
