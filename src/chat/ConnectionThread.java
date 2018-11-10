package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionThread extends Thread {
    private Socket mySocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean running = true;

    ConnectionThread(Socket sock, String username) {
        this.mySocket = sock;
        try {
            this.reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.writer = new PrintWriter(sock.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setName(username); //updates name of thread to be the username.
    }

    @Override
    public void run() {
        System.out.println("Starting thread...");
        String line;
        while (running) {
            try {
                line = reader.readLine();
                if (line != null)
                    HandleMsg(line);
                else {
                    System.out.println("ConnectionThread " + this.getId() + "(username: "+this.getName()+ ") entered NULL, killing this thread."); //update: just to indicate ourselves in console. delete this
                    shutdown(false);
                }
            } catch (Exception e) {
                System.out.println("Some exception in ConnectionThread: " + this.getId() +" . On username: "+this.getName());
                e.printStackTrace();
            }
        }
    }

    // !1  indicates the client want to send a private message.
    // !2  indicates the client want to get all online users.
    // !3  indicates the client wants to disconnect.
    // !4  indicates the client asks to save his name.
    // !5  indicates the clients wants to broadcast.
    // nothing entered  indicates the client wants to broadcast to everyone.
    private void HandleMsg(String str) {
        if (str.startsWith("!1")) { //private message another client
            System.out.println("Sending request from thread to Server, client asking to privateMessage"); //update: delete this.
            Server.sendPvtMsg(str.substring(2), getName());
        } else if (str.startsWith("!2")) {//to get all users online, type !2
            writer.println("!2" + Server.getUsersOnline());
        } else if (str.startsWith("!3")) {//client asks to disconnect.
            this.shutdown(false);
        } else if (str.startsWith("!4")) {//client asks to set his username.
            String username = str.substring(2);
            if(this.getName().equals("temp")){ //if we haven't choose a name yet, we still have the name which the Server gave us initially.
                boolean sameName = false;
                for(ConnectionThread ct : Server.getConnections()){
                    if(ct.getName().equals(username)){
                        print("!9Server: username already in use. try a different name."); //!9 indicates to pick different username.
                        System.out.println("Found some1 with the same name, disconnecting this client connection attempt.");
                        sameName = true;
                        shutdown(true); //no broadcasting for disconnecting this user.
                        break;
                    }
                }
                this.setName(username); //we can update our name, once.
                //we can now broadcast connection.
                if(!sameName)
                Server.broadcastServEvents(this.getName() + " has connected.");

            }else{ //if user already has a username.
                System.out.println("Already provided username!");
                writer.println("You already provided a username.");
            }
        } else if (str.startsWith("!5")){
            Server.broadcastMsgs(str.substring(2), getName());//normal messages send through broadcast.
        }
    }

    void print(String str) {
        writer.println(str);
    }

    private void shutdown(boolean silentShutdown) {
        running = false;
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            System.out.println("Error with closing current outputStream -> writer");
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            System.out.println("Error with closing current inputStream -> reader");
        }
        try {
            if (mySocket != null) {
                mySocket.close();
            }
        } catch (Exception e) {
            System.out.println("Error with closing socket!");
        }
        if(silentShutdown){
            Server.silentRemoveConnection(this.getId());
        }
        else{
            Server.removeConnection(this.getName());
        }
    }
}