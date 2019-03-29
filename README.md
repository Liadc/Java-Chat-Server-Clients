# Networks-Ex4
An amazing chat app using Java including a Server backend and a Client frontend, a GUI and support for multiple users online. 
Supports private messaging between clients.

<b>Client side GUI:</b>

![image](https://user-images.githubusercontent.com/6436094/55222392-d786ac00-521c-11e9-9ca6-8f2aae3257b8.png)


<b>Server side GUI:</b>

![image](https://user-images.githubusercontent.com/6436094/55222435-f4bb7a80-521c-11e9-9319-21d609974c84.png)


### Instructions: 

Run Server.jar on your server/host. Choose a port between 1024-65553 and click "Start Server".

Run Client.jar on any computer with internet connection. Does <b>NOT</b> have to be in the same network or LAN.

Client.jar can be run anywhere. Pick a username, enter the host IP address and the port which is running on.
Click "Connect". 

Have fun chatting.

Server supports tens or hundreds of users online at the same time, all depending on host server hardware. (New thread for each client).

<br>
<b>- Notes:</b>
Server can see private messages between users.<br>
Server informs of user connecting/disconnecting.<br>
Server informs of user trying/failure to connect (username already in use, socket connection failure etc.). <br>
Server has "Server Events" window with a GUI to inform Server Admin with all critical information happening in the server or chat room.<br>

#### This project is a Computer Networks Course assignment number 4 at Ariel University.

A Class Diagram for the project:

![image](https://github.com/Liadc/Networks-Ex4/blob/master/classDiagram/UpdatedClassDiagram.png?raw=true)
