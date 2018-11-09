package chat;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Client implements Runnable {

    Client(InetAddress host, int port, ClientGUI gui, String username) {
        this.ip = host;
        this.port = port;
        this.clientGUI = gui;
        this.username = username;
    }


    @Override
    public void run() {
        try { //trying to connect
            socket = new Socket(this.ip, this.port);
        } catch (IOException e) { //some error connecting, cannot even establish connection with socket.
            clientGUI.addMsg("Cannot connect to server: connection refused. \nPlease check your input. The server might also be offline." ); //connection refused.
            clientGUI.getConnectBtn().setText("Connect");
            return; //kill current thread.
        }
        try { //trying to create i/o streams.
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e1) {
            clientGUI.addMsg("Cannot create input/output streams (reader/writer)");
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
                    clientGUI.addMsg("You are disconnected.");
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

    //sends message to Server, asking to update his username.
    private void requestUsername(String username) {
        sendMsg("!4"+username);
    }

    //a message "!2" indicates a request for all online users.
    void requestOnline() {
        sendMsg("!2");
    }

    void sendMsg(String msg) { // does not update GUI, because it sends to the server. the server will update all GUIs accordingly.
        writer.println(msg);
    }

    void closeConnection() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            clientGUI.addMsg("Error with closing current outputStream -> writer");
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            clientGUI.addMsg("Error with closing current inputStream -> reader");
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            clientGUI.addMsg("Error with closing socket!");
        }
        clientGUI.getConnectBtn().setText("Connect");
    }

    //Private

    private String username;
    private boolean keepGoing = true;
    private ClientGUI clientGUI;
    private int port;
    private InetAddress ip;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     *
     * @param line - represents string by the form:
     *             if starts with !2 so its in the form !2ID1,ID2,ID3,ID4 where IDi where i is ID.
     *             else if PRIVETMSG - start with !1 so its in the form !1ID:MSG
     *             else,
     *             send the msg to the ClientGUI
     *
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
