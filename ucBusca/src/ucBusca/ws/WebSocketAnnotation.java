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

@ServerEndpoint(value = "/wss")
public class WebSocketAnnotation {
    public Session session;
    public static final Set<Integer> userIDs = new CopyOnWriteArraySet<>();
    public static final Set<WebSocketAnnotation> users = new CopyOnWriteArraySet<>();
    private RMI_S server = null;
    public int userID;


    public WebSocketAnnotation() {
        try {
            this.server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary"); //Gets the RMIServer's reference
        }
        catch(NotBoundException | RemoteException e) {
            e.printStackTrace(); // what happens *after* we reach this line?
        }
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        users.add(this);
    }

    @OnClose
    public void end() {
        // clean up once the WebSocket connection is closed
        userIDs.remove(this.userID);
        users.remove(this);
    }

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

    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
    }

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
