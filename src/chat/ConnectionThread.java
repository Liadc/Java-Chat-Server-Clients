package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class represents a Thread on the Server-side which will handle all communications with a specific client.
 * All input from a client and output from the server to a client will be handled on this thread.
 * It will store the following values: the Socket, the BufferedReader, the PrintWriter, and a boolean 'running' to
 * indicate if we should keep listening for the client inputs.
 * it will have the following functions: run(), HandleMsg, print, shutdown, and a constructor.
 *
 * @author Liad Cohen, Timor Sharabi.
 */
public class ConnectionThread extends Thread {
    private Socket mySocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean running = true;

    /**
     * A Constructor getting a Socket and a String username. it will initiate a bufferedReader and a printWriter,
     * and set the name of the thread to be the username given.
     * @param sock Socket, will update mySocket to the Socket given.
     * @param username String, will update the name of the Thread to be the username given.
     */
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

    /**
     * This method will be called when the thread starts.
     * It will also initiate a new thread to keep listening for client messages there, so we don't block this thread.
     */
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

    /**
     * This method will get a String message (coming from another thread, input from client).
     * and will handle the messages received from the client.
     * This method will notice what the client meant (is it an event or just a message to show on chat, etc).
     * The logic is as follows; the client sent:
     * starts with '!1' indicates the client requested to private message another client. its in the form of !2toUser:MSG.
     * starts with '!2' indicates the client requested to get all online users.
     * starts with '!3' indicates the client requested to disconnect. (so we can update the server and close buffers and threads properly)
     * starts with '!4' indicates the client is asking to set a username.
     * starts with '!5' indicates the client requested to broadcast a message to everyone on chat.
     * @param str String, the message from the client.
     */
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

    /**
     * This method will get a String str and send it to the client through the printWriter.
     * @param str String, the message to send to the client.
     */
    void print(String str) {
        writer.println(str);
    }

    /**
     * This method will shutdown current thread properly and close connection with the client, and will also notify the
     * server about the shutdown, so it is informed.
     * it will also indicate if we want to broadcast this disconnection or not.
     * @param silentShutdown Boolean, True indicates if we should broadcast this disconnection to all other clients or just
     *                       remove the connection without notifying other clients.
     */
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