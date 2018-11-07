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

    public ConnectionThread(Socket sock,String username) {
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
        String line = null;
        while (running) {
            try {
                line = reader.readLine();
                if (line != null)
                    HandleMsg(line);
                else {
                    System.out.println("ConnectionThread " + this.getId() + " entered NULL, killing this thread."); //just to indicate ourselves in console.
                    shutdown();
                }
            } catch (Exception e) {
                System.out.println("Some exception in ConnectionThread: " + this.getId());
                e.printStackTrace();
            }
        }
    }

    // !1  indicates the client want to send a private message.
    // !2  indicates the client want to get all online users.
    // !3  indicates the client wants to disconnect.
    // !4  indicates the client asks to save his name.
    // nothing entered  indicates the client wants to broadcast to everyone.
    private void HandleMsg(String str) {
        if (str.startsWith("!1")) { //private message another client
            System.out.println("Sending request from thread to Server, client asking to privateMessage"); //update: delete this.
            Server.sendPvtMsg(str.substring(2), getId());
        } else if (str.startsWith("!2")) {//to get all users online, type !2
            writer.println("!2" + Server.getUsersOnline(getId()));
        } else if (str.startsWith("!3")) {//client asks to disconnect.
            this.shutdown();
        } else if (str.startsWith("!4")) {//client asks to set his username.
            String username = str.substring(2);
            this.setName(username);
            if(this.getName()=="temp"){ //if we haven't choose a name yet, we still have the name which the Server gave us initially.
                this.setName(username); //we can update our name, once.
            }else{ //if user already has a username.
                writer.println("You already provided a username.");
            }
        } else {
            Server.broadcastMsgs(str, getId());//normal messages send through broadcast.
        }
}

    public void print(String str) {
        writer.println(str);
    }

    public void shutdown() {
        Server.removeConnection(this.getId());
        running = false;
    }
}