package timor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable {

    public Client(InetAddress host,int port){
        this.ip = host;
        this.port = port;

    }

    private int port;
    private InetAddress ip;
    private String name;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    @Override
    public void run() {
        try {
            socket = new Socket(this.ip,this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        //create readers and writes.
    }
}
