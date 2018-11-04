package timor;

import javax.swing.*;

public class ServerGUI {



    ServerGUI(){ //constructor.
        startServerButton.addActionListener(e -> { //lambda function
            int portInteger = 1337; //our default port
            try {
                portInteger = Integer.parseInt(portField.getText().trim()); //parsing from text field into integer representing port number.
            } catch (Exception ex) {
                eventsArea.append("Bad port! Starting server on default port: 1337\n");
            }
            if(portInteger <1000 || portInteger>65553){
                eventsArea.append("Bad port! Must be between 1001-65553. \nStarting server on default port: 1337");
                portInteger = 1337;
            }
            Thread serverThread = new Thread(new Server(portInteger));
            serverThread.start(); //created new Thread and starting the server listener there.
            eventsArea.append("Server started on new Thread, listening on port "+portInteger+"...");
        });
    }


    public static void main(String[] args){
        JFrame frame = new JFrame("Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ServerGUI().panel1); //set the pane for the frame as our JPanel from our form.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //the default close for the frame, just exit.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.


        new ServerGUI(); //calls the constructor.
    }

    /******* Private ********/
    private JPanel panel1;
    private JButton startServerButton;
    private JTextField portField;
    private JLabel portLabel;
    private JLabel chatTitle;
    private JTextArea chatArea;
    private JTextArea eventsArea;
    private JLabel eventsTitle;
    private Server server;


}
