package ucBusca.model;

import Server.*;
import com.github.scribejava.core.model.Response;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.regex.Pattern;

//todo:yandex imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UcBuscaBean extends UnicastRemoteObject implements RMI_C {
    private RMI_S server;
    private int clientId = 0;

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

    public String getLanguage(String text){
        try {
            System.out.println(text);
            server.yandexLangDetector(text);
            return null;
        } catch (RemoteException e) {

            e.printStackTrace();
            return null;
        }
    }

    public String getFacebookAuth2(String auth_url) throws RemoteException {

        return server.verifyToken(auth_url);

    }

    @Override
    public String sendToClient(ArrayList<String> message) throws RemoteException {
        return null;
    }

    @Override
    public String sendToClient(String message) throws RemoteException {
        return null;
    }
}
