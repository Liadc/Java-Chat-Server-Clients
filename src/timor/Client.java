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
        try {
            socket = new Socket(this.ip, this.port);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable chatViewer = () -> {
            String line = null;
            while (keepGoing) {
                try {
                    line = reader.readLine();
                    if (line != null) {
                        handleMsg(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread chatThread = new Thread(chatViewer);
        chatThread.start();

    }




    public void sendMsg(String msg) {
        writer.println(msg);
    }

    public void shutdown() throws IOException {
        keepGoing = false;
//        socket.close();
    }

    //Private Methods

    private void handleMsg(String msg) {
        clientGUI.addMsg(msg);
    }
    //Bones

    private boolean keepGoing = true;
    private ClientGUI clientGUI;
    private int port;
    private InetAddress ip;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;


}
