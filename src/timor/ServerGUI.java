package timor;

import javax.swing.*;

public class ServerGUI extends JFrame {



    ServerGUI(int port){
        super("Amazing Ex4 Chat App");
//        stopStartBtn = new JButton("Start Server");

    }


    public static void main(String[] args){
        new ServerGUI(3000); //update: this port to port text area value
    }

    /******* Private ********/
    private JPanel panel1;
    private JButton startServerButton;
    private JTextField portField;
    private JLabel portLabel;
    private JLabel chatTitle;
    private JTextArea chatArea;
    private Server server;


}
