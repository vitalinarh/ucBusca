package Multicast;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

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

    //todo:
    /**
     * UNFINISHED METHOD.
     *
     * @param username
     * @param password
     * @param clientId
     * @param client
     * @return
     * @throws RemoteException
     */
    @Override
    public int[] login(String username, String password, int clientId, RMI_C client) throws RemoteException {
        String command = "id - " + clientId + " ; type - login ; username - " + username + " ; password - " + password + "";

        String response = sendToGroup(command, clientId);

        String [] newResponse = response.split("[ |;]+");

        int [] idStatus;

        switch(newResponse[newResponse.length - 1]){

            case("Admin"):

                idStatus = new int[2];
                idStatus[0] = Integer.parseInt(newResponse[7]);
                idStatus[1] = 1;

                this.listClient.put( (Integer) Integer.parseInt(newResponse[7]), client);

                //todo: Need to update the notification database on the multicast server. And if the RMIServer dies, the notifications also die.
                //todo: So, do I forget about storing the notifs on the RMIServer, or do I update them from time to time?
                //todo: If I had the notificationBoard on the Multicast servers, I would have to sync them and that's not good.
                //todo: But if I wrote them to disc and read from there every single time... Need to think about it.

                //todo: Forget about making the offline callbacks a surefire. Online callbacks and most of the offline ones work too.

                this.checkNotifications(idStatus[0], client);

                return idStatus;

            case("User"):

                idStatus = new int[2];
                idStatus[0] = Integer.parseInt(newResponse[7]);
                idStatus[1] = 0;

                this.listClient.put( (Integer) Integer.parseInt(newResponse[7]), client);

                this.checkNotifications(idStatus[0], client);

                return idStatus;
            case("incorrect"):

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
     * @throws java.rmi.RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    private void checkNotifications(int clientId, RMI_C client) throws java.rmi.RemoteException{
        //Check if the user has any notifications pending
        ArrayList<String> notifications;

        //Sends them to the client
        if( (notifications = this.notificationBoard.get(clientId)) != null)
            client.sendToClient(notifications);

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
        String command = "id - " + clientId + " ; type - register ; username - " + newUsername + " ; password - " + newPassword + "";

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

        String command = "id - " + clientId + " ; type - termSearch ; terms - ";

        for(int i=0 ; i<query.length ; i++){
            command += query[i] + ",";
        }

        Random generator = new Random();

        String response = sendToGroup(command, clientId);

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
        String command = "id - " + clientId + " ; type - byReference ; url - " + searchQuery + "";

        String response = sendToGroup(command, clientId);

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
        String command = "id - " + clientId + " ; type - userSearches";

        String response = sendToGroup(command, clientId);

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

        /*while(this.aliveMulticastServers.isEmpty()){
            System.out.println("I'm stuck");
        }*/

        command = "id - " + clientId + " ; type - urlInsert ; url - " + urlToIndex + " ; serverId - " + 2 /*this.aliveMulticastServers.get(generator.nextInt(aliveMulticastServers.size() - 1))*/ + "";

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
    public String adminGetAdminPage(int clientId) throws RemoteException {
        //todo: this isn't done yet
        return null;
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
        String command = "id - " + clientId + " ; type - adminAccess ; user - " + username + "";

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
        int tries = 0;

        while(true){
            try {
                LocateRegistry.getRegistry(7001).lookup("primary");
                tries = 0;
            } catch (NotBoundException | RemoteException e) {
                tries++;

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

        String name =  ManagementFactory.getRuntimeMXBean().getName();

        String [] newName = name.split("@");

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
                RMI_S server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary");

                this.primaryPID = server.getServerPID();

                this.secondary();

                this.createAndBind();

                break;
            } catch (NotBoundException | RemoteException e) {
                try{
                    this.createAndBind();

                    break;
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //CheckLifeThread lifeChecker = new CheckLifeThread();

        String MULTICAST_ADDRESS = "224.0.0.0";
        int PORT = 3999;

        MulticastSocket socket = null;
        while(true) {
            try {
                socket = new MulticastSocket(PORT);  // create socket and bind it
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);

                while (true) {
                    byte[] buffer = new byte[8000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(message);

                    String [] parsedMessage = message.split("[ |;-]+");

                    int clientId;
                    String newNotification;
                    RMI_C client;

                    switch(parsedMessage[parsedMessage.length - 1]){
                        case("ImAlive"):
                            this.aliveMulticastServers.add(Long.parseLong(parsedMessage[1]));

                            break;

                        case("IndexationComplete"):

                            //SendSyncOrderThread syncIndex = new SendSyncOrderThread();

                            clientId = Integer.parseInt(parsedMessage[3]);

                            newNotification = "The url " + parsedMessage[5] + " has been indexed!";
                            client = this.listClient.get(clientId);

                            try {

                                String response = client.sendToClient(newNotification);

                            } catch (RemoteException e){

                                ArrayList<String> clientNotifs;

                                if(this.notificationBoard.containsKey(clientId) && this.notificationBoard.get(clientId) != null) {
                                    clientNotifs = this.notificationBoard.get(clientId);
                                } else {
                                    clientNotifs =  new ArrayList<>();
                                }

                                clientNotifs.add(newNotification);
                                this.notificationBoard.put(clientId, clientNotifs);

                            }

                            break;

                        case("AdminAccess"):

                            clientId = Integer.parseInt(parsedMessage[3]);

                            newNotification = "You have been given admin permissions! Restart your client to access the admin functionalities.";
                            client = this.listClient.get(clientId);

                            try {

                                String response = client.sendToClient(newNotification);

                            } catch (RemoteException e){
                                System.out.println("User was offline - admin access");

                                ArrayList<String> clientNotifs;

                                if(this.notificationBoard.containsKey(clientId) && this.notificationBoard.get(clientId) != null) {
                                    clientNotifs = this.notificationBoard.get(clientId);
                                } else {
                                    clientNotifs =  new ArrayList<>();
                                }

                                clientNotifs.add(newNotification);
                                this.notificationBoard.put(clientId, clientNotifs);

                            }


                            break;

                        default:

                        //todo: If it receives an "indexation completed" notification, it calls the SendSyncOrderThread to sync the Multicast servers' threads.

                        //todo: If it receives an "admin permissions given" notification, it tries to send it to the client. If it fails - user is offline -,
                        //todo: it updates the notification board.

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

            String check = "id - 1 ; type - CheckLife";

            try{
                checkLifeSocket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                checkLifeSocket.joinGroup(group);

                byte[] buffer = check.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);

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

            String check = "id - 1 ; type - SyncMaster ; serverId - " + aliveMulticastServers.get(generator.nextInt(aliveMulticastServers.size() - 1)) + "";

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
