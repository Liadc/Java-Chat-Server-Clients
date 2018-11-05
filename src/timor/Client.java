package timor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable {

    public Client(InetAddress host,int port){
        this.ip = host;
        this.port = port;
    }


    @Override
    public void run() {
        try {
            socket = new Socket(this.ip,this.port);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        writer.println(msg);
    }

    //Private Methods


    //Bones

    private int port;
    private InetAddress ip;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

}
