package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class represents the GUI of the client.
 * It will show buttons for connecting to server, disconnecting, sending message to everyone, sending private message,
 * refresh (for online users).
 * and input area for text of the message, for a private message, input for PORT, for InetAddress, for a username,
 * and for a username of the private message we send to.
 * and TextArea for the chat area itself.
 *
 * @author Liad Cohen, Timor Sharabi.
 */
public class ClientGUI {
    /**
     * Constructor for the GUI.
     * All listeners for actions (click events on buttons, etc.) will be in this constructor.
     * logic for connecting/valid inputs will be checked inside also.
     */
    private ClientGUI() {
        /** actionListener for red X Jframe close button.*/
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                String ObjButtons[] = {"Yes","No"};
                int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you want to exit? Liad & Timor will miss you.","Leaving Ex4 Amazing Chat?",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                {
                    if (connectButton.getText().equals("Disconnect")) { //this indicates the client is connected at this moment.
                        sendMsg("!3"); //sends the server !3 , indicating ask to disconnect.
                    }
                    System.exit(0);
                }
            }
        });

        /** actionListener for Connect/Disconnect button.*/
        connectButton.addActionListener(e -> {
            if (connectButton.getText().equals("Connect")){
                //check for valid inputs here, as well as username/port/ip etc. before try/catch.
                if (usernameTextField.getText() == null || usernameTextField.getText().equals("")) {
                    addMsg("You must enter a username!");
                    return;
                }
                /** lets check IP and PORT are valid. */
                try{
                    ip = InetAddress.getByName(ipField.getText());
                    port = Integer.parseInt(portField.getText()); //update: might fail, use try-catch.
                } catch (UnknownHostException e1) {
                    addMsg("Invalid IP provided. Unknown host.");
                    return;
                } catch (NumberFormatException e1){
                    addMsg("Invalid port provided. Must be an integer between 1024-65553");
                    return;
                }
                if(port<1024 || port >65553){ //only this range is valid.
                    addMsg("Invalid port provided. Must be an integer between 1024-65553");
                    return;
                }

                /** we can now try to connect on another Thread. */
                client = new Client(ip, port, this,usernameTextField.getText(),null);
                Thread clientThread = new Thread(client);
                clientThread.start();
                connectButton.setText("Disconnect");

        }else{
                try {
                    client.closeConnection();
                } catch (Exception e1) { //update to correct exception
                    System.out.println("Exception thrown!!!");
                    e1.printStackTrace();
                }
                connectButton.setText("Connect");
            }

        }); /** end actionListener for connect/disconnect button. */

        /** actionListener for "Send" button. for broadcasting. */
        sendAllButton.addActionListener(e -> {
            if(connectButton.getText().equals("Disconnect")){ //indicates client is connected.
                sendMsg("!5"+msgField.getText());
            }
            else{
                addMsg("You are disconnected, cannot send message...");
            }
           msgField.setText(""); //empty text area after message sent.

        }); /** end actionListener for sendAllButton. */

        /** actionListener for "Refresh" button. */
        refreshButton.addActionListener(e -> { //indicates client is connected.
            if (connectButton.getText().equals("Disconnect")) { //only if user is connected.
                client.requestOnline();
            }
            else{
                addMsg("You are disconnected, cannot refresh online users list...");
            }
        }); /** end actionListener for refreshButton. */

        /** actionListener for "Send Private Message" button. */
        sendPvtMsgBtn.addActionListener(e -> {
            if (connectButton.getText().equals("Disconnect")) { //only if user is connected.
                //try to send private message here.
                String pvtMsg = "!1" + userPvtMsgName.getText()+":"+pvtMsgText.getText(); //pvtMsg form: !1nameToSend:MSG
                client.sendMsg(pvtMsg);
                addMsg("Sending message to " +userPvtMsgName.getText()+": "+pvtMsgText.getText());
            }
            else{
                addMsg("You are disconnected, cannot send private message...");
            }
            pvtMsgText.setText("");
        });
        /** actionListener for "clearTextBtn" */
        clearTextBtn.addActionListener(e -> chatArea.setText(""));
    }

    /**
     * This method will get a String and append it to the chatArea element in the UI.
     * @param msg String, the message to add to the chat Area.
     */
    void addMsg(String msg) {
        chatArea.append(msg + "\n");
    }

    /**
     * Main function will run as we run the application.
     * we will declare new JFrame, set its properties and then call the constructor which will initiate everything to the screen.
     * @param args String[], will run without any params.
     */
    public static void main(String[] args) {
        frame = new JFrame("Client - Amazing Ex4 Chat App"); //new frame for our GUI
        frame.setContentPane(new ClientGUI().mainPanel); //set the pane for the frame as our JPanel from our form.
        frame.pack(); //causes the window to be sized to fit the preferred size and layouts of its sub-components.
        frame.setVisible(true); //showing the frame to the screen.
        frame.setMinimumSize(new Dimension(630,420));
        frame.setSize(640,440);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //the default close for the frame, do nothing, because we will prompt a confirmation message. (in constructor).
        frame.setIconImage( new ImageIcon("./img/clientIcon.png").getImage()); // Set our icon to Client gui.
        new ClientGUI(); //calls constructor.
    }

    /**
     * This method will get a DefaultListModel and add it to the GUI as the list of online users.
     * @param _model DefaultListModel, containing all online users.
     */
    void setListModel(DefaultListModel _model) {
        connectedUsers.setModel(_model);
        connectedUsers.setVisible(true);
    }

    /**
     * This method will return the connectButton of the GUI. the method will be called sometimes from the Client
     * class, so the client can update this button according to the connection with the server.
     * @return JButton, the connect/disconnect button for client gui.
     */
    JButton getConnectBtn(){ //will be used by Client.java to update button when connection is terminated or failed.
        return this.connectButton;
    }

    /******* Private *********/

    private void sendMsg(String msg) {
        client.sendMsg(msg);
    }


    private static JFrame frame;
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
    private JButton sendAllButton;
    private JList<String>  connectedUsers;
    private JButton refreshButton;
    private JLabel privateMsgLabel;
    private JTextField userPvtMsgName;
    private JTextField pvtMsgText;
    private JLabel onlineUsersLabel;
    private JLabel pvtMsgLabel;
    private JButton sendPvtMsgBtn;
    private JButton clearTextBtn;
    private JLabel usernameLabel;
    private JTextField usernameTextField;

}
