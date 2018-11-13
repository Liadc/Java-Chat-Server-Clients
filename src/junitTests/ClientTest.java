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

class ClientTest {

    private BlockingQueue<String> queue = new LinkedBlockingDeque<>(10); //our BlockingQueue sharing data with Client thread.
    static int countID =0; //usersNameCOUNTID to login with, since username must be unique.
    private Client testClient;
    private InetAddress localhost;

    /**
     * Before all tests of client side, we create a local server listening on port 1337.
     */
    @BeforeAll
    static void createServClientConnection(){
        Server localServ = new Server(1337);
        Thread serverThread = new Thread(localServ); //assign new thread with Server object (Server implements Runnable)
        serverThread.start();
        System.out.println("Server Created, listening on port 1337");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Before each test, we will connect to the server and try to use a function.
     * Because a function might fail or things might go wrong - disconnection may happen or we disconnects on purpose.
     * hence, we will connect to our localServ before each test.
     */
    @BeforeEach
    void connectServ(){
        localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            fail("Failed getting localhost address, this is an internal failure.");
        }
        testClient = new Client(localhost, 1337, null,"testUSER"+countID++,queue);
        Thread clientThread = new Thread(testClient);
        clientThread.start();
        try {
            Thread.sleep(20);
            if (queue.take().contains("ERR:")) {
                fail("Failed connection to the localhost server");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void disconnectClient(){
        testClient.closeConnection();
    }

    @Test
    void connectingWrongServer(){

    }

    @Test
    void run() {
    }

    @Test
    void requestOnline() {
    }

    @Test
    void sendMsg() {
    }

    @Test
    void closeConnection() {
    }

}