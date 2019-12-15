package ucBusca.ws;

import Server.RMI_S;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;

/**
 * Class that works in parallel with the view's websockets.
 * Opens the websockets, adds users' ids and websockets to its list and receives/sends messages to their respective receivers.
 *
 * Implements the wss protocol.
 */
@ServerEndpoint(value = "/wss")
public class WebSocketAnnotation {
    /**
     * WebSocket Session, unique to each user.
     */
    public Session session;
    /**
     * Set with the users' ids.
     */
    public static final Set<Integer> userIDs = new CopyOnWriteArraySet<>();
    /**
     * Set with the users' WebSocketAnnotations instaces.
     */
    public static final Set<WebSocketAnnotation> users = new CopyOnWriteArraySet<>();
    /**
     * Reference to the RMIServer.
     */
    private RMI_S server = null;
    /**
     * The user's id.
     */
    public int userID;


    /**
     * Constructor.
     * Gets a reference to the remote server.
     */
    public WebSocketAnnotation() {
        try {
            this.server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary"); //Gets the RMIServer's reference
        }
        catch(NotBoundException | RemoteException e) {
            e.printStackTrace(); // what happens *after* we reach this line?
        }
    }

    /**
     * Called when the websocket opens.
     * Links the session to the user and adds the user to the set.
     * @param session
     */
    @OnOpen
    public void start(Session session) {
        this.session = session;
        users.add(this);
    }

    /**
     * Called when the websocket closes.
     * Removes the user's id and WebSocketAnnotation reference from their respective sets.
     */
    @OnClose
    public void end() {
        // clean up once the WebSocket connection is closed
        userIDs.remove(this.userID);
        users.remove(this);
    }

    /**
     * Called when the websocket receives a message.
     * Adds the user id to its set and relays the message to the send method.
     * Also checks if the user has any notifications pending.
     * @param message
     */
    @OnMessage
    public void receiveMessage(String message) {
        if(message != null && !message.equals("")){

            if(message.startsWith("Statistics")){
                this.sendMessage(message);
                return;
            }

            this.userIDs.add(Integer.parseInt(message));
            this.userID = Integer.parseInt(message);
            this.users.add(this);

            //check if there are notifications
            try {
                ArrayList<String> notifications = server.checkNotification(Integer.parseInt(message));

                if(notifications != null){
                    for(String notification:notifications)
                        this.sendMessage(notification);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Called when the websocket gets an error.
     * @param t
     */
    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
    }

    /**
     * Send method. Access the user's session and sends the message.
     * @param text
     */
    private void sendMessage(String text) {
        // uses *this* object's session to call sendText()
        try {
            this.session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            // clean up once the WebSocket connection is closed
            try {
                this.session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
