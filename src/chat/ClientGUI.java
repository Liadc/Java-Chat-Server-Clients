package chat;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientGUI {


    public ClientGUI() {
        connectButton.addActionListener(e -> {
            if (connectButton.getText().equals("Connect")){
            try {
                ip = InetAddress.getByName(ipField.getText()); //update: might fail, use try-catch.
                port = Integer.parseInt(portField.getText()); //update: might fail, use try-catch.
                client = new Client(ip, port, this);
                Thread clientThread = new Thread(client);
                clientThread.start();
                connectButton.setText("Disconnect");
            } catch (UnknownHostException e1) { //update to meaningful error and change gui accordingly.
                e1.printStackTrace();
            }
        }else{
                try {
                    sendMsg("!2");
                    client.closeConnection();
                } catch (Exception e1) { //update to correct exception
                    System.out.println("Exception thrown!!!");
                    e1.printStackTrace();
                }
                connectButton.setText("Connect");
            }

        });
        sendButton.addActionListener(e -> {
           sendMsg(msgField.getText());
        });
        refreshButton.addActionListener(e -> {
         client.requestOnline();
        });
    }
    public void addMsg(String msg) {
        chatArea.append(msg + "\n");
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Client - Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ClientGUI().mainPanel); //set the pane for the frame as our JPanel from our form.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //the default close for the frame, just exit.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.

        new ClientGUI();
    }

    /******* Private *********/

    private void sendMsg(String msg) {
        client.sendMsg(msg);
    }

    private Client client;
    private int port;
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
    private JList<String>  connectedUsers;
    private JButton refreshButton;

    public void setListModel(DefaultListModel _model) {
        DefaultListModel model = _model;
        connectedUsers.setModel(model);
        connectedUsers.setVisible(true);
    }
}
