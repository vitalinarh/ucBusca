package Multicast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.net.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

/**
 *Class Multicast.
 */
public class MulticastServer extends Thread {

    /**
     *Multicast Address, Default: 224.0.0.0.
     *
     */
    private String MULTICAST_ADDRESS = "224.0.0.0";
    /**
     * Defined port for reception of messages from RMI server.
     */
    private int PORTin = 4321;
    /**
     * Defined port for messages being sent to RMI server.
     */
    private int PORTout = 3999;
    /**
     * ArrayList that stores user information, like username, password, if user is admin, port id for RMI Client, and searches made by user.
     */
    private ArrayList<User> userList = new ArrayList<>();
    /**
     * Tree map that stores indexation, key: word, values: websites that mention it.
     */
    private Map<String, ArrayList<String>> index = new TreeMap<>();
    /**
     * Tree map that stores websites as key and websites that mention it as values.
     */
    private Map<String, ArrayList<String>> urlMentionsList = new TreeMap<>();
    /**
     * Tree map that stores information of all websites that were indexed, key : websites, values: title and citation.
     */
    private Map<String, String[]> wsInfo = new TreeMap<>();
    /**
     * ArrayList that queues links to be indexed.
     */
    private ArrayList<String> linkQueue = new ArrayList<String>();
    /**
     * ArrayList that stores urls by order of relevancy.
     */
    private ArrayList<UrlInfo> urlsByOrder = new ArrayList<>();
    /**
     * ArrayList that stores terms by order of times that they were searched.
     */
    private ArrayList<ArrayList<String>> mostSearched;
    /**
     * ArrayList with terms by order of popularity
     */
    private ArrayList <String> searchesByOrder = new ArrayList<>();
    /**
     * Recursivity counter for indexation, used to limit urls indexed.
     */
    private int recursiveCounter = 0;

    /**
     * server PId.
     */
    public long myId;

    /**
     * Multicast Server constructor that defines server Pid of the respective server.
     */
    public MulticastServer() {
        LocalTime time = java.time.LocalTime.now();

        String stringId = "" + time.getMinute() + "" + time.getSecond() + "" + time.getNano() + "";

        this.myId = Long.parseLong(stringId);
        System.out.println("I'm multicast server " + this.myId + "");
    }

    /**
     * main class of MulticastServer
     * @param args
     */
    public static void main(String[] args) {

        MulticastServer client = new MulticastServer();
        client.start();
    }

    /**
     * method that writes object file with the specified ArrayList of users.
     * @param list ArrayList of users to be stored in file
     * @param filename name of file
     */
    public void writeObj(ArrayList<User> list, String filename){

        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            oos.close();
            fos.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * method that writes object file with the specified Tree map
     * @param list Tree map with information to be stored, it can be either the indexation tree map or the url mentions tree map.
     * @param filename name of file
     */
    public void writeObjTreeMap(Map<String, ArrayList<String>> list, String filename){

        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            oos.close();
            fos.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * method used to store information of each website, such as title and citation.
     * @param list Tree map that stores website info
     * @param filename name of file
     */
    public void writeObjInfo(Map<String, String[]> list, String filename){

        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            oos.close();
            fos.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * method used to read object file with indexation info
     * @param list tree map with the indexation info
     * @param filename name of the file
     */

    public void readObjIndex(Map<String, ArrayList<String>> list, String filename){

        try
        {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);

                if (list != null)

                    index = (Map<String, ArrayList<String>>) ois.readObject();

                ois.close();
                fis.close();
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return;
        }
        catch (ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

    }

    /**
     * method used to read object file with urls and urls that mention it.
     * @param list tree map with urls as keys and urls that mention it as values
     * @param filename name of the file
     */
    public void readObjMentions(Map<String, ArrayList<String>> list, String filename){

        try
        {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);

                if (list != null)

                    urlMentionsList = (Map<String, ArrayList<String>>) ois.readObject();

                ois.close();
                fis.close();
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return;
        }
        catch (ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

    }


    /**
     * method that reads website information, title and citation
     * @param list
     * @param filename
     */
    public void readObjInfo(Map<String, String[]> list, String filename){

        try
        {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);

                if (list != null)

                    wsInfo = (Map<String, String[]>) ois.readObject();

                ois.close();
                fis.close();
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return;
        }
        catch (ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

    }

    /**
     * method that reads object file with user info.
     * @param filename name of the file
     */
    public void readObj(String filename){

        try
        {
            File file = new File(filename);

            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);

                userList = (ArrayList<User>) ois.readObject();

                ois.close();
                fis.close();
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return;
        }
        catch (ClassNotFoundException c)
        {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }

        //Verify list data
        for (User user : userList) {
            System.out.println(user.username);
        }
    }

    /**
     * method that orders searched terms by popularity
     */
    public void getMostSearched() {

        readObj("userdata");

        Map<String, Integer> wordCounter = new TreeMap<>();

        //goes through list of users and gets all searches and stores count in wordCounter tree map, key : website url, value: counter
        for(User user: userList) {
            for(String terms[]: user.searches) {
                for(String term: terms) {
                    if(!wordCounter.containsKey(term)) {
                        wordCounter.put(term, 1);
                        searchesByOrder.add(term);
                    }
                    else
                        wordCounter.put(term, wordCounter.get(term) + 1);
                }
            }
        }

        //BUBBLE SORT FOR SEARCHES
        int n = searchesByOrder.size();

        for (int i = 0; i < n-1; i++) {

            for (int j = 0; j < n - i - 1; j++) {

                if (wordCounter.get(searchesByOrder.get(j)) < wordCounter.get(searchesByOrder.get(j + 1))) {

                    String temp = searchesByOrder.get(j);
                    searchesByOrder.set(j, searchesByOrder.get(j + 1));
                    searchesByOrder.set(j + 1, temp);
                }
            }
        }
    }

    /**
     * web crawler
     */
    public class WebCrawler extends Thread {

        String ws;
        private String originalURL;
        String text;
        int clientId;

        /**
         * Used for callback, when indexation is completed a message is sent with the indexed url and with the client id.
         * @param clientId
         * @param originalURL
         */
        public WebCrawler (int clientId, String originalURL) {
            super();
            this.clientId = clientId;
            this.originalURL = originalURL;
        }

        /**
         * method that builds index tree map and urlMentionsList
         */
        public void run() {

            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

            //indexation ocurres while link queue is not empty and recursive counter is less than 100
            while(!linkQueue.isEmpty() && recursiveCounter < 100) {

                //gets first link in queue
                ws = linkQueue.get(0);
                System.out.println(ws + "-------------------------");
                try {
                    if (! ws.startsWith("http://") && ! ws.startsWith("https://"))
                        ws = "http://".concat(ws);

                    // Attempt to connect and get the document
                    Document doc = Jsoup.connect(ws).get();  // Documentation: https://jsoup.org/

                    // Get all links
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {

                        // Ignore bookmarks within the page
                        if (link.attr("href").startsWith("#")) {
                            continue;
                        }

                        // Shall we ignore local links? Otherwise we have to rebuild them for future parsing
                        if (!link.attr("href").startsWith("http")) {
                            continue;
                        }

                        //key is the link url
                        String key = link.  attr("href");

                        if (! key.startsWith("http://") && ! key.startsWith("https://"))
                            key = "http://".concat(key);

                        //adds to link queue
                        linkQueue.add(key);

                        //adds link to reference ArrayList
                        if (!urlMentionsList.containsKey(key)) {

                            ArrayList<String> lista = new ArrayList<String>();
                            lista.add(ws);
                            urlMentionsList.put(key, lista);

                        } else {

                            ArrayList<String> lista;
                            lista = urlMentionsList.get(key);

                            if (!lista.contains(ws)) {
                                lista.add(ws);
                                urlMentionsList.put(key, lista);
                            }
                        }
                    }

                    //removes first link in queue that was just indexed
                    linkQueue.remove(0);

                    text = doc.text(); // We can use doc.body().text() if we only want to get text from <body></body>

                    //array that stores url info, title in index 0 and citation in index 1
                    String info[] = new String[2];
                    info[0] = doc.title();
                    int length = doc.text().length();
                    info[1] = doc.text().substring(length / 2 - length /20, length / 2 + length / 20);
                    wsInfo.put(ws, info);

                    //recursive counter is incremented
                    recursiveCounter++;
                    System.out.println(recursiveCounter);

                    //puts words and its link in TreeMap
                    WebCrawler indexBuild = new WebCrawler(this.clientId, this.originalURL);
                    indexBuild.indexBuilder(text, ws);

                } catch (IOException e) {
                    System.out.println("BAD LINK");
                    String url = linkQueue.get(0);
                    linkQueue.remove(0);
                }
            }

            try {
                //sends notification to client that the indexation was completed
                MulticastSocket notifyIndexationSocket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                notifyIndexationSocket.joinGroup(group);

                String indexationNotification = "id - " + myId + " ; userId - " + this.clientId + " ; url - " + this.originalURL + " ; type - IndexationComplete";
                byte[] buffer = indexationNotification.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORTout);

                notifyIndexationSocket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //after indexation is done, information is stored in object files
            Map<String, ArrayList<String>> indexCopy = new TreeMap<>();
            Map<String, ArrayList<String>> urlMentionsListCopy = new TreeMap<>();
            Map<String, String[]> wsInfoCopy = new TreeMap<>();

            indexCopy = index;
            urlMentionsListCopy = urlMentionsList;
            wsInfoCopy = wsInfo;

            writeObjTreeMap(indexCopy, "index");
            writeObjTreeMap(urlMentionsListCopy, "references");
            writeObjInfo(wsInfoCopy, "info");

            //stores all url and its counut
            for (String url: urlMentionsList.keySet()) {
                int len = urlMentionsList.get(url).size();
                UrlInfo urlInfo = new UrlInfo(url, len);
                urlsByOrder.add(urlInfo);
            }

            //orders the urls by relevancy
            urlsByOrder.sort(Comparator.comparing(UrlInfo::getCount));

        }

        /**
         * method that builds index tree map
         * @param text text found in html
         * @param website website url
         */
        private void indexBuilder(String text, String website) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
            String line;

            // Get words and respective count
            while (true) {
                try {

                    if ((line = reader.readLine()) == null)
                        break;

                    String[] words = line.split("[ ,;:.?!“”(){}\\[\\]<>']+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        if ("".equals(word)) {
                            continue;
                        }
                        if (!index.containsKey(word)) {
                            ArrayList<String> lista = new ArrayList<String>();
                            lista.add(website);
                            index.put(word, lista);

                        }
                        else {

                            ArrayList<String> lista;
                            lista = index.get(word);

                            if(!lista.contains(website)) {
                                lista.add(website);
                                index.put(word, lista);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Close reader
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * run method for main class that constantly receives messages from RMI client through RMI server, and consequently responds appropriately
     */
    public void run() {
        MulticastSocket socketOut = null, socketIn = null;
        //reads object files and stores all information in tree maps and arraylists
        readObj("userdata");
        readObjIndex(index, "index");
        readObjMentions(urlMentionsList, "references");
        readObjInfo(wsInfo, "info");

        for (String url: urlMentionsList.keySet()) {
            System.out.println(url);
        }

        for (String word: index.keySet()) {
            System.out.println(word);
        }

        try {
            socketOut = new MulticastSocket(PORTout);  // create socket and bind it (socket for sending messages)
            socketIn = new MulticastSocket(PORTin);     // socket for receiving messages
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socketIn.joinGroup(group);
            socketOut.joinGroup(group);
            while (true) {

                byte[] buffer = new byte[64000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketIn.receive(packet);

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);

                //--------------------handling of received command---------------------------------------------------------
                message = message.replaceAll("\\s+", ""); //removes spaces
                String array[] = message.split(";");
                String output = "";

                String type;
                int id = Integer.parseInt(array[0].split("\\|")[1]);
                type = array[1].split("\\|")[1];
                String commands[];

                //if type equals CheckLife server sends message saying that it's alive
                if(type.equals("CheckLife")) {

                    output = "id - " + this.myId + "; type - ImAlive";
                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, PORTout);
                    socketOut.send(packet2);

                }

                //type login verifies username and password and if it's valid
                if(type.equals("login")) {

                    String username = array[2].split("\\|")[1];
                    String password = array[3].split("\\|")[1];

                    for (User user: userList) {
                        if(username.equals(user.username) && password.equals(user.password)){

                            //checks if user making the login is admin or not
                            if (user.admin) {
                                output = "id | " + id + " ; type | status ; logged | on ; userId | " + user.portId + " ; msg | Welcome to ucBusca Admin";

                            }
                            else {
                                output = "id | " + id + " ; type | status ; logged | on ; userId | " + user.portId + " ; msg | Welcome to ucBusca User";

                            }

                            byte[] buffer2 = output.getBytes();
                            DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                            socketOut.send(packet2);
                        }
                    }

                    output = "id | 000 ; type | status ; logged | off ; msg | No user found or password is incorrect";
                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);

                }

                //type for registry for new users
                else if(type.equals("register")) {

                    User user = new User(array[2].split("-")[1], array[3].split("-")[1], id);
                    output = "";
                    int flag = 0;

                    if(userList.isEmpty()) {
                        user.admin = true;
                    }
                    else
                        user.admin = false;

                    //checks if username is taken or not
                    for (User currUser : userList) {
                        if (currUser.username.equals(user.username)) {
                            output = "type | status ; registered | negative ; msg | Failed to register, choose different username";
                            flag = 1;
                        }
                    }

                    if(flag == 0) {
                        //goes through list of users and checks if there is a port that matches the new user, if there is a match, increment new user port by 1
                        for(User currUser : userList){
                            if(currUser.portId == user.portId){
                                user.portId++;
                            }
                        }

                        //adds user to list of users
                        userList.add(user);
                        //writes into file the new list
                        writeObj(userList, "userdata");
                        //output saying that registry was completed
                        output = "type | status ; registered | affirmative ; msg | Registered";
                    }

                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);
                }

                //for url indexation
                else if(type.equals("urlInsert")) {

                    MulticastSocket socket = new MulticastSocket();
                    String ws = array[2].split("\\|")[1];

                    //recursive counter is set to 0
                    recursiveCounter = 0;

                    //link queue is cleared
                    linkQueue.clear();
                    //first element of queue is the insered link
                    linkQueue.add(ws);

                    //thread for indexation is initiated
                    WebCrawler wc = new WebCrawler(id, ws);
                    //runs thread
                    wc.start();

                    output = "type | status ; urlInsertion | successful ; msg | Indexation In Progress";
                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);
                }

                //type termSearch returns 10, or less, websites that mention all terms inserted by user
                else if(type.equals("termSearch")) {

                    String termsArray[] = (array[2].split("\\|"))[1].split(","); //get array with the terms
                    Map<String, ArrayList<String>> containsTerms = new TreeMap<>(); //tree map that will contain key as the terms inserted by user, and value the urls that mention it


                    for (int i = 0; i < termsArray.length ; i++) {
                        termsArray[i] = termsArray[i].toLowerCase();
                    }

                    //get all sites that got the terms
                    for (String word : index.keySet()) {
                        for (String term : termsArray) {
                            if (word.equals(term)) {
                                for (String url : index.get(word)) {
                                    if (!containsTerms.containsKey(url)) {
                                        ArrayList<String> terms = new ArrayList<>();
                                        terms.add(word);
                                        containsTerms.put(url, terms);
                                    } else {
                                        ArrayList<String> terms;
                                        terms = containsTerms.get(url);
                                        terms.add(word);
                                        containsTerms.put(url, terms);
                                    }
                                }
                            }
                        }
                    }

                    ArrayList<String> listUrls = new ArrayList<>();

                    //verify if all terms are in the link, removes the ones that haven't
                    for (String link : containsTerms.keySet()) {
                        if (containsTerms.get(link).size() == termsArray.length) {
                            listUrls.add(link);
                        }
                    }

                    ArrayList<UrlInfo> urlListCounter = new ArrayList<>();
                    UrlInfo obj;
                    //first is created a ArrayList with the counter of times that the website is mentioned in other websites
                    for (String url : listUrls) {
                        if (!urlMentionsList.containsKey(url)) {
                            obj = new UrlInfo(url, 0);
                            urlListCounter.add(obj);
                        } else {
                            obj = new UrlInfo(url, urlMentionsList.get(url).size());
                            urlListCounter.add(obj);
                        }
                    }

                    //sorts found urls by order of relevancy
                    urlListCounter.sort(Comparator.comparing(UrlInfo::getCount));

                    //goes through user list and adds to searches the latest user search, updates it
                    for (User user : userList) {
                        if (user.getPortId() == id) {
                            if (user.getSearches() == null) {
                                ArrayList<String[]> searches = new ArrayList<>();
                                user.searches = searches;
                                user.searches.add(termsArray);
                            } else {
                                user.searches.add(termsArray);
                            }
                        }
                    }

                    //add list of results
                    output = "";
                    String title;
                    String citation;

                    int num = 0;
                    for (int i = urlListCounter.size() - 1; i >= 0; i--) {
                        num++;
                        title = wsInfo.get(urlListCounter.get(i).getUrl())[0];
                        citation = wsInfo.get(urlListCounter.get(i).getUrl())[1];
                        output += title + "\n\t" + urlListCounter.get(i).getUrl() + " [Referenced " + urlListCounter.get(i).getCount() + " times]" + "\n\t- " + citation + "\n\n";
                        if(num == 9) {
                            break;
                        }
                    }

                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);
                    writeObj(userList, "userdata");
                    getMostSearched();
                }

                //by reference return all websites that mention said websites
                else if (type.equals("byReference")) {

                    String ref = (array[2].split("\\|"))[1];
                    output = "";

                    if(urlMentionsList.get(ref) != null) {
                        output = "List of websites that mention : " + ref + " (" + urlMentionsList.get(ref).size() + ")";
                        for (int i = 0; i < urlMentionsList.get(ref).size(); i++) {
                            output += "\n";
                            output += urlMentionsList.get(ref).get(i);
                        }
                    }
                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);

                }

                //returns user searches
                else if (type.equals("userSearches")) {

                    output = "";
                    output += "Search History: \n";
                    int i = 0;

                    for (User user: userList) {
                        if (user.getPortId() == id && user.searches != null) {
                            for (String search[]: user.searches) {
                                output += "\n";
                                for (String term: search) {
                                    output += term;
                                    output += " ";
                                }
                            }
                        }
                    }

                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);
                }

                //grants admin access to user, if current user is admin
                else if (type.equals("adminAccess")) {
                    output = "";
                    int flag = 0;
                    String user = (array[2].split("\\|"))[1];

                    for (User user1: userList) {
                        if(user.equals(user1.username)) {
                            user1.admin = true;
                            flag = 1;
                        }
                    }

                    if(flag == 0)
                        output += "No user found";
                    if(flag == 1)
                        output += "Permission granted";

                    writeObj(userList, "userdata");

                    byte[] buffer2 = output.getBytes();
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, id);
                    socketOut.send(packet2);

                    for (User destUser: userList) {
                        if (destUser.username.equals(user)) {
                            output = "id | " + id + " ; userId | " + destUser.portId + " ; type | AdminAccess";
                        }
                    }

                    buffer = output.getBytes();
                    packet2 = new DatagramPacket(buffer, buffer.length, group, PORTout);
                    socketOut.send(packet2);
                }

                else if (type.equals("adminPage")) {
                    //10 most important pages
                    output = "Most Important Pages: \n";
                    for (UrlInfo info: urlsByOrder) {
                        output += info.getUrl();
                        output += "\n";
                    }

                    //10 most searched terms
                    output += "\nMost Searched Terms (Sorted by popularity):\n";
                    for (String term: searchesByOrder) {
                        output += term + "\n";
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socketIn.close();
            socketOut.close();
        }
    }
}