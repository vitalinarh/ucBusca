package Server;

import java.rmi.Remote;
import java.util.ArrayList;

/**
 * RMI interface for the RMIClient.
 *
 * @author Rodrigo Martins
 */
public interface RMI_C extends Remote {
    /**
     * RMI method: prints on the client the notifications from the multicast server.
     *
     * @param message ArrayList compiling all the notifications that regard the user.
     * @return String for validation.
     *
     * @author Rodrigo Martins
     */
    String sendToClient(ArrayList<String> message) throws java.rmi.RemoteException;

    String sendToClient(String message) throws java.rmi.RemoteException;
}
