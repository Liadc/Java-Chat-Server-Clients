package chat;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
 */
public class Server implements Runnable {

    /**
     * Constructor method for the Server.
     * This constructor is used mainly with the CMD given only a port. (no GUI).
     * This constructor is used to test SERVER logic and functions, including Server communication to Client through TELNET inside CMD.
     * @param port Integer, between 1024 to 65553.
     */
    Server(int port) {
        this.port = port;
    }

    /**
     * Constructor method for the Server.
     * Will get initiated from a GUI using a button.
     * This constructor initiate the Server with a given port number
     * @param port Integer, between 1024-65553 (check for validation doesn't happen in the constructor).
     * @param anyGUI ServerGUI, will get initiated from a ServerGUI button and will pass itself (the gui) to the constructor.
     */
    Server(int port, ServerGUI anyGUI) { //Providing a GUI so the server can update some UI elements in another thread.
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
        } catch (BindException bException) { //port is binded and already in use.
            serverGUI.addToEvents("Recently used this port, try a different port!");
            stopServer();
        } catch (IOException e) {
            e.printStackTrace();
            stopServer();
        }

        while (keepGoing) {
            try {
                serverGUI.addToEvents("Waiting for connections on port: " + this.port + "....");
                Socket connection = server.accept(); //Thread stays here, waiting for connections.
                ConnectionThread ct = new ConnectionThread(connection,"temp"); //username will be changed once InputStream object is initiated and gets the request from client.
                connections.add(ct);
                ct.start();
                serverGUI.addToEvents("Connection made with new client, initiating new thread for this connection, we can keep listening...");
                while (ct.getName().equals("temp") && ct.isAlive()) {
                    System.out.println("Still temp");
                    //do nothing, waiting for update. input stream is building up on another thread, let's wait for username update.
                }
                //username is now updated and sync in both server , connectionThread, and client.
                serverGUI.addToEvents(ct.getName() + " has connected");

            } catch (IOException e) {
                System.out.println("Error with IO");
                e.printStackTrace();
            } catch (NullPointerException nullPointerException) {
                serverGUI.addToEvents("Recently used port, try a different port.");
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

    synchronized static void broadcastMsgs(String msg, String fromThreadName) {
        String msgSent = "Username " + fromThreadName + " broadcasted: " + msg; //update threadID to username.
        serverGUI.addToMsgs(msgSent);
        System.out.println(msgSent);
        for (ConnectionThread ct : connections) { //send to every client (to every connection thread).
            ct.print("Username " + fromThreadName + " says: " + msg);
        }
    }

    //to private message between clients.
    synchronized static void sendPvtMsg(String msg, String fromThreadUsername) { //update: optimize this, changes needed
        boolean foundTargetUser = false; //indicate if we found target user.
        String msgTo = msg.substring(0, msg.indexOf(':'));
        System.out.println("msgTo now equals: " + msgTo);
        for (ConnectionThread ct : connections) {
            if (ct.getName().equals(msgTo)) { //found the userID. changed to userName.
                System.out.println("Found username.!!");
                String pureMsg = msg.substring(msg.indexOf(':') + 1); //pure message is the text data in the message.
                ct.print("From " + fromThreadUsername + ": " + pureMsg);
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

    synchronized static void removeConnection(String userName) {
        serverGUI.addToEvents(userName + " asked to disconnect.");
        if(connections != null) {
            for (ConnectionThread ct : connections) {
                if (ct.getName().equals(userName)) {
                    broadcastServEvents(ct.getName() + " has disconnected.");
                    connections.remove(ct);
                    serverGUI.addToEvents(userName + " has disconnected.");
                    break;
                }
            }
        }
    }

    synchronized static void silentRemoveConnection(long threadID) {
        serverGUI.addToEvents(threadID + " will disconnect silently, no broadcasting.");
        if(connections != null) {
            for (ConnectionThread ct : connections) {
                if (ct.getId() == threadID) {
                    connections.remove(ct);
                    serverGUI.addToEvents("ThreadID: " + threadID + " has disconnected silently. No broadcasting.");
                    break;
                }
            }
        }
    }

    /**
     * Going through all connection threads, and adding their names.
     * This method will be called when "show online" button is pressed.
     * @return String, contains all users.
     */
    static String getUsersOnline() {
        StringBuilder allUsers = new StringBuilder();
        for (ConnectionThread ct : connections) {
            allUsers.append(ct.getName()).append(",");
        }
        if (allUsers.length() > 0)
            allUsers = new StringBuilder(allUsers.substring(0, allUsers.length() - 1));
        return allUsers.toString();
    }

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

    @Override
    public void run() {
        this.keepGoing = true;
        this.startServer();
    }

    static ArrayList<ConnectionThread> getConnections() {
        return connections;
    }

    /******** Private *********/
    private static ArrayList<ConnectionThread> connections = new ArrayList<>();
    private int port;
    private boolean keepGoing = true;
    private static ServerGUI serverGUI; //a GUI (on another thread) so this server can update some UI elements.
}
