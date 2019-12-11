package ucBusca.model;

import Server.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class UcBuscaBean {
    private RMI_S server;
    private int clientId = 0;

    public UcBuscaBean() {
        try {
            server = (RMI_S) Naming.lookup("primary");

            server.subscribe(clientId);
        }
        catch(NotBoundException | MalformedURLException | RemoteException e) {
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
}
