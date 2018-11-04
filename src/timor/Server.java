package timor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{
    private static ArrayList<ConnectionThread> connections = new ArrayList<>();
    private int port;

    Server(int port){
        this.port = port;
    }
    public void startServer(){
        ServerSocket server = null;

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean keepGoing = true;
        while(keepGoing){
            try {
                System.out.println("Waiting for connections on port: "+port+ "....");
                Socket connection = server.accept();
                ConnectionThread ct = new ConnectionThread(connection);
//                Thread t = new Thread(ct);
                connections.add(ct);
                ct.start();

            } catch (IOException e) {
                e.printStackTrace();
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

    @Override
    public void run() {
        this.startServer();
    }
}
