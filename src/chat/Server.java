package chat;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {

    Server(int port) {
        this.port = port;
    } //constructor for Server. doesn't use GUI.
    // This is mainly used to complete the functions needed using CMD client<->server communication using TELNET without any GUI developed.

    Server(int port, ServerGUI anyGUI) { //Providing a GUI so the server can update some UI elements in another thread.
        this.port = port;
        serverGUI = anyGUI;
    }

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
                Socket connection = server.accept();
                ConnectionThread ct = new ConnectionThread(connection);
                connections.add(ct);
                ct.start();
//                serverGUI.addToEvents("Server started on new Thread, listening on port " + this.port + "..."); //update: actual message has to be
// something like "connection made with client, initiating new thread for the server, to keep listening..."
                serverGUI.addToEvents(ct.getName() + " has connected");
                broadcastServEvents(ct.getName() + " has connected.");

            } catch (IOException e) {
                System.out.println("Error with IO");
                e.printStackTrace();
            } catch (NullPointerException nullPointerException) {
                serverGUI.addToEvents("Recently used port, try a different port.");
                stopServer();
            }
        }
    }//listening method

    /**
     * This method will iterate through all online clients and send them an event(string) from the server.
     *
     * @param msg the event to send.
     */
    synchronized static void broadcastServEvents(String msg) {
        System.out.println("event occured, broadcasting this event: " + msg);
        if(connections!=null || connections.size()!=0) {
            for (ConnectionThread ct : connections) { //send to every client (to every connection thread).
                ct.print("Server System says: " + msg);
            }
        }
    }

    //return true if succeeded.
    synchronized static boolean setUsername(String username, long fromThreadID){
        for(ConnectionThread ct : connections){
            if(fromThreadID == ct.getId()){
                ct.setName(username);
                return true;
            }
        }
        return false;
    }

    synchronized static void broadcastMsgs(String msg, long threadID) {
        String msgSent = "ThreadID " + threadID + " Broadcasted: " + msg; //update threadID to username.
        serverGUI.addToMsgs(msgSent);
        System.out.println(msgSent);
        //serverGUI.addToMsgs("ThreadID "+threadID+" broadcasted: "+msg); //update threadID to username.
        for (ConnectionThread ct : connections) { //send to every client (to every connection thread).
            ct.print("ThreadID " + threadID + " says: " + msg);
        }
    }

    //to private message between clients.
    synchronized static void sendPvtMsg(String msg, long fromThreadID) { //update: optimize this, changes needed
        boolean foundTargetUser = false; //indicate if we found target user.
        String msgTo = msg.substring(0, msg.indexOf(':'));
        long msgToID = Long.parseLong(msgTo); //update: this should be the username, stays string. no casting.
        for (ConnectionThread ct : connections) {
            if (ct.getId() == msgToID) { //found the userID.
                String pureMsg = msg.substring(msg.indexOf(':') + 1); //pure message is the text data in the message.
                ct.print("From " + fromThreadID + ": " + pureMsg);
                serverGUI.addToMsgs("User: " +fromThreadID +" sent a private message to "+ msgToID+": "+pureMsg); //just so the server knows about this.
                foundTargetUser = true;
                break; //end searching for userID.
            }
        }
        if(!foundTargetUser){ //target user cannot be found, lets notify sender.
            for(ConnectionThread ct : connections){
                if(ct.getId() == fromThreadID){
                    ct.print("User: "+msgToID+" cannot be found on the server. He is already offline or you have a typo in his username.");
                }
            }
        }
    }

    synchronized static void removeConnection(long threadID) {
        serverGUI.addToEvents(threadID + " asked to disconnect.");
        for (ConnectionThread ct : connections) {
            if (ct.getId() == threadID) {
                broadcastServEvents(ct.getId() + " has disconnected.");
                connections.remove(ct);
                serverGUI.addToEvents(threadID + " has disconnected.");
                break;
            }
        }
    }

    /**
     * Going through all connection threads, and adding their names.
     * This method will be called when "show online" button is pressed.
     *
     * @param threadID the threadID who called the function. (doesn't use it)
     * @return String, contains all users.
     */
    static String getUsersOnline(long threadID) {
        String allUsers = "";
//        Iterator<ConnectionThread> it = connections.iterator();
//        while(it.hasNext()){
//            all
//        }
        for (ConnectionThread ct : connections) {
            allUsers += ct.getId() + ",";
        }
        if (allUsers.length() > 0)
            allUsers = allUsers.substring(0, allUsers.length() - 1);
        return allUsers;
    }

    protected void stopServer() {
        connections = null; //Server is shutting down.
        if(serverGUI!=null){
            serverGUI.toggleStartStopBtn(true); //update GUI start/stop button text.
        }
        this.keepGoing = false; //shutdown this thread.
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
