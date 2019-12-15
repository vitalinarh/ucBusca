package ucBusca.model;

import Server.*;
import com.github.scribejava.core.model.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.omg.PortableInterceptor.SUCCESSFUL;
import ucBusca.ws.WebSocketAnnotation;

//todo:yandex imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Class that acts as the client. It calls remote methods in the RMIServer and waits for their responses.
 * @author Rodrigo Martins
 */
public class UcBuscaBean extends UnicastRemoteObject implements RMI_C {
    /**
     * The reference to the RMIServer.
     * Used for remote method invocation.
     */
    private RMI_S server;
    /**
     * The id of the bean/client. Unique. Identifies the client.
     */
    private int clientId = 0;
    /**
     * Client's name.
     */
    private String username = null;
    /**
     * Used to tell the AdminPageUpdaterThread if the user is still on the admin page.
     * @see #getAdminPage()
     * @see AdminPageUpdaterThread
     */
    private boolean isOnAdminPage;
    /**
     * Reference to the AdminPageUpdaterThread instance in use.
     * @see #getAdminPage()
     * @see AdminPageUpdaterThread
     */
    private AdminPageUpdaterThread aput;


    /**
     * Constructor of the bean.
     * Connects to the remote server and keeps its reference.
     * @throws RemoteException
     */
    public UcBuscaBean() throws RemoteException {
        super();
        try {

            server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary"); //Gets the RMIServer's reference

            this.clientId = server.subscribe(this, clientId);
        }
        catch(NotBoundException | RemoteException e) {
            e.printStackTrace(); // what happens *after* we reach this line?
        }
    }

    /**
     * Calls the remote method in the RMIServer to login the user.
     * @param username String - Credential given by the anonymous user.
     * @param password String - Credential given by the anonymous user.
     * @return int - if the login was successful, or not, along with if the user is and admin, or not.
     */
    public int getLogin(String username, String password){
        try {
            int [] loginStatus = server.login(username, password, this.clientId, this);

            this.clientId = loginStatus[0];

            return loginStatus[1];

        } catch (RemoteException e) {
            e.printStackTrace();

            return -2;
        }
    }

    /**
     * Calls the remote method in the RMIServer to search a query.
     * @param search String - Query given by the user.
     * @return String - the search results.
     */
    public String getSearchResults(String search) {
        try {
            String searchResults = server.searchWords(search, this.clientId);

            return searchResults;
        }
        catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calls the remote method in the RMIServer to search pages that reference a given URL.
     * @param url String - url to search.
     * @return String - contains all the urls that reference the given url.
     */
    public String getSearchPagesResults(String url) {
        try {
            String searchResults = server.searchPagesConnectedToPage(url, this.clientId);

            return searchResults;
        }
        catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calls the remote method in the RMIServer to promote a user to admin.
     * @param userToAdmin String - name of the user to promote.
     * @return int - validation of the promotion.
     */
    public int getUserAdmined(String userToAdmin){
        try {
            int validation = server.adminGiveAdminPrivilege(userToAdmin, clientId);

            return validation;

        } catch (RemoteException e) {
            e.printStackTrace();

            return -2;

        }
    }

    /**
     * Calls the remote method in the RMIServer to get a url indexed.
     * @param urlToIndex String - url to index.
     * @return String - validation.
     */
    public String getURLIndexed(String urlToIndex){
        try {
            String response = server.adminIndexUrl(urlToIndex, clientId);

            return response;

        } catch (RemoteException e) {
            e.printStackTrace();

            return "Failed to index url";
        }

    }

    /**
     * Calls the remote method in the RMIServer to register an anonymous user.
     * @param username String - the user's desired name.
     * @param password String - the user's desired password.
     * @return int - tells if the registration was successful, or if there already was a user with the desired username.
     */
    public int getRegister(String username, String password){
        try {
            int validation = server.register(username, password, this.clientId);

            return validation;

        } catch (RemoteException e) {
            e.printStackTrace();

            return -2;
        }

    }

    /**
     * Calls the remote method in the RMIServer to receive a user's search history.
     * @return String - user's search history.
     */
    public String getSearchHistory(){
        try {

            return server.getSearchHistory(this.clientId);

        } catch (RemoteException e) {
            e.printStackTrace();

            return null;
        }
    }

    public String getFacebookAuth(){
        try{
            return server.facebookAuth();

        } catch (RemoteException e) {

            e.printStackTrace();
            return null;
        }

    }

    public String getLanguage(String text){
        try {

            return server.yandexLangDetector(text);

        } catch (RemoteException e) {

            e.printStackTrace();
            return null;
        }
    }

    public String getTranslation(String text, String lang){
        try {

            return server.yandexTranslate(text, lang);

        } catch (RemoteException e) {

            e.printStackTrace();
            return null;
        }
    }

    public String getFacebookAuth2(String auth_url) throws RemoteException {

        return server.verifyToken(auth_url);

    }

    /**
     * Sets the user's name in the bean.
     * @param username String - the user's name.
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * Updates the isOnAdminPage and creates a AdminPageUpdaterThread instance.
     * @see AdminPageUpdaterThread
     */
    public void getAdminPage(){
        this.isOnAdminPage = true;
        this.aput = new AdminPageUpdaterThread();
    }

    /**
     * Changes isOnAdminPage when the user leaves the admin page and waits for the AdminPageUpdaterThread to die.
     * @throws InterruptedException
     */
    public void setIsOnAdminPage() throws InterruptedException {
        this.isOnAdminPage = false;
        this.aput.join();
    }

    /**
     * Deprecated: Meta 1
     * @param message ArrayList compiling all the notifications that regard the user.
     * @return
     */
    @Override
    public String sendToClient(ArrayList<String> message) { return null; }

    /**
     * Remote method called by the RMIServer to deliver a notification.
     * @param message String - notification to deliver.
     * @return String - validation  message for the RMIServer.
     * @throws RemoteException
     */
    @Override
    public String sendToClient(String message) throws RemoteException {
        if(WebSocketAnnotation.userIDs.contains(this.clientId)){
            int index = 0;
            for(Integer userID:WebSocketAnnotation.userIDs) {
                if (userID == this.clientId)
                    break;

                index++;
            }

            WebSocketAnnotation ws = (WebSocketAnnotation) WebSocketAnnotation.users.toArray()[index];

            try {
                ws.session.getBasicRemote().sendText(message);

                return "Notification Sent";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "WebSocketOffline";
    }

    /**
     * Class that extends thread.
     * It calls a remote method in the RMIServe every 10 seconds to retrieve statistics related to the Admin page.
     */
    public class AdminPageUpdaterThread extends Thread{
        /**
         * Last most search words received.
         * If the received statistics match the previous, no changes are detected in the admin page.
         */
        private String lastMostSearchedWords = null;
        /**
         * Last most search pages received.
         * If the received statistics match the previous, no changes are detected in the admin page.
         */
        private String lastMostSearchedPages = null;


        /**
         * Constructor.
         * Starts the thread.
         */
        public AdminPageUpdaterThread(){
            super();
            this.start();
        }

        public void run() {
            while(isOnAdminPage){

                try {
                    ArrayList<String> statistics = server.adminGetAdminPage(clientId);



                    if (WebSocketAnnotation.userIDs.contains(clientId)) {
                        int index = 0;
                        for (Integer userID : WebSocketAnnotation.userIDs) {
                            if (userID == clientId)
                                break;

                            index++;
                        }

                        WebSocketAnnotation ws = (WebSocketAnnotation) WebSocketAnnotation.users.toArray()[index];

                        for(String statistic:statistics){
                            if(!statistic.equals(this.lastMostSearchedPages) && !statistic.equals(this.lastMostSearchedWords))
                                ws.session.getBasicRemote().sendText("Statistics!" + statistic.split(Pattern.quote("|"))[1]);
                        }
                    }

                    sleep(10000);


                } catch (IOException | InterruptedException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Gets the user's id.
     * @return int - user's id.
     */
    public int getClientId(){
        return this.clientId;
    }
}
