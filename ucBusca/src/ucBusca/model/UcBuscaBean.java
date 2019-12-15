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

import ucBusca.ws.WebSocketAnnotation;

public class UcBuscaBean extends UnicastRemoteObject implements RMI_C {
    private RMI_S server;
    private int clientId = 0;
    private String username = null;
    private boolean isOnAdminPage;
    private AdminPageUpdaterThread aput;

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

    public void setUsername(String username){
        this.username = username;
    }

    public int getClientId(){
        return this.clientId;
    }

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

    public int getUserAdmined(String userToAdmin){
        try {
            int validation = server.adminGiveAdminPrivilege(userToAdmin, clientId);

            return validation;

        } catch (RemoteException e) {
            e.printStackTrace();

            return -2;

        }
    }

    public String getURLIndexed(String urlToIndex){
        try {
            String response = server.adminIndexUrl(urlToIndex, clientId);

            return response;

        } catch (RemoteException e) {
            e.printStackTrace();

            return "Failed to index url";
        }

    }

    public int getRegister(String username, String password){
        try {
            int validation = server.register(username, password, this.clientId);

            return validation;

        } catch (RemoteException e) {
            e.printStackTrace();

            return -2;
        }

    }

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

    public String getFacebookAuth2(String auth_url) throws RemoteException {

        return server.verifyToken(auth_url);

    }

    public void getAdminPage(){
        this.isOnAdminPage = true;
        this.aput = new AdminPageUpdaterThread();
    }

    public void setIsOnAdminPage() throws InterruptedException {
        this.isOnAdminPage = false;
        this.aput.join();
    }

    @Override
    public String sendToClient(ArrayList<String> message) { return null; }

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


    public class AdminPageUpdaterThread extends Thread{
        private String lastMostSearchedWords = null;
        private String lastMostSearchedPages = null;


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
}
