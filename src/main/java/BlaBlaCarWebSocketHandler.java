import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

@WebSocket
public class BlaBlaCarWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session userSession) throws Exception {
        int index = BlaBlaCar.getIndexOfFirstAvailableUser();
        User user = BlaBlaCar.users.get(index);
        BlaBlaCar.users.get(index).setUserSession(userSession);
        BlaBlaCar.userNamesMap.put(userSession, user);
        BlaBlaCar.broadcastMessage(sender = user.getFirstName() + " " + user.getLastName(), msg = "podłączył się", userSession);
    }

    @OnWebSocketClose
    public void onClose(Session userSession, int statusCode, String reason) {
        User user1 = BlaBlaCar.userNamesMap.get(userSession);
        BlaBlaCar.userNamesMap.remove(userSession);
        BlaBlaCar.removeUserBySession(userSession);
        BlaBlaCar.broadcastMessage(sender = user1.getFirstName() + " " + user1.getLastName(), msg = "rozłączył się", userSession);
    }

    @OnWebSocketMessage
    public void onMessage(Session userSession, String message) throws JSONException, ParseException {
        User user = BlaBlaCar.userNamesMap.get(userSession);

        JSONObject obj = new JSONObject(message);
        String type = obj.getString("type");

        switch(type) {
            case "addTrip":
                BlaBlaCar.addTrip(obj, user);
                break;
            case "subscribeTrip":
                BlaBlaCar.subscribeTrip(obj, user);
                break;
            case "saveForTrip":
                BlaBlaCar.saveMeForTheTrip(obj, user);
                break;
            case "cancelMyTrip":
                BlaBlaCar.cancelTrip(obj, user);
                break;
        }
    }

}
