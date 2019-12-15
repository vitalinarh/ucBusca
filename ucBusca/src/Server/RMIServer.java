package Server;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import uc.sd.apis.FacebookApi2;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

//Class for the RMIServer; extends UnicastRemoteObeject - so it can be referenced - and implements the corresponding RMI interface

/**
 * Class that will represent the RMI servers.
 * It will be the bridge between the RMIClients and the Multicast servers.
 *
 * @author Rodrigo Martins
 */
public class RMIServer extends UnicastRemoteObject implements RMI_S {
    private static final long serialVersionUID = 1L;

    OAuthService service;

    /**
     * Treemap with the client ids and their respective interface.
     */
    private Map<Integer, RMI_C> listClient;

    /**
     * ArrayList with the ids of the multicast servers that are alive.
     */
    private ArrayList<Long> aliveMulticastServers;

    /**
     * Treemap with the client ids and their respective ArrayLists with their notifications.
     */
    private Map<Integer, ArrayList<String>> notificationBoard;

    /**
     * The PID of the primary RMI server. Used for STONITH.
     */
    private int primaryPID;


    //==========================================================

    /**
     * Constructor of the class RMIServer.
     * @throws RemoteException caught outside of the scope.
     */
    private RMIServer() throws RemoteException {
        super();
        this.listClient = new TreeMap<>();
        this.aliveMulticastServers = new ArrayList<Long>();
        this.notificationBoard = new TreeMap<>();
    }


    //=============================[BASIC]=============================

    /**
     * RMI method: gives the server the client's reference (for callback) and gives/updates their id.
     *
     * @param client RMI_C - reference to the client.
     * @param clientId int - the client's id.
     * @return returns a id.
     *
     * @author Rodrigo Martins
     */
    public int subscribe(RMI_C client, int clientId){

        //If the client has the default id a new one is given.
        if(clientId == 0) {
            int id = 5000; //above 5000 due to port availability.

            //Assures there are now two identical ids.
            while (this.listClient.containsKey(id))
                id++;

            this.listClient.put(id, client); //Updates the client treemap with its current interface.

            System.out.println("Subscribed client number " + id + "!");

            return id;
        }

        this.listClient.put(clientId, client);

        return clientId;
    }

    /**
     * RMI method: logs the user and gives him his definitive ID.
     *
     * @param username String - name of the user.
     * @param password String - password of the user.
     * @param clientId int - unique identifier of the client.
     * @param client RMI_C - reference to the client's interface. Used for remote method invocation.
     * @return return a array of two ints; first is the user's definitive id adnt he second the login status (1 for admins, 0 for users and -1 for non registered users).
     * @throws RemoteException caught outside of scope.
     */
    @Override
    public int[] login(String username, String password, int clientId, RMI_C client) throws RemoteException {
        String command = "id | " + clientId + " ; type | login ; username | " + username + " ; password | " + password + "";

        //Sends the Multicast server the protocol and waits for its response.
        String response = sendToGroup(command, clientId);

        String [] newResponse = response.split("[ |;]+");

        int [] idStatus;

        switch(newResponse[newResponse.length - 1]){

            case("Admin"): //If the client is an admin

                //Getting the definitive id and the validation int.
                idStatus = new int[2];
                idStatus[0] = Integer.parseInt(newResponse[7]);
                idStatus[1] = 1;

                //Uppdate the user's interface
                this.listClient.put( (Integer) Integer.parseInt(newResponse[7]), client);

                //Check wether or not the user has any pending notifications.
                this.checkNotifications(idStatus[0], client);

                //return an int for validation.
                return idStatus;

            case("User"): //If the client is an user

                //Getting the definitive id and the validation int.
                idStatus = new int[2];
                idStatus[0] = Integer.parseInt(newResponse[7]);
                idStatus[1] = 0;

                //Uppdate the user's interface
                this.listClient.put( (Integer) Integer.parseInt(newResponse[7]), client);

                //Check wether or not the user has any pending notifications.
                this.checkNotifications(idStatus[0], client);

                //return an int for validation.
                return idStatus;

            case("incorrect"): //If the client entered the wrong credentials

            default:
                idStatus = new int[2];
                idStatus[0] = 0;
                idStatus[1] = -1;

                return idStatus;
        }
    }

    /**
     * Auxiliary method of login. Checks if the user has any notifications and sends them to the client.
     *
     * @param clientId int - unique identification of the client.
     * @param client RMI_C reference to the client. Used for remote method invocation.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    private void checkNotifications(int clientId, RMI_C client) throws RemoteException{
        //Check if the user has any notifications pending
        ArrayList<String> notifications;

        String response = null;

        //Sends them to the client
        if( (notifications = this.notificationBoard.get(clientId)) != null)
            response = client.sendToClient(notifications);

        //Removes the now sent notifications from the board
        if(response != null)
            this.notificationBoard.put(clientId, null);
    }

    /**
     * RMI method: receives the credentials of the new user and relays them to the Multicast servers.
     *
     * @param newUsername String with the new user's name.
     * @param newPassword String with the new user's password.
     * @param clientId int - unique identification of the client.
     * @return returns validation int; -1 if username already taken, 1 if registration was successful.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    @Override
    public int register(String newUsername, String newPassword, int clientId) throws RemoteException {
        String command = "id | " + clientId + " ; type | register ; username | " + newUsername + " ; password | " + newPassword + "";

        //Sends the credentials to the multicast servers.
        String response = sendToGroup(command, clientId);

        if(response.equals("type | status ; registered | negative ; msg | Failed to register, choose different username"))
            return -1;

        return 1;
    }

    /**
     * RMI method: takes the terms given and sends them to a randomly selected alive multicast server.
     *
     * @param searchQuery String - words requested by user to be searched.
     * @param clientId int - unique identifier of the client.
     * @return returns a String with all the search results. Links to the pages along with a title and a description.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    @Override
    public String searchWords(String searchQuery, int clientId) throws RemoteException {
        String[] query = searchQuery.split(" ");

        String command = "id | " + clientId + " ; type | termSearch ; terms | ";

        //Appends every term entered to the protocol.
        for(int i=0 ; i<query.length ; i++){
            command += query[i] + ",";
        }

        //Sends the terms to the multicast server and waits for the response.
        String response = sendToGroup(command, clientId);

        //returns the search results
        return response;
    }

    //=============================[USER]=============================

    /**
     * RMI method: takes the url given and returns the urls of all the pages that mention it.
     *
     * @param searchQuery String - url given by user.
     * @param clientId int - unique identifier of the client.
     * @return returns a String with the results of the search.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    @Override
    public String searchPagesConnectedToPage(String searchQuery, int clientId) throws RemoteException {
        String command = "id | " + clientId + " ; type | byReference ; url | " + searchQuery + "";

        //Sends the url to the multicast server and waits for the response.
        String response = sendToGroup(command, clientId);

        //returns the search results
        return response;
    }

    /**
     * RMI method: returns all the terms searched by the user.
     *
     * @param clientId int - unique identifier of the client.
     * @return returns a String with every term searched by the user.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    @Override
    public String getSearchHistory(int clientId) throws RemoteException {
        String command = "id | " + clientId + " ; type | userSearches";

        //Sends the request to the multicast server and waits for the response.
        String response = sendToGroup(command, clientId);

        //returns the user's search history
        return response;
    }

    //=============================[ADMIN]=============================

    /**
     * RMI method: gives the url, inserted by the user, to the multicast server for indexation.
     *
     * @param urlToIndex String with the url to be indexed.
     * @param clientId int -  unique identifier of the client.
     * @return returns String for validation.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    @Override
    public String adminIndexUrl(String urlToIndex, int clientId) throws RemoteException {
        Random generator = new Random();
        String command;

        command = "id | " + clientId + " ; type | urlInsert ; url | " + urlToIndex + " ; serverId | " + 2  + "";

        //Sends the url to the multicast server and waits for the response.
        String response = sendToGroup(command, clientId);

        if(response.equals("type | status ; urlInsertion | successful ; msg | Indexation In Progress"))
            return "Indexation has begun.";

        return "Indexation unsuccessful.";
    }

    //todo:
    /**
     * UNFINISHED METHOD.
     *
     * @param clientId
     * @return
     * @throws RemoteException
     */
    @Override
    public ArrayList<String> adminGetAdminPage(int clientId) throws RemoteException {
        //todo: this isn't done yet

        String command;

        command = "id | " + clientId + " ; type | adminPage ; serverId | " + 2  + "";

        String response = sendToGroup(command, clientId);

        ArrayList<String> statistics = new ArrayList<>();
        statistics.add(response.split(Pattern.quote(";"))[response.split(Pattern.quote(";")).length - 2]);
        statistics.add(response.split(Pattern.quote(";"))[response.split(Pattern.quote(";")).length - 1]);
        
        return statistics;
    }

    public String facebookAuth() throws RemoteException {

        String NETWORK_NAME = "Facebook";
        String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";
        Token EMPTY_TOKEN = null;

        // Replace these with your own api key and secret
        String apiKey = "2592229557523032";
        String apiSecret = "867b0cf29c9e909f0a05a2c7b151b738";

        OAuthService service = new ServiceBuilder()
                .provider(FacebookApi2.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback("https://ucBusca.vita.rodrigo:8443/ucBuscaWAR/faceauth2.action") // Do not change this.
                .scope("public_profile")
                .build();

        this.service = service;

        String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);

        return authorizationUrl;
    }

    public String verifyToken(String code) {

        Verifier verifier = new Verifier(code);
        String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";
        Token EMPTY_TOKEN = null;

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        Token accessToken = this.service.getAccessToken(EMPTY_TOKEN, verifier);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken + " )");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL, this.service);
        service.signRequest(accessToken, request);
        Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        String data = response.getBody();

        return data;
    }

    public String yandexLangDetector(String text) {

        try {
            String link = "https://translate.yandex.net/api/v1.5/tr/detect?key=trnsl.1.1.20191214T152639Z.763aac1b6a3b7865.b01c2b569456e48f6058fd02c10d4f1dd7c84c3f&text=" + java.net.URLEncoder.encode(text, "UTF-8").replaceAll("\\+", "%20");

            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            OutputStream os = connection.getOutputStream();
            os.flush();
            InputStream inputStreamObject = connection.getInputStream();

            InputStreamReader isReader = new InputStreamReader(inputStreamObject);
            BufferedReader reader = new BufferedReader(isReader);
            StringBuffer sb = new StringBuffer();
            String str;

            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }

            String lang = sb.toString().split("lang=")[1].substring(1,3);
            return lang;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String yandexTranslate(String text, String lang) {

        try {

            String link = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20191214T152639Z.763aac1b6a3b7865.b01c2b569456e48f6058fd02c10d4f1dd7c84c3f&text="+java.net.URLEncoder.encode(text, "UTF-8").replaceAll("\\+", "%20")+"&lang="+lang+"-pt";
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            OutputStream os = connection.getOutputStream();
            os.flush();
            InputStream inputStreamObject = connection.getInputStream();

            InputStreamReader isReader = new InputStreamReader(inputStreamObject);
            BufferedReader reader = new BufferedReader(isReader);
            StringBuffer sb = new StringBuffer();
            String str;

            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }

            String translation = sb.toString().split("text")[1];
            System.out.println(translation);
            translation = translation.substring(4, translation.length() - 3);

            return translation;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

        /**
     * RMI method: grants the desired user admin privileges.
     *
     * @param username String - name of the user to be given admin permissions.
     * @param clientId int - unique identifier of the client.
     * @return returns a String for validation.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    @Override
    public int adminGiveAdminPrivilege(String username, int clientId) throws RemoteException {
        String command = "id | " + clientId + " ; type | adminAccess ; user | " + username + "";

        //Sends the user's name to the multicast server and waits for the response.
        String response = sendToGroup(command, clientId);

        //Check if whether or not that user exist
        switch(response){
            case("No user found"):
                return -1;

            case("Permission granted"):
                return 1;

            default:
                return 0;
        }
    }

    @Override
    public ArrayList<String> checkNotification(int clientId) throws RemoteException {
        //Check if the user has any notifications pending
        ArrayList<String> notifications;

        //Sends them to the client
        if( (notifications = this.notificationBoard.get(clientId)) != null){
            this.notificationBoard.put(clientId, null);
            return notifications;
        }

        return null;
    }

    //==============================================================

    /**
     * Generic method to relay information from the client to the multicast servers.
     *
     * @param message String - protocol specific to the type of request.
     * @param clientId int - unique identifier of the client.
     * @return returns a String with the server's response.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    public String sendToGroup(String message, int clientId) throws RemoteException {
        String MULTICAST_ADDRESS = "224.0.0.0"; //Multicast group's address
        int PORT = 4321; //Port of the Multicast socket

        MulticastSocket socket = null;
        String returnMessage = "";
        try {
            socket = new MulticastSocket(clientId); //Binded multicast socket - sends and receives packets.
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS); //Gets the reference to the multicast group
            socket.joinGroup(group); //Connects the socket to the multicast group

            //Turns the message into bytes, so that it can be send through the socket
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);

            socket.send(packet);

            //=================================================================

            //Creates a packet and waits for response
            byte[] rcvBuffer = new byte[16000];
            DatagramPacket rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
            socket.receive(rcvPacket);

            //Gets response from packet
            returnMessage = new String(rcvPacket.getData(), 0, rcvPacket.getLength());

            System.out.println(returnMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }

        return returnMessage;
    }

    /**
     * Auxiliary method. Creates a registry and binds the server to it.
     *
     * @throws RemoteException caught of scope.
     *
     * @author Rodrigo Martins
     */
    public void createAndBind() throws RemoteException{
        Registry r = LocateRegistry.createRegistry(7001);
        r.rebind("primary", this);

        System.out.println("Primary server ready!");
    }

    /**
     * Cycle executed by the secondary RMI server. When the primary server crashes, the secondary checks its vitals for 5 seconds.
     * If after that time the primary is still dead, the secondary takes over and becomes the new primary server.
     * When the other server returns, it becomes the secondary.
     *
     * Applies STONITH to assure the death of the old primary server.
     *
     * @author Rodrigo Martins
     */
    public void secondary() throws IOException {
        int tries = 0; //Counter used to track how many time the connection has been tested

        while(true){
            try {
                LocateRegistry.getRegistry(7001).lookup("primary"); //Attempts to get the primary server's reference
                tries = 0; //If it succeeds, it resets the tries counter.
            } catch (NotBoundException | RemoteException e) {
                tries++;

                //When 5 seconds of retries (1 second in-between each try), the server terminates the other server - STONITH - to assure its death.
                if(tries>5) {
                    String cmd = "taskkill /F /PID " + this.primaryPID;
                    Runtime.getRuntime().exec(cmd);

                    break;
                }

                System.out.println("Try: " + tries + "\nPrimary still dead...");

                try{sleep(1000);}catch(InterruptedException ie) {System.out.println("uh?!");}
            }
        }
    }

    /**
     * RMI method: called by the secondary server; gives the primary server's PID for STONITH.
     *
     * @return int - PID of the primary RMI server.
     * @throws RemoteException caught outside of scope.
     */
    @Override
    public int getServerPID() throws RemoteException {

        //Gets the name of the process.
        String name =  ManagementFactory.getRuntimeMXBean().getName();

        //Splits the name to get the PID
        String [] newName = name.split("@");

        //returns the primary server's PID.
        return Integer.parseInt(newName[0]);
    }

    //==============================================================

    /**
     * RMIServer's main. Starts the server.
     *
     * @param args not used.
     *
     * @author Rodrigo Martins
     */
    public static void main(String[] args){
        RMIServer server = null;
        try {server = new RMIServer();} catch(RemoteException e) {e.printStackTrace();}

        server.start();
    }

    /**
     * RMIServer's start. Begins with the establishment of its registry adn proceeds to create the LifeChecker thread and the SendSyncOrder thread
     * before dedicating itself to receiving messages from the multicast servers.
     *
     * @author Rodrigo Martins
     */
    public void start() {
        while(true) {
            try {
                //Primary server tries to get the registry, causing it to fail since it started first and get caught by the exception.
                //The secondary succeeds and proceeds to the next line.
                RMI_S server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary");

                this.primaryPID = server.getServerPID(); //Secondary server gets the primary server's PID for STONITH.

                this.secondary(); //The secondary server starts its check cycle of the primary server's vitals.

                this.createAndBind(); //When the secondary server asserts that the primary is dead, it takes its place.

                break;
            } catch (NotBoundException | RemoteException e) {
                try{
                    this.createAndBind(); //Primary creates a registry and binds itself to it.

                    break;
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //CheckLifeThread lifeChecker = new CheckLifeThread(); //Thread that pings the multicast servers for their id.

        String MULTICAST_ADDRESS = "224.0.0.0"; //Multicast group.
        int PORT = 3999; //Fixed port of the RMI server.

        MulticastSocket socket = null;
        while(true) {
            try {
                socket = new MulticastSocket(PORT);  // create socket and bind it
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);

                while (true) {

                    //The server waits for any incoming packets from the multicast group.
                    byte[] buffer = new byte[8000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    //The server prints the message contained within the received packet.
                    System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(message);

                    String [] parsedMessage = message.split("[ |;-]+");

                    int clientId;
                    String newNotification;
                    RMI_C client;

                    //Analysis of the message
                    switch(parsedMessage[parsedMessage.length - 1]){
                        case("ImAlive"): //If it's assuring a multicast server's vitals.
                            this.aliveMulticastServers.add(Long.parseLong(parsedMessage[1])); //He's added to the list of alive multicast servers.

                            break;

                        case("IndexationComplete"): //If it's a notification regarding the completion of an indexation.

                            //SendSyncOrderThread syncIndex = new SendSyncOrderThread(); //Commands the multicast servers to merge their indexes

                            //Gets the id of the intended recipient of the notification.
                            clientId = Integer.parseInt(parsedMessage[3]);

                            //Creates the message and gets the most recent interface of the user
                            newNotification = "The url " + parsedMessage[5] + " has been indexed!";
                            client = this.listClient.get(clientId);

                            try {

                                String response = client.sendToClient(newNotification); //Tries relay the notification to the user

                                if(response.equals("WebSocketOffline")){
                                    ArrayList<String> clientNotifs;

                                    //We get, or create, a ArrayList<String> to keep the notification in the notification board
                                    if(this.notificationBoard.containsKey(clientId) && this.notificationBoard.get(clientId) != null) {
                                        clientNotifs = this.notificationBoard.get(clientId);
                                    } else {
                                        clientNotifs =  new ArrayList<>();
                                    }

                                    //The notification is added to the board.
                                    clientNotifs.add(newNotification);
                                    this.notificationBoard.put(clientId, clientNotifs);
                                }

                            } catch (RemoteException e){ //If the suer was offline

                                ArrayList<String> clientNotifs;

                                //We get, or create a ArrayList<String> to keep the notification in the notification board
                                if(this.notificationBoard.containsKey(clientId) && this.notificationBoard.get(clientId) != null) {
                                    clientNotifs = this.notificationBoard.get(clientId);
                                } else {
                                    clientNotifs =  new ArrayList<>();
                                }

                                //The notification is added to the board.
                                clientNotifs.add(newNotification);
                                this.notificationBoard.put(clientId, clientNotifs);

                            }

                            break;

                        case("AdminAccess"): //If the message is a notification of received admin privileges

                            //We get the recipient's id.
                            clientId = Integer.parseInt(parsedMessage[3]);

                            //We build the notification and get the user's most recent interface
                            newNotification = "You have been given admin permissions! Restart your client to access the admin functionalities.";
                            client = this.listClient.get(clientId);

                            try {

                                String response = client.sendToClient(newNotification); //We attempt to send the notification

                                if(response.equals("WebSocketOffline")){
                                    ArrayList<String> clientNotifs;

                                    //We get, or create, a ArrayList<String> to keep the notification in the notification board
                                    if(this.notificationBoard.containsKey(clientId) && this.notificationBoard.get(clientId) != null) {
                                        clientNotifs = this.notificationBoard.get(clientId);
                                    } else {
                                        clientNotifs =  new ArrayList<>();
                                    }

                                    //The notification is added to the board.
                                    clientNotifs.add(newNotification);
                                    this.notificationBoard.put(clientId, clientNotifs);
                                }

                            } catch (RemoteException e){ //If the user is offline

                                ArrayList<String> clientNotifs;

                                //We get, or create a ArrayList<String> to keep the notification in the notification board
                                if(this.notificationBoard.containsKey(clientId) && this.notificationBoard.get(clientId) != null) {
                                    clientNotifs = this.notificationBoard.get(clientId);
                                } else {
                                    clientNotifs =  new ArrayList<>();
                                }

                                //The notification is added to the board.
                                clientNotifs.add(newNotification);
                                this.notificationBoard.put(clientId, clientNotifs);

                            }


                            break;

                        default:
                            break;
                    }

                    //todo: RIP cbt

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }

        }

    }

    /**
     * Entity responsible for checking the multicast server's life signs.
     *
     * @author Rodrigo Martins
     */
    public class CheckLifeThread extends Thread {

        /**
         * Constructor of the class CheckLifeThread.
         */
        public CheckLifeThread() {
            super();
            this.start();
        }

        /**
         * CheckLifeThread's run. Creates and unbinded socket to send CheckLife messages to the multicast servers.
         * Every ten seconds it sends the CheckLife protocol message.
         *
         * @author Rodrigo Martins
         */
        @Override
        public void run(){
            String MULTICAST_ADDRESS = "224.0.0.0";
            int multicastPort = 4321;
            MulticastSocket checkLifeSocket = null;

            String check = "id | 1 ; type | CheckLife";

            try{
                checkLifeSocket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                checkLifeSocket.joinGroup(group);

                byte[] buffer = check.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);

                //A check life packet is sent every 20 seconds.
                while(true) {
                    checkLifeSocket.send(packet);
                    sleep(20000); //10s in-between checks
                    aliveMulticastServers.clear();
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * The entity responsible for requesting the synchronization of the word index and reference table of the multicast servers.
     *
     * @author Rodrigo Martins
     */
    public class SendSyncOrderThread extends Thread {

        /**
         * Constructor.
         */
        public SendSyncOrderThread() {
            super();
            this.start();
        }

        /**
         * SendSyncOrderThread's run. Creates an unbinded socket to send the SyncMaster protocol message.
         * Selects a random multicast server to give its TCP socket IP address and port.
         *
         * Sends one message every time it is called.
         *
         * @author Rodrigo Martins
         */
        public void run() {
            String MULTICAST_ADDRESS = "224.0.0.0";
            int multicastPort = 4321;
            MulticastSocket sendSyncOrder = null;

            Random generator = new Random();

            //A randomly selected multicast server will be selected to merge the indexes.
            String check = "id | 1 ; type | SyncMaster ; serverId | " + aliveMulticastServers.get(generator.nextInt(aliveMulticastServers.size() - 1)) + "";

            try{
                sendSyncOrder = new MulticastSocket();
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                sendSyncOrder.joinGroup(group);

                byte[] buffer = check.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);

                sendSyncOrder.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
