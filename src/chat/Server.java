package chat;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * This class represents the Server side of our chat application.
 * a Server object will run as a Thread hence this class will implement Runnable.
 * The server is the "Brain" of our application. The server will initiate itself with the ServerGUI (or without it)
 * The server will listen for any connection on the port given (valid ports 1024-65553), and as soon as a connection is made-
 * The server will initiate a ConnectionThread Thread which will handle all communication between the user connected and the server.
 * Then the server will keep listening for new clients. (Until disconnection happens or the Stop Server button pressed).
 * The server will have the following functions: broadcastServEvents, broadcastMsgs, sendPvtMsg, removeConnection, silentRemoveConnection,
 * getUsersOnline, stopServer, run (Override run of Runnable). Since the server will always listen for new connections,
 * these functions will actually be called from a ConnectionThread thread, hence some of these functions will be Synchronized.
 * The Server will store the following: its Port, its ServerGUI (so it can update UI elements), ArrayList<ConnectionThread> connections (to manage all currently online users)
 * and a boolean keepGoing which indicates to terminate the thread or not.
 *
 * @author Liad Cohen, Timor Sharabi
 */
public class Server implements Runnable {

    /**
     * Constructor method for the Server.
     * This constructor is used mainly with the CMD given only a port. (no GUI).
     * This constructor is used to test SERVER logic and functions, including Server communication to Client through TELNET inside CMD.
     * And used for the JUnit tests.
     * @param port Integer, between 1024 to 65553.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Constructor method for the Server.
     * Will get initiated from a GUI using a button.
     * This constructor initiate the Server with a given port number
     * @param port Integer, between 1024-65553 (check for validation doesn't happen in the constructor).
     * @param anyGUI ServerGUI, will get initiated from a ServerGUI button and will pass itself (the gui) to the constructor.
     */
    public Server(int port, ServerGUI anyGUI) { //Providing a GUI so the server can update some UI elements in another thread.
        this.port = port;
        serverGUI = anyGUI;
    }

    /**
     * startServer represents an attempt to start listening for clients on specific port given.
     * as long as the attempt succeeded, the Server will keep listening for new clients.
     * as soon as a client connected, the Server will "move" the connection to a new thread (ConnectionThread) to handle all communication with this client.
     * This way, the server can keep listening for new clients.
     * if a failure happens (invalid port, used port, stopServerButton pressed, etc.) it will throw specific exceptions, notify and update the GUI.
     */
    private void startServer() {
        this.keepGoing = true;
        ServerSocket server = null;

        try {
            server = new ServerSocket(port);
        } catch (BindException bException) { /**port is binded and already in use.*/
            if (serverGUI!=null)
                serverGUI.addToEvents("Recently used this port, try a different port!");
            stopServer();
        } catch (IOException e) {
            e.printStackTrace();
            stopServer();
        }

        while (keepGoing) {
            try {
                if(serverGUI!=null)
                    serverGUI.addToEvents("Waiting for connections on port: " + this.port + "....");
                Socket connection = server.accept(); //Thread stays here, waiting for connections.
                ConnectionThread ct = new ConnectionThread(connection,"temp"); //username will be changed once InputStream object is initiated and gets the request from client.
                connections.add(ct);
                ct.start();
                if (serverGUI!=null)
                    serverGUI.addToEvents("Connection made with new client, initiating new thread for this connection, we can keep listening...");
                while (ct.getName().equals("temp") && ct.isAlive()) {
//                    System.out.println("New user connected--Still waiting for a request to update his name!");
                    //do nothing, waiting for update. input stream is building up on another thread, let's wait for username update.
                }
                System.out.println("Updated new username now");
                //username is now updated and sync in both server , connectionThread, and client.
                if (serverGUI!=null)
                    serverGUI.addToEvents(ct.getName() + " has connected");
            } catch (IOException e) {
                System.out.println("Error with IO");
                e.printStackTrace();
            } catch (NullPointerException nullPointerException) {
                if (serverGUI!=null) {
                    serverGUI.addToEvents("Recently used port, try a different port.");
                } else {
                    //all ok
                }
                stopServer();
            }
        }
    }

    /**
     * This method will iterate through all online clients and send them an event(string) from the server.
     * Our application works in two simultaneous directions: server Events and Messages. Events including: someone connected/disconnected,
     * someone wants to private message, someone requested to get all online users, etc. Most of clients requests are considered events.
     * We want to broadcast everyone about some of these event, for example: Client X connected.  we will use this method.
     *
     * This method might be called simultaneously from many different ConnectionThreads, and might get out of sync. thus - this method will be synchronized.
     * @param msg String, the event to send to everybody.
     */
    synchronized static void broadcastServEvents(String msg) {
        System.out.println("event occured, broadcasting this event: " + msg);
        try {
            if (connections != null && connections.size() != 0) {
                for (ConnectionThread ct : connections) { //send to every client (to every connection thread).
                    if (msg.equals("!3")) { //client asked to disconnect.
                        ct.print(msg);
                    } else ct.print("Server System says: " + msg);
                }
            }
        }catch (Exception e){}//nothing we can do.
    }

    /**
     * This method will iterate through all online clients and send them a message (string) from the server.
     * Our application works in two simultaneous directions: server Events and Messages. Messages including:
     * a client requested to send everyone a message in the chat (regular message). we will use this method.
     * This method might be called simultaneously from many different ConnectionThreads, and might get out of sync. thus - this method will be synchronized.
     * @param msg String, the message to send to everybody.
     * @param fromThreadName String, the name of the thread(which equals the name of the client) who wants to send the message.
     *                       This will be used to manipulate the actual message with the name of the sender.
     */
    synchronized static void broadcastMsgs(String msg, String fromThreadName) {
        String msgSent = "Username " + fromThreadName + " broadcasted: " + msg; //update threadID to username.
        if (serverGUI!=null)
            serverGUI.addToMsgs(msgSent);
        System.out.println(msgSent);
        for (ConnectionThread ct : connections) { //send to every client (to every connection thread).
            ct.print("Username " + fromThreadName + " says: " + msg);
        }
    }

    /**
     * This method will get a String (username who wants to send the message) and a string representing the message of the form: toUsername:bla
     * the method will substring the message into two parts revealing who we need to send the message to.
     * Then, the method will iterate through all currently online users and search for this username. once it found, it will send the second part of the message
     * with the name of the sender to the corresponding client. otherwise, it will send the sender that this username is not found.
     *
     * This method might be called simultaneously from many different ConnectionThreads, and might get out of sync. thus - this method will be synchronized.
     * @param msg String, the message of the form:  toUsername:Message
     * @param fromThreadUsername String, the name of the sender.
     */
    synchronized static void sendPvtMsg(String msg, String fromThreadUsername) {
        boolean foundTargetUser = false; //indicate if we found target user.
        String msgTo = msg.substring(0, msg.indexOf(':'));
        System.out.println("msgTo now equals: " + msgTo);
        for (ConnectionThread ct : connections) {
            if (ct.getName().equals(msgTo)) { //found the userName.
                System.out.println("Found username.!!");
                String pureMsg = msg.substring(msg.indexOf(':') + 1); //pure message is the text data in the message.
                ct.print("From " + fromThreadUsername + ": " + pureMsg);
                if (serverGUI!=null)
                    serverGUI.addToMsgs("User: " +fromThreadUsername +" sent a private message to "+ msgTo+": "+pureMsg); //just so the server knows about this.
                foundTargetUser = true;
                break; //end searching for userID.
            }
        }
        if(!foundTargetUser){ //target user cannot be found, lets notify sender.
            for(ConnectionThread ct : connections){
                if(ct.getName().equals(fromThreadUsername)){
                    ct.print("User: "+msgTo+" cannot be found on the server. He is already offline or you have a typo in his username.");
                }
            }
        }
    }

    /**
     * This method will get a String representing the username of the client who asked to disconnect,
     * and removes it from the connections ArrayList, as well as notifying every other client about this disconnection,
     * as well as updating the ServerGUI events area with this disconnection.
     *
     * This method might be called simultaneously from many different ConnectionThreads, and might get out of sync. thus - this method will be synchronized.
     * @param userName String, represents the username of the client who asked to disconnect.
     */
    synchronized static void removeConnection(String userName) {
        if (serverGUI!=null)
            serverGUI.addToEvents(userName + " asked to disconnect.");
        if(connections != null) {
            for (ConnectionThread ct : connections) {
                if (ct.getName().equals(userName)) {
                    broadcastServEvents(ct.getName() + " has disconnected.");
                    connections.remove(ct);
                    if (serverGUI!=null)
                        serverGUI.addToEvents(userName + " has disconnected.");
                    break;
                }
            }
        }
    }

    /**
     * This method will get a Long threadID representing the ID of the THREAD (the ConnectionThread) for a client who is going to get disconnected,
     * and removes it from the connections ArrayList, but WILL NOT notify any client about this disconnection.
     * This method is used when a user tries to pick a username which is already taken for example. This way,
     * we disconnect the client because of the same username, (we have unique-username policy)
     * but we won't notify anyone about the connection or the disconnection of that client.
     *
     * This method might be called simultaneously from many different ConnectionThreads, and might get out of sync. thus - this method will be synchronized.
     * @param threadID Long, representing the ID of the Thread called the function. (a ConnectionThread representing the client).
     */
    synchronized static void silentRemoveConnection(long threadID) {
        if(serverGUI!=null)
            serverGUI.addToEvents(threadID + " will disconnect silently, no broadcasting.");
        if(connections != null) {
            for (ConnectionThread ct : connections) {
                if (ct.getId() == threadID) {
                    connections.remove(ct);
                    if (serverGUI!=null)
                        serverGUI.addToEvents("ThreadID: " + threadID + " has disconnected silently. No broadcasting.");
                    break;
                }
            }
        }
    }

    /**
     * This method will be going through all connection threads, and adding their names to a string.
     * This method will be called when "show online" (refresh) button is pressed. eventually, will return that string.
     *
     * This method might be called simultaneously from many different ConnectionThreads, and might get out of sync. thus - this method will be synchronized.
     * @return String, contains all users separated by comma.
     */
    synchronized static String getUsersOnline() {
        StringBuilder allUsers = new StringBuilder();
        for (ConnectionThread ct : connections) {
            allUsers.append(ct.getName()).append(",");
        }
        if (allUsers.length() > 0)
            allUsers = new StringBuilder(allUsers.substring(0, allUsers.length() - 1));
        return allUsers.toString();
    }

    /**
     * This method will be called to shutdown the server. it will also try to notify all users about this shutdown.
     * we broadcast the message "!3" to indicate the clients that the server is telling them about a shutdown.
     * we also stop listening to new connections and shutting down this thread by changing the keepGoing variable to FALSE.
     */
    void stopServer() {
        try {
            broadcastServEvents("!3"); //telling all clients we are shutting down.
        }catch (Exception e){} //nothing we can do. actually can't send because we stopped server maybe before even connected with clients.
        this.keepGoing = false; //shutdown this thread.
        connections=null;
        if (serverGUI != null) {
            serverGUI.toggleStartStopBtn(true); //update GUI start/stop button text.
        }
    }

    /**
     * This method will be called as soon as this Thread is created. we override the run() method of Runnable so we can manage the server properly.
     */
    @Override
    public void run() {
        this.keepGoing = true;
        this.startServer();
    }

    /**
     * a Getter method for the ArrayList of ConnectionThread, which contains all currently threads handling the client-server communication.
     * @return ArrayList of type ConnectionThread, contains all currently alive threads handling the client-server communication.
     */
    static ArrayList<ConnectionThread> getConnections() {
        return connections;
    }

    /******** Private *********/
    private static ArrayList<ConnectionThread> connections = new ArrayList<>(); //all our connections with clients, saved in arraylist.
    private int port; //our port.
    private boolean keepGoing = true; //boolean to indicate keep listening for new clients, or terminate and close this thread.
    private static ServerGUI serverGUI = null; //a GUI (on another thread) so this server can update some UI elements.
}
