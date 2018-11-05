package timor;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientGUI {


    public ClientGUI() {
        connectButton.addActionListener(e -> {
            try {
                ip = InetAddress.getByName(ipField.getText());
                port = Integer.parseInt(portField.getText());
                socket = new Socket(ip, port);
                client = new Client(ip,port);
                Thread clientThread = new Thread(client);
                clientThread.start();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }



        });
        sendButton.addActionListener(e -> {
           sendMsg(msgField.getText());
        });
    }

    private void sendMsg(String msg) {
        client.sendMsg(msg);


    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ClientGUI().mainPanel); //set the pane for the frame as our JPanel from our form.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //the default close for the frame, just exit.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.

        new ClientGUI();
    }


    private Client client;
    private int port;
    private Socket socket;
    private InetAddress ip;
    private JButton connectButton;
    private JTextField ipField;
    private JTextField portField;
    private JTextArea chatArea;
    private JLabel IP;
    private JLabel portLabel;
    private JLabel chatLabel;
    private JPanel mainPanel;
    private JTextField msgField;
    private JLabel msgLabel;
    private JButton sendButton;
}
