import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

@WebSocket
public class BlaBlaCarWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        User user1 = BlaBlaCar.users.get(BlaBlaCar.nextUserNumber);
        BlaBlaCar.users.get(BlaBlaCar.nextUserNumber).setUserSession(user);
        BlaBlaCar.userNamesMap.put(user, BlaBlaCar.users.get(BlaBlaCar.nextUserNumber));
        BlaBlaCar.broadcastMessage(sender = user1.getFirstName() + " " + user1.getLastName(), msg = "podłączył się");
        ++BlaBlaCar.nextUserNumber;
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        User user1 = BlaBlaCar.userNamesMap.get(user);
        BlaBlaCar.userNamesMap.remove(user);
        BlaBlaCar.broadcastMessage(sender = user1.getFirstName() + " " + user1.getLastName(), msg = "rozłączył się");
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws JSONException, ParseException {
        User user1 = BlaBlaCar.userNamesMap.get(user);

        JSONObject obj = new JSONObject(message);
        String type = obj.getString("type");

        switch(type) {
            case "addTrip":
                BlaBlaCar.addTrip(obj, user1);
                break;
            case "subscribeTrip":
                BlaBlaCar.subscribeTrip(obj, user1);
                break;
            case "saveForTrip":
                BlaBlaCar.saveMeForTheTrip(obj, user1);
                break;
            case "cancelMyTrip":
                BlaBlaCar.cancelTrip(obj, user1);
                break;
        }
    }

}
