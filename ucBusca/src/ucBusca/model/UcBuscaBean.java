package ucBusca.model;

import Server.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class UcBuscaBean {
    private RMI_S server;
    private int clientId = 0;

    public UcBuscaBean() {
        try {
            server = (RMI_S) LocateRegistry.getRegistry(7001).lookup("primary"); //Gets the RMIServer's reference

            this.clientId = server.subscribe(clientId);
        }
        catch(NotBoundException | RemoteException e) {
            e.printStackTrace(); // what happens *after* we reach this line?
        }
    }

    public int getLogin(String username, String password){
        try {
            int [] loginStatus = server.login(username, password, this.clientId);

            this.clientId = loginStatus[0];

            return loginStatus[1];

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
}
