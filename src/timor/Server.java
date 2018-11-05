package timor;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{
    private static ArrayList<ConnectionThread> connections = new ArrayList<>();
    private int port;
    private boolean keepGoing = true;
    private ServerGUI serverGUI; //a GUI (on another thread) so the server can update some UI elements.

    Server(int port){
        this.port = port;
    } //constructor for Server. doesn't use GUI.
    // This is mainly used to complete the functions needed using CMD client<->server communication using TELNET without any GUI developed.

    Server(int port, ServerGUI anyGUI){ //Providing a GUI so the server can update some UI elements in another thread.
        this.port = port;
        this.serverGUI = anyGUI;
    }

    public void startServer(){
        this.keepGoing = true;
        ServerSocket server = null;

        try {
            server = new ServerSocket(port);
        } catch (BindException bException){
            serverGUI.addToEvents("Recently used port, try a different port!");
            stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
      
        while(keepGoing){
            try {
                serverGUI.addToEvents("Waiting for connections on port: "+this.port+ "....");
                Socket connection = server.accept();
                ConnectionThread ct = new ConnectionThread(connection);
                connections.add(ct);
                ct.start();
//                serverGUI.addToEvents("Server started on new Thread, listening on port " + this.port + "..."); //update: actual message has to be
// something like "connection made with client, initiating new thread for the server, to keep listening..."

            } catch (IOException e) {
                System.out.println("Error with IO");
                e.printStackTrace();
            } catch (NullPointerException nullPointerException){
                serverGUI.addToEvents("Recently used port, try a different port.");
                stopServer();
            }
        }
    }//listening method


    synchronized static void Broadcast(String msg, long threadID){

        System.out.println("ThreadID " + threadID +" Broadcasted: " + msg);
        for(ConnectionThread ct : connections){
           ct.Print("ThreadID " + threadID +" says: " + msg);
       }
    }

    synchronized static void sendMsg(String msg, long threadID) {
        String msgTo = msg.substring(0,msg.indexOf(':'));
        long msgToID = Long.parseLong(msgTo);
        System.out.println("msgTo now equals: "+ msgTo);
        for(ConnectionThread ct : connections){
            if(ct.getId() == msgToID){
                System.out.println("Entered if");
                ct.Print("From " + threadID+": " + msg.substring(msg.indexOf(':')+1));
            }
            //continue
        }
    }

    synchronized static void removeConnection(long threadID){
        for(ConnectionThread ct : connections){
            if(ct.getId() == threadID){
                connections.remove(ct);
                break;
            }
        }
    }

    static String getUsersOnline(long threadID) {
        String allUsers = "";
//        Iterator<ConnectionThread> it = connections.iterator();
//        while(it.hasNext()){
//            all
//        }
        for (ConnectionThread ct : connections){
            allUsers += ct.getId() + ", ";
        }
        return allUsers;
    }

     protected void stopServer(){
        connections = null; //Server is shutting down.
        serverGUI.toggleStartStopBtn(true); //update GUI start button text.
        this.keepGoing = false;
    }

    @Override
    public void run() {
        this.keepGoing = true;
        this.startServer();
    }
}
