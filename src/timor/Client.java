package timor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable {

    public Client(InetAddress host, int port, ClientGUI gui) {
        this.ip = host;
        this.port = port;
        this.clientGUI = gui;
    }


    @Override
    public void run() {
        try { //trying to connect
            socket = new Socket(this.ip, this.port);
        } catch (IOException e) { //some error connecting, cannot even establish connection with socket.
            clientGUI.addMsg("Cannot connect to server: "+e);
            return;
        }
        try{ //trying to create i/o streams.
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }catch (IOException e1){
            clientGUI.addMsg("Cannot create input/output streams (reader/writer)");
            return;
        }
        //we can now listen to the server, on another thread (so we don't block this thread!).
        //**** ListenFromServer-Thread-HERE!!!!
        Runnable listeningToServer = () ->{
            String line;
            while (true){
                try{
                     line = reader.readLine();
                     if(line!=null){
                         clientGUI.addMsg(line);
                     }
                }catch (IOException ioException){ //This means the connection is now closed, probably by the server.
                    clientGUI.addMsg("You are disconnected. Server closed the connection.");
                    //update: maybe change some GUI buttons to non-clickable if this happens.
                }
            }
        };
        Thread listenServerThread = new Thread(listeningToServer);
        listenServerThread.start();

    }

    void sendMsg(String msg) {
        writer.println(msg);
    }

    void closeConnection() { //update
        try{
            if(writer != null){ writer.close(); }
        }catch (Exception e){
            clientGUI.addMsg("Error with closing current outputStream -> writer");
        }
        try {
            if (reader != null) { reader.close(); }
        }catch(Exception e){
            clientGUI.addMsg("Error with closing current inputStream -> reader");
        }
        try{
            if(socket != null){ socket.close(); }
        }catch (Exception e){
            clientGUI.addMsg("Error with closing socket!");
        }

    }

    //Private Methods

    private void handleMsg(String msg) {
        clientGUI.addMsg(msg);
    }

    private boolean keepGoing = true;
    private ClientGUI clientGUI;
    private int port;
    private InetAddress ip;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;


}
