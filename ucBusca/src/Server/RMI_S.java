package Server;

import com.github.scribejava.core.model.Response;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * RMI interface for RMIServer.
 *
 * @author Rodrigo Martins
 */
public interface RMI_S extends Remote {

    //=============================[TECHNICAL]=============================

    /**
     * RMI method: called by the secondary server; gives the primary server's PID for STONITH.
     *
     * @return int - PID of the primary RMI server.
     * @throws RemoteException caught outside of scope.
     */
    int getServerPID() throws RemoteException;

    //=============================[BASIC]=============================
    /**
     * RMI method: gives the server the client's reference (for callback) and gives/updates their id.
     *
     * @param client reference to the client.
     * @param clientId the client's id.
     * @return returns a id.
     *
     * @author Rodrigo Martins
     */
    int subscribe(RMI_C client, int clientId) throws RemoteException;

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
    int[] login(String username, String password, int clientId, RMI_C client) throws RemoteException;

    /**
     * RMI method: receives the credentials of the new user and relays them to the Multicast servers.
     *
     * @param username String with the new user's name.
     * @param password String with the new user's password.
     * @param clientId unique identification of the client.
     * @return returns validation int; -1 if username already taken, 1 if registration was successful.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    int register(String username, String password, int clientId) throws RemoteException;

    /**
     * RMI method: takes the terms given and sends them to a randomly selected alive multicast server.
     *
     * @param searchQuery words requested by user to be searched.
     * @param clientId unique identifier of the client.
     * @return returns aString with all the search results. Links to the pages along with a title and a description.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    String searchWords(String searchQuery, int clientId) throws RemoteException;

    //=============================[USER]=============================
    /**
     * RMI method: takes the url given and returns the urls of all the pages that mention it.
     *
     * @param searchQuery url given by user.
     * @param clientId unique identifier of the client.
     * @return returns a String with the results of the search.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    String searchPagesConnectedToPage(String searchQuery, int clientId) throws RemoteException;

    /**
     * RMI method: returns all the terms searched by the user.
     *
     * @param clientId unique identifier of the client.
     * @return returns a String with every term searched by the user.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    String getSearchHistory(int clientId) throws RemoteException;

    //=============================[ADMIN]=============================
    /**
     * RMI method: gives the url, inserted by the user, to the multicast server for indexation.
     *
     * @param urlToIndex String with the url to be indexed.
     * @param clientId int, unique identifier of the client.
     * @return returns String for validation.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    String adminIndexUrl(String urlToIndex, int clientId) throws RemoteException;

    /**
     * UNFINISHED METHOD.
     *
     * @param clientId
     * @return
     * @throws RemoteException
     */
    ArrayList<String> adminGetAdminPage(int clientId) throws RemoteException;

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
    int adminGiveAdminPrivilege(String username, int clientId) throws RemoteException;

    ArrayList<String> checkNotification(int clientId) throws RemoteException;

    /**
     * RMI method: facebook authentication.
     *
     * @return returns a String for validation.
     * @throws RemoteException caught outside of scope.
     *
     * @author Rodrigo Martins
     */
    String facebookAuth() throws RemoteException;

    String verifyToken(String url) throws RemoteException;

    String yandexLangDetector(String text) throws RemoteException;
}
