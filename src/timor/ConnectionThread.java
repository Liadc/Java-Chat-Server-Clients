package timor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionThread extends Thread{
   private Socket mySocket;
   private BufferedReader reader;
   private PrintWriter writer;
   private boolean running=true;
//   String name;

   public ConnectionThread(Socket sock){
       this.mySocket = sock;
       try {
           this.reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
           this.writer = new PrintWriter(sock.getOutputStream(),true);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    @Override
    public void run() {
        System.out.println("Starting thread...");
        writer.println("Hi from server thread, enter your name: ");
        String line = null;
        while (running) {
            try {
                line = reader.readLine();
                if(line != null)
                    HandleMsg(line);
                else{
                    System.out.println("entered null, now removing u");
                    Server.removeConnection(getId());
                    shutdown();
                }
            } catch (Exception e) {
                System.out.println("Some exception in thread");
                e.printStackTrace();
            }


//            if(line.startsWith(""))
        }
//        Server.removeConnection(getId());

    }

    private void HandleMsg(String str){
        if(str.startsWith("!")){ //to broadcast, start with !
            Server.Broadcast(str.substring(1),getId());
        }
        else if(str.startsWith("@")){
            System.out.println("Sending request from thread to Server, im asking to privateMessage");
            Server.sendMsg(str.substring(1),getId());
        }
        else if(str.matches("\\$")){
            writer.println("All users connected: " + Server.getUsersOnline(getId()));
        }
    }

    public void Print(String str){
       writer.println(str);
    }
    public void shutdown(){
       running = false;
    }
}