package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;


public class ServerGUI {

    private ServerGUI() { //constructor.
        //actionListener for red X button to close window.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                String ObjButtons[] = {"Yes","No"};
                int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you want to exit? Liad & Timor will miss you.","Leaving Ex4 Amazing Chat?",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                {
                    Server.broadcastServEvents("Server is shutting down.."); //sends all clients, server is closing down.
                    System.exit(0);
                }
            }
        });

        //actionListener for Start/Stop button.
        startServerButton.addActionListener(e -> { //lambda function
            if (startServerButton.getText().equals("Start Server")) {
                int portInteger = 1337; //our default port
                try {
                    portInteger = Integer.parseInt(portField.getText().trim()); //parsing from text field into integer representing port number.
                } catch (Exception ex) {
                    addToEvents("Bad port! Starting server on default port: 1337");
                }
                if (portInteger < 1024 || portInteger > 65553) {
                    addToEvents("Bad port! Must be between 1024-65553. \nStarting server on default port: 1337");
                    portInteger = 1337;
                }
                this.server = new Server(portInteger, this); //just constructs an object Server with specific port. also sends this GUI to the server, so it can update some UI elements.
                Thread serverThread = new Thread(this.server); //assign new thread with Server object (Server implements Runnable)
                serverThread.start(); //created new Thread and starting the server listener there.
                toggleStartStopBtn();

            } else {
                Server.broadcastServEvents("Server is shutting down.."); //sends all clients, server is closing down.
                server.stopServer();
                addToEvents("Server has shut down.");
            }
        });

        // refresh button is pressed.

        refreshButton.addActionListener(e -> {
            if (startServerButton.getText().equals("Stop Server")) {
                DefaultListModel model = new DefaultListModel();
                ArrayList<ConnectionThread> connections = Server.getConnections();
                ArrayList<String> usernames = new ArrayList<>(connections.size());
                for(ConnectionThread ct : connections){
                    usernames.add(ct.getName());
                }
                model.addAll(usernames);
                onlineUsers.setModel(model);
                onlineUsers.setVisible(true);
            } else{
                addToEvents("Server is disconnected, cannot refresh online users..");
            }
        });
    }

    void addToEvents(String eventMsg) { //appending new event to according text area.
        eventsArea.append(eventMsg + "\n");
    }

    void addToMsgs(String chatMsg) {//appending new chat message to according text area.
        chatArea.append(chatMsg + "\n");
    }

    private void toggleStartStopBtn() {
        if (this.startServerButton.getText().equals("Start Server")) {
            this.startServerButton.setText("Stop Server");
        } else {
            this.startServerButton.setText("Start Server");
        }
    }

    void toggleStartStopBtn(boolean stopped) {
        if (stopped) {
            this.startServerButton.setText("Start Server");
        } else {
            this.startServerButton.setText("Stop Server");
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("Server - Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ServerGUI().panel1); //set the pane for the frame as our JPanel from our form.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.
        frame.setMinimumSize(new Dimension(650,490));
        frame.setSize(700,500);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //the default close for the frame, do nothing, because we will prompt a confirmation message. (in constructor).
        frame.setIconImage( new ImageIcon("./img/serverIcon.png").getImage()); // Set our icon to Server gui.
        new ServerGUI(); //calls the constructor.
    }

    /******* Private ********/
    private static JFrame frame;
    private JPanel panel1;
    private JButton startServerButton;
    private JTextField portField;
    private JLabel portLabel;
    private JLabel chatTitle;
    private JTextArea chatArea;
    private JTextArea eventsArea;
    private JLabel eventsTitle;
    private JList<String> onlineUsers;
    private JLabel onlineUsersLabel;
    private JButton refreshButton;
    private JScrollPane chatScroll;
    private Server server;


}
