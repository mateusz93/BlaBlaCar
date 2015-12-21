import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

@WebSocket
public class ChatWebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        String username = "User" + Chat.nextUserNumber++;
        Chat.userUsernameMap.put(user, username);
        Chat.broadcastMessage(sender = "BlaBlaCar", msg = (username + " joined the chat"));
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        Chat.broadcastMessage(sender = "BlaBlaCar", msg = (username + " left the chat"));
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws JSONException {
        JSONObject obj = new JSONObject(message);
        String type = obj.getString("type");
        if (type.equals("registration")) {
            System.out.println(message.toString());
            Chat.register(obj);
            for (User s : Chat.users) {
                System.out.println(s.getFirstName() + " " + s.getLastName() + " " + s.getEmail() + " " + s.getPassword());
            }
            Chat.broadcastMessage(sender = Chat.userUsernameMap.get(user), msg = "Zarejestrowano pomyślnie");
        } else if (type.equals("login")) {
            Chat.login(obj);
            Chat.broadcastMessage(sender = Chat.userUsernameMap.get(user), msg = "Zalogowano pomyślnie");
        } else {
            throw new UnsupportedOperationException();
        }


    }

}
