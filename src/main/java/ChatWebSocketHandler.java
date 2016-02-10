import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        User user1 = Chat.users.get(Chat.nextUserNumber);
        Chat.users.get(Chat.nextUserNumber).setUserSession(user);
//        String username = "" + users.get(Chat.nextUserNumber).getFirstName() + " " + users.get(Chat.nextUserNumber).getLastName();
        Chat.userUsernameMap.put(user, Chat.users.get(Chat.nextUserNumber));
        Chat.broadcastMessage(sender = user1.getFirstName() + " " + user1.getLastName(), msg = "podłączył sie");
        ++Chat.nextUserNumber;
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        User user1 = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        Chat.broadcastMessage(sender = user1.getFirstName() + " " + user1.getLastName(), msg = "rozłączył się");
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws JSONException {
        User user1 = Chat.userUsernameMap.get(user);

        JSONObject obj = new JSONObject(message);
        String type = obj.getString("type");

        if ("addTrip".equals(type)) {
            System.out.println(message.toString());
            Chat.addTrip(obj, user1);
            for (User s : Chat.users) {
                System.out.println(s.getFirstName() + " " + s.getLastName() + " " + s.getEmail() + " " + s.getPassword());
            }
            Chat.checkSubscriptionForTripAndSendMessage(obj);
        }
        if ("subscribeTrip".equals(type)) {
            Chat.subscribeTrip(obj, user1);
        }
        if ("saveForTrip".equals(type)) {
            Chat.saveMeForATrip(obj, user1);
        }
    }

}
