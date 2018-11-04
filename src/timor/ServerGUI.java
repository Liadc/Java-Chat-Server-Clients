package timor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI {



    ServerGUI(){
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int portInteger = 1337;
                try {
                    portInteger = Integer.parseInt(portField.getText().trim());
                } catch (Exception ex) {
                    System.out.println("Bad port! Starting server on default port: 1337");
                }
                if(portInteger <1000 || portInteger>65553){
                    System.out.println("Bad port! Must be between 1001-65553. \nStarting server on default port: 1337");
                    portInteger = 1337;
                }
                Thread serverThread = new Thread(new Server(portInteger));
                serverThread.start(); //created new Thread and starting the server listener.
                chatArea.append("");

            }
        });
    }


    public static void main(String[] args){
        JFrame frame = new JFrame("Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ServerGUI().panel1); //set the pane for the frame as our JPanel from our form.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //the default close for the frame, just exit.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.


        new ServerGUI(); //update: this port to port text area value
    }

    /******* Private ********/
    private JPanel panel1;
    private JButton startServerButton;
    private JTextField portField;
    private JLabel portLabel;
    private JLabel chatTitle;
    private JTextArea chatArea;
    private JTextArea textArea1;
    private Server server;


}
