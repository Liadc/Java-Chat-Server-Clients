package timor;

import javax.swing.*;

public class ServerGUI extends JFrame {

    private JPanel panel1;
    private JButton startServerButton;
    private JTextField textField1;
    private JTextArea textArea1;

    ServerGUI(int port){
        super("Amazing Ex4 Chat App");
        stopStartBtn = new JButton("Start Server");

    }


    public static void main(String[] args){
        new ServerGUI(3000); //update: this port to port text area value
    }

    /******* Private ********/
    private JButton stopStartBtn;
    private JTextField portField;
    private JTextArea chatArea;
    private Server server;


}
