package Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Thread.sleep;

//Class for the client; extends UnicastRemoteObject - so it can be referenced - and implements the corresponding RMI interface

/**
 * Class that will represent the client.
 * It will invoke remote methods, through rmi, to contact the RMI server.
 * @author Rodrigo Martins
 */
public class RMIClient extends UnicastRemoteObject implements RMI_C {
    /**
     * The id of the client.
     * By default, the id is 0. When the client subscribes, a number beyond 5000 is given.
     *
     * After a user has logged in for the first time, the id given to him is permanent.
     */
    private int clientId = 0;

    /**
     * Constructor of the class RMIClient.
     * @throws RemoteException caught outside of scope.
     */
    private RMIClient() throws RemoteException{
        super();
    }

    //RMI method: prints on the client the server's response to his message

    /**
     * RMI method: prints on the client the notifications from the multicast server.
     *
     * @param message ArrayList String compiling all the notifications that regard the user.
     * @return String for validation.
     *
     * @author Rodrigo Martins
     */
    public String sendToClient(ArrayList<String> message) throws RemoteException{

        //Prints every single notification and waits for the user input in-between
        for(int i=0 ; i<message.size() ; i++) {
            System.out.println("\n" + message.get(i));
        }

        return "Notifications sent.";
    }

    /**
     * RMI method: prints on the client the notifications from the multicast server.
     *
     * @param message String with a notification that regards the user.
     * @return String for validation.
     *
     * @author Rodrigo Martins
     */
    @Override
    public String sendToClient(String message) throws RemoteException {

        System.out.println(message);

        return "Notification sent.";
    }

    //====================================================================================================

    /**
     * Gathers the terms to be searched for the client and sends the to the RMI server.
     *
     * @param scan Scanner - reads the user input.
     * @param server RMI_s - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void searchWords(Scanner scan, RMI_S server) throws RemoteException{
        System.out.println("Enter the desired query:");
        String searchQuery = scan.nextLine();

        //Sends the search terms to the RMIServer
        String response = server.searchWords(searchQuery, this.clientId);
        System.out.println(response + "");

        System.out.println("Press enter to continue to menu...");
        scan.nextLine();
    }

    /**
     * Sends to RMIServer the credentials for the clients new account.
     *
     * @param scan Scanner - reads the user input.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void registerUser(Scanner scan, RMI_S server) throws RemoteException{
        System.out.println("Enter the desired username:");
        String newUsername = scan.nextLine();

        System.out.println("Enter the desired password:");
        String newPassword = scan.nextLine();

        //Sends the credentials to the RMIServer
        int validation = server.register(newUsername, newPassword, this.clientId);

        if(validation == -1)
            System.out.println("Username already taken.\nReturning to main menu...");

    }

    /**
     * If the user has registered himself, gives access to either the user menu, or the admin menu (depending on the permission level of the user).
     *
     * @param scan Scanner - reads the user input.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void loginUser(Scanner scan, RMI_S server) throws RemoteException{
        System.out.println("Enter your username:");
        String username = scan.nextLine();

        System.out.println("Enter your password:");
        String password = scan.nextLine();

        //Sends the credentials to the RMIServer for validation.
        int [] loginStatus = server.login(username, password, this.clientId, this);


        switch(loginStatus[1]){
            case(-1): //If the credentials are wrong.
                System.out.println("Invalid credentials.\n Either user does not exist or password is incorrect.");
                break;

            case(0): //If the user has no admin privileges.
                System.out.println("User logged in.\nHeading to user menu...");

                this.clientId = loginStatus[0];

                this.userMenu(scan, server);

                break;

            case(1): //If the user has admin privileges.
                System.out.println("Admin logged in.\nHeading to admin menu...");

                this.clientId = loginStatus[0];

                this.adminMenu(scan, server);

                break;

            default:
                break;
        }

        System.out.println("Returning to main menu...");
    }

    /**
     * Terminates the client.
     *
     * @author Rodrigo Martins
     */
    private void exitUcBusca(){
        System.out.println("Exiting ucBusca...\n\nBye-bye!");

        System.exit(0);
    }

    /**
     * With a given url returns all the pages that mention it.
     *
     * @param scan Scanner - reads the user input.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void searchUrlReferences(Scanner scan, RMI_S server) throws RemoteException{
        System.out.println("Enter the desired URL: ");
        String urlToSearch = scan.nextLine();

        //Sends the url to the RMIServer.
        String response = server.searchPagesConnectedToPage(urlToSearch, this.clientId);
        System.out.println(response);

        System.out.println("Press enter to proceed...");
        scan.nextLine();
    }

    /**
     * Shows the user the terms it has searched.
     *
     * @param scan Scanner - reads the user input.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void searchHistory(Scanner scan, RMI_S server) throws RemoteException{

        //Requests the RMIServer the search history of the user with this id.
        String response = server.getSearchHistory(this.clientId);
        System.out.println(response);

        System.out.println("Press enter to proceed...");
        scan.nextLine();
    }

    /**
     * Whenever the client detects the RMIServer is unreachable it tries to establish the connection for 30 seconds.
     * After that time, it lets the user know that the connection has been lost and gives him the option to either
     * retry connecting, or exit ucBusca.
     *
     * If the user chooses to retry reaching the server, the client, every 10 seconds, sees if the server is operational.
     * If it fails, it gives the user the choice to exit or try again once more.
     *
     * @param scan Scanner - reads the user input.
     * @return returns true if the user knows of the server crashing.
     *
     * @author Rodrigo Martins
     */
    private boolean retryConnection(Scanner scan){

        boolean hasCrashed = false;
        int tries = 0;

        while (true) {
            try {
                LocateRegistry.getRegistry(7001).lookup("primary"); //Tries to see if the registry exists.

                //If the user knows about the crash, we let him know that the connection has returned.
                if(tries>=30)
                    System.out.println("Connection returned!");

                return hasCrashed;

            } catch(RemoteException | NotBoundException re){ //if the server is still down.

                tries++; //updates the tries

                //If 30 seconds have passed (30 tries with 1 seconds of downtime in-between), we let the user know about the issue adn let him decide.
                if(tries>=30){
                    System.out.println("You have lost connection to the server.\n 1-Retry connection\n 2-Exit");
                    String decision = scan.nextLine();

                    hasCrashed = true;

                    if(decision.equals("2"))
                        this.exitUcBusca();
                }



                try { //While the user doesn't know about the issue, 1 second in-between calls. 10 seconds otherwise.

                    //( (tries<30) ? sleep(1000) : sleep(10000) );

                    if (tries < 30)
                        sleep(1000);
                    else
                        sleep(10000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the server's reference, if online, and subscribes to him.
     *
     * @return returns the RMIServer's interface.
     * @throws RemoteException gets caught outside of the scope.
     * @throws NotBoundException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private RMI_S connectToServer() throws RemoteException, NotBoundException{

        RMI_S server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary"); //Gets the RMIServer's reference

        this.clientId = server.subscribe(this, this.clientId); //Gives the server a reference of itself for Multicast's response and obtains a new id.

        return server;
    }

    /**
     * Receives the url from the admin and sends it to RMIServer for indexation.
     *
     * @param scan Scanner -  reads the admin's input.
     * @param clientId int - admin's id for request identification.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void indexUrl(Scanner scan, int clientId, RMI_S server) throws RemoteException{
        System.out.println("Insert the url to index: ");
        String urlToIndex = scan.nextLine();

        //Sends the RMIServer the url to index along with its id.
        String response = server.adminIndexUrl(urlToIndex, clientId);
        System.out.println(response);

        System.out.println("Press enter to proceed...");
        scan.nextLine();
    }

    /**
     * Sends the RMIServer the username of whom the admin wishes to give privileges to.
     *
     * @param scan  Scanner - reads the admin's input.
     * @param clientId int - the admin's id.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void giveUserAdminPrivileges(Scanner scan, int clientId, RMI_S server) throws RemoteException{
        System.out.println("Insert the username of the new admin: ");
        String newAdmin = scan.nextLine();

        int validation = server.adminGiveAdminPrivilege(newAdmin, clientId);

        //If the user exists, let's the admin know they are now a admin. Otherwise, tells the admin to retry.
        if(validation == 1)
            System.out.println("User " + newAdmin + " is now and admin.");
        else
            System.out.println("Operation failed.\n User does not exist.");
    }

    /**
     * Requests the RMIServer to show the admin statistics regarding the Multicast servers.
     *
     * @param scan Scanner - reads the admin's input.
     * @param clientId int - the admin's id.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    private void showAdminPage(Scanner scan, int clientId, RMI_S server) throws RemoteException{
        /*System.out.println("Entering administration page...");
        String response = server.adminGetAdminPage(clientId);

        System.out.println(response);

        System.out.println("Press enter to proceed...");*/
        scan.nextLine();
    }

    //====================================================================================================

    /**
     * RMIClient's main. Starts the client.
     *
     * @param args not used.
     * @throws RemoteException gets caught outside of the scope.
     *
     * @author Rodrigo Martins
     */
    public static void main(String[] args) throws RemoteException {
        RMIClient client = new RMIClient();
        client.anonMenu();
    }

    /**
     * The client's initial menu. Reserved for unregistered and not yet logged in users.
     *
     * @author Rodrigo Martins
     */
    private void anonMenu(){

        Scanner scan = new Scanner(System.in);

        while(true) {
            try {

                RMI_S server = this.connectToServer();


                while(true) {

                    System.out.println("===[MENU]===\n" +
                            " 1-Search words\n" +
                            " 2-Register\n" +
                            " 3-Login\n" +
                            " 4-Exit");


                    String chosenOption = scan.nextLine();

                    switch (chosenOption) {
                        case ("1"):
                            this.searchWords(scan, server);

                            break;

                        case ("2"):
                            this.registerUser(scan, server);

                            break;

                        case ("3"):
                            this.loginUser(scan, server);

                            break;

                        case ("4"):
                            this.exitUcBusca();

                            break;

                        default:
                            System.out.println("Invalid input.\nReturning to main menu...");

                            break;
                    }

                }

            } catch (RemoteException | NotBoundException e) {
                this.retryConnection(scan);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * User menu. Once a user has logged in, he gains access to new functionalities, such as: getting pages that mention a certain url and user search history.
     *
     * @param scan Scanner - reads the user's input.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     *
     * @author Rodrigo Martins
     */
    private void userMenu(Scanner scan, RMI_S server){
        boolean hasCrashed = false;

        while(true) {
            try {

                if(hasCrashed) {
                    server = this.connectToServer();
                }

                while(true) {

                    System.out.println("===[USER MENU]===\n" +
                            " 1-Search words\n" +
                            " 2-Search pages connected to specified page\n" +
                            " 3-Search history\n" +
                            " 4-Exit");

                    hasCrashed = false;

                    String chosenOption = scan.nextLine();

                    switch (chosenOption) {
                        case ("1"):
                            this.searchWords(scan, server);

                            break;

                        case ("2"):
                            this.searchUrlReferences(scan, server);

                            break;

                        case ("3"):
                            this.searchHistory(scan, server);

                            break;

                        case ("4"):
                            this.exitUcBusca();

                            break;
                        default:
                            System.out.println("Invalid input.\nReturning to main menu...");

                            break;
                    }

                }

            } catch (RemoteException | NotBoundException e) {
                hasCrashed = this.retryConnection(scan);

            } catch (Exception ignored) {

            }

        }
    }

    /**
     * Admin menu. Gives access to url indexation, giving admin privileges to other users and the administration page.
     *
     * @param scan Scanner - reads the admin's input.
     * @param server RMI_S - reference to the RMIServer. Used to invoke remote methods.
     *
     * @author Rodrigo Martins
     */
    private void adminMenu(Scanner scan, RMI_S server){
        boolean hasCrashed = false;

        while(true) {
            try {
                if(hasCrashed) {
                    server = this.connectToServer();
                }

                while(true) {


                    System.out.println("===[ADMIN MENU]===\n" +
                            " 1-Search words\n" +
                            " 2-Search pages connected to specified page\n" +
                            " 3-Search history\n" +
                            " 4-Index URL\n" +
                            " 5-Give a user admin privileges\n" +
                            " 6-Administration page\n" +
                            " 7-Exit");
                    hasCrashed = false;

                    String chosenOption = scan.nextLine();

                    switch (chosenOption) {
                        case ("1"):
                            this.searchWords(scan, server);

                            break;

                        case ("2"):
                            this.searchUrlReferences(scan, server);

                            break;

                        case ("3"):
                            this.searchHistory(scan, server);

                            break;

                        case("4"):
                            this.indexUrl(scan, this.clientId, server);

                            break;

                        case("5"):
                            this.giveUserAdminPrivileges(scan, this.clientId, server);

                            break;

                        case("6"):
                            this.showAdminPage(scan, this.clientId, server);

                            break;

                        case ("7"):
                            this.exitUcBusca();

                            break;
                        default:
                            System.out.println("Invalid input.\nReturning to main menu...");

                            break;
                    }

                }

            } catch (RemoteException | NotBoundException e) {
                System.out.println("what");
                hasCrashed = this.retryConnection(scan);

            } catch (Exception ignored) {

            }

        }
    }
}
