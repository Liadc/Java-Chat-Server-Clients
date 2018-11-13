package chat;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 This class represents the Client side of our chat application.
 * a Client object will run as a Thread hence this class will implement Runnable.
 * The client will initiate itself with the ClientGUI, the IP address and port to connect to, and a unique username.
 * The client will initiate a connect to a server on given IP address and PORT. (valid ports 1024-65553), and as soon as a connection is made-
 * The client will initiate a thread which will listen for messages from the server.
 * The client itself will have the option to send messages to the server. (Until disconnection happens or the Disconnect button pressed).
 * The client will have the following functions: run(), requestUsername, requestOnline, sendMsg, closeConnection.
 * The client-server communication will happen between the Client (this) class and the ClientConnection which will manage
 * the input/output between the client/server.
 * The Client will store the following: the Port to connect to, the Host IP address to connect to,
 * the ClientGUI, and the username.
 *
 * @author Liad Cohen, Timor Sharabi.
 */
public class Client implements Runnable {

    /**
     * A constructor for the Client object, will update the port, host IP, GUI, and username.
     * @param host InetAddress, the IP address of the host server to connect to.
     * @param port Integer, the port of the host server to connect to.
     * @param gui ClientGUI, our GUI for the client side.
     * @param username String, our username we to the chat with.
     */
    Client(InetAddress host, int port, ClientGUI gui, String username) {
        this.ip = host;
        this.port = port;
        this.clientGUI = gui;
        this.username = username;
    }

    /**
     * This method will get initiated as soon as we start the Client thread. hence, we override the run() of Runnable.
     * This method will try to connect to the server with the parameters given in constructor.
     * once connected to the host server, it will initiate a new thread to listen for messages from the server.
     * and also sends a request (a string message) to the server asking to set a username.
     * if connection fails, or the server respond with the answer "username already taken" (actual message is "!9")
     * the client will close connection and will have to try and connect again.
     */
    @Override
    public void run() {
        try { //trying to connect
            socket = new Socket(this.ip, this.port);
        } catch (IOException e) { //some error connecting, cannot even establish connection with socket.
            if(clientGUI!=null) { /** if we have a GUI, show message on GUI. otherwise, to system out.*/
                clientGUI.addMsg("Cannot connect to server: connection refused. \nPlease check your input. " +
                        "The server might also be offline." ); //connection refused.
                clientGUI.getConnectBtn().setText("Connect");
            } else { /**No GUI, we print to console. */
                System.out.println("Cannot connect to server: connection refused. \nPlease check your input. " +
                        "The server might also be offline.");
            }
            return; /**kill current thread.*/
        }
        try { /**trying to create i/o streams.*/
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e1) {
            if(clientGUI != null){
            clientGUI.addMsg("Cannot create input/output streams (reader/writer)");}
            else{
                System.out.println("Cannot create input/output streams (reader/writer)");
            }
            return; //kill current thread.
        }
        //we can now listen to the server, on another thread (so we don't block this thread!).
        Runnable listeningToServer = () -> {
            String line;
            while (keepGoing) {
                try {
                    line = reader.readLine();
                    if (line != null) {
                        handleMsg(line);
                    }
                } catch (IOException ioException) { //This means the connection is now closed, probably by the server, but maybe by "Disconnect" button from client.

                    if (clientGUI!=null) {
                        clientGUI.addMsg("You are disconnected.");
                    } else {
                        System.out.println("You are disconnected.");
                    }
                    break;
                    //update: maybe change some GUI buttons to non-clickable if this happens.
                }
            }
        };
        Thread listenServerThread = new Thread(listeningToServer);
        listenServerThread.start();
        //after connection made, listening to server, we can request new username for ourselves.
        requestUsername(this.username);
    }

    /**
     * This method will get a String username (taken from the GUI corresponding username text area) and sends
     * a message to the server "!4USERNAME". the server will notice it starts with '!4', and will know the user is
     * asking to set his name.
     * This method will be called once connection is made, and only once.
     * @param username String, the username we are asking the server to set for us.
     */
    private void requestUsername(String username) {
        sendMsg("!4"+username);
    }

    //a message "!2" indicates a request for all online users.

    /**
     * This method will send a message "!2" to the server. the server will notice it starts with '!2', and will
     * know the user is asking to get all currently online users.
     * This method will be called once 'refresh' button is pressed in GUI.
     */
    void requestOnline() {
        sendMsg("!2");
    }

    /**
     * This method will get a String message and sends this string to the server through our 'writer'
     * Which is our PrintWriter, the OutputStream to the server.
     * @param msg String, the message to send to the server.
     */
    void sendMsg(String msg) { // does not update GUI, because it sends to the server. the server will update all GUIs accordingly.
        writer.println(msg);
    }

    /**
     * This method will close the inputStream and outputStream and then the socket itself.
     * it will also update the GUI "disconnect" button to "Connect".
     */
    void closeConnection() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            if(clientGUI!=null)
                clientGUI.addMsg("Error with closing current outputStream -> writer");
            else
                System.out.println("Error with closing current outputStream -> writer");
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            if (clientGUI!=null) {
                clientGUI.addMsg("Error with closing current inputStream -> reader");
            } else {
                System.out.println("Error with closing current inputStream -> reader");
            }
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            if (clientGUI!=null) {
                clientGUI.addMsg("Error with closing socket!");
            } else {
                System.out.println("Error with closing socket!");
            }
        }
        if(clientGUI!=null) /** updates GUI 'disconnect' button to 'Connect' because we are disconnected now. */
            clientGUI.getConnectBtn().setText("Connect");
    }

    /******* Private *******/
    private String username;
    private boolean keepGoing = true;
    private ClientGUI clientGUI;
    private int port;
    private InetAddress ip;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * This method will handle the messages received from the server.
     * This method will be called from another Thread which listens to the server messages.
     * This method will notice what the server meant (is it an event or just a message to show on chat, etc).
     * The logic is as follows; the server sent:
     * starts with '!2' indicates the server sends us the online users, in the form of !2name1,name2,name3, etc.
     * starts with '!3' indicates the server is telling us he is shutting down. (so we can close all buffers and threads properly)
     * starts with '!9' indicates the server is telling us to pick a different username.
     * starts with Non-Of-The-Above, the server sending us a regular message to show on Chat on ClientGUI.
     *
     * @param line String, represents message from the server, with certain logic:
     * starts with '!2' indicates the server sends us the online users, in the form of !2name1,name2,name3, etc.
     * starts with '!3' indicates the server is telling us he is shutting down. (so we can close all buffers and threads properly)
     * starts with '!9' indicates the server is telling us to pick a different username.
     * starts with Non-Of-The-Above, the server sending us a regular message to show on Chat on ClientGUI.
     */
    private void handleMsg(String line) {
        if (line.startsWith("!2")) { //all online users
                line = line.substring(2);
                String[] onlines = line.split(",");
                DefaultListModel model = new DefaultListModel();
                model.addAll(Arrays.asList(onlines));
                clientGUI.setListModel(model);
        } else if (line.startsWith("!3")) {//server telling us he is shutting down.
            closeConnection();
            keepGoing = false;
            clientGUI.addMsg("Server is shutting down, you are disconnected.");
        }else if(line.startsWith("!9")){ //server telling us to pick different username.
            closeConnection();
            clientGUI.addMsg(line.substring(2));
        }else{
            clientGUI.addMsg(line);
        }
    }


}
