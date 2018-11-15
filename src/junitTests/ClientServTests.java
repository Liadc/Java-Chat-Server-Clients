package junitTests;

import chat.Client;
import chat.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;

class ClientServTests {

    private static BlockingQueue<String> queue = new LinkedBlockingDeque<>(10); //our BlockingQueue sharing data with Client thread.
    static int countID = 1; //usersNameCOUNTID to login with, since username must be unique. -- for each test.
    private static Client testClient;
    private static InetAddress localhost;

    /**
     * Before all tests of client side, we create a local server listening on port 1337.
     */
    @BeforeAll
    static void createServClientConnection() {
        Server localServ = new Server(1337, null);
        Thread serverThread = new Thread(localServ); //assign new thread with Server object (Server implements Runnable)
        serverThread.start();
        System.out.println("Server Created, listening on port 1337");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            fail("Failed getting localhost address, this is an internal failure.");
        }
    }

    /**
     * Before each test, we will connect to the server and try to use a function.
     * Because a function might fail or things might go wrong - disconnection may happen or we disconnects on purpose.
     * hence, we will connect to our localServ before each test.
     */
    @BeforeEach
    void connectServ(){
        /********** Connection to server with client **************/

        testClient = new Client(localhost, 1337, null,"testUser"+countID++,queue);
        Thread clientThread = new Thread(testClient);
        clientThread.start();
        try {
            Thread.sleep(100); //lets connection be made - server initiates threads, connection with client, client initiates listener thread etc.
            if (queue.size()>0 && queue.take().contains("ERR:")) {
                fail("Failed connection to the localhost server");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /************* We now got a connected client ready for a new test! *****************/
    }

    /**
     * After each test, just sleep for 0.5 seconds before we connect to the server with a new client. no stress-tests required for the server.
     */
    @AfterEach
    void threadSleep(){
        try{
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * a client tries to connect to a wrong IP server. should get an error.
     */
    @Test
    void connectingWrongServer(){ //connecting to wrong ip server.
        InetAddress serverIP = null;
        try {
            serverIP = InetAddress.getByName("0.0.33.3"); //some wrong ip.
        } catch (UnknownHostException e) {
            fail("Failed getting localhost address, this is an internal failure.");
            e.printStackTrace();
        }
        Client testClient2 = new Client(serverIP, 1337, null,"testUser",queue);
        Thread clientThread = new Thread(testClient2);
        clientThread.start();
        try {
            Thread.sleep(3000); //gives client thread 7 seconds to try to connect to wrong server.
            if (queue.size()>0 && queue.take().contains("ERR: Cannot")) {
                //all ok. we should get ERR because we are connecting to wrong server.
            }
            else{
                fail("Something is wrong with connection to server. succeeded connecting to wrong IP.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * a Client requests all connected clients. the return from the server must at least return the current username as a connected client.
     * This test ensures requestUsername is also working.
     */
    @Test
    void requestOnline() {
        testClient.sendMsg("!2");
        try {
            Thread.sleep(1000); /** enough time for the server to handle the message and return an answer. over that -> consider as failure. */
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (queue.size() > 0 && queue.take().contains("TestUserRequestOnline")) {
                //all ok. Success.
            }else{
                fail("Something is wrong with requestOnline function, or handleMsg or requestUsername in Client side.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * a Client request to broadcast a message to everyone. the client himself (at least) should get its own message back from the server.
     */
    @Test
    void sendMsgBroadcast() {
        testClient.sendMsg("!5hi");
        try {
            Thread.sleep(1000); /** enough time for the server to handle the message and return an answer. over that -> consider as failure. */
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (queue.size() > 0 && queue.take().contains("hi")) {
                //all ok. Success.
            }else{
                fail("Something is wrong with broadcasting a message, or handleMsg in Client side.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * a Client request to private message to someone. (for testing purpose-himself) the client himself should get its own message back from the server.
     */
    @Test
    void sendPvtMsg() {
        testClient.sendMsg("!1");
        try {
            Thread.sleep(1000); /** enough time for the server to handle the message and return an answer. over that -> consider as failure. */
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (queue.size() > 0 && queue.take().contains("hi")) {
                //all ok. Success.
            }else{
                fail("Something is wrong with private message function, or handleMsg in Client side.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}