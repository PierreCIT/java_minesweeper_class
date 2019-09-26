package emse.ismin.demineur;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServeurDemineur extends JFrame implements Runnable {
    private GUIServer guiServer;
    private ServerSocket manageSock;
    private static final int serverPort = 10000; //Default server port
    private int playerIds = 0; //To id players by a number
    List<DataInputStream> inList = new ArrayList<DataInputStream>();
    List<DataOutputStream> outList = new ArrayList<DataOutputStream>();

    //Game Variables
    private boolean gameStarted = false;

    ServeurDemineur() {
        System.out.println("Server starting ...");
        //Create GUI
        guiServer = new GUIServer(this);
        setContentPane(guiServer);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        startServer();
    }

    private void startServer() {
        try {
            //Start socket management
            manageSock = new ServerSocket(serverPort);
            //Thread to wait for client
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServeurDemineur();
    }

    public void startGame() {
        guiServer.addDialogText("Game Started");
    }

    /**
     * A thead process that will listen to new connections. Once a new connection is established it will create a new
     * tread to listen to new connections. And will then confirm the connection with the client, assign a playerId and
     * start listening for the client.
     */
    @Override
    public void run() {
        String command;
        int playerId = -1; //Default value, negative because it should not be seen
        try {
            System.out.println("Thread " + playerIds + " Started");
            playerId = playerIds;
            playerIds++;
            Socket socket = manageSock.accept(); //New client connected
            new Thread(this).start(); //Wait for new client
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String namePlayer = in.readUTF(); //Wait for first message which shall contain the player's name
            inList.add(in);
            outList.add(out);
            guiServer.addDialogText("New connexion " + namePlayer + ", set as player " + playerId);
            out.writeUTF("MSG");//Send the command message to the client
            out.writeUTF("Connected as player " + playerId + "."); //Sent info connection confirmation to the client

            while (true) {
                command = in.readUTF();
                manageCommands(command, playerId);
            }

        } catch (IOException e) {
            guiServer.addDialogText("Error while receiving information from player " + playerId + ". " +
                    "Thread killed.");
            e.printStackTrace();
        }
    }

    /**
     * Manage command messages from clients. A client always have to start a communication by sending a command.
     * This command will be interpreted here, and the server will start to listen for the expected following
     * informations.
     *
     * @param cmd      A string that must exist in Commands ENUM
     * @param playerId A int that is the Id of the player's thread that called the function
     */
    private void manageCommands(String cmd, int playerId) {
        switch (cmd) {
            case "POSITION":
                caseClicked(playerId); //Will wait for position information
                break;
            default:
                break;
        }
    }

    private void caseClicked(int playerId) {
        try {
            int X = inList.get(playerId).readInt();
            int Y = inList.get(playerId).readInt();
        } catch (IOException e) {
            guiServer.addDialogText("Error while receiving position information from client " + playerId);
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client
     * @param msg The message (String) to send
     */
    public void broadcastMSG(String msg){
        try{
            for(DataOutputStream out: outList) { //For all output stream saved (broadcast)
                out.writeUTF("MSG"); //Send the command
                out.writeUTF(msg); //send the message
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast message.");
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client to say that the game started
     */
    public void gameStarted(){
        try{
            for(DataOutputStream out: outList) { //For all output stream saved (broadcast)
                out.writeUTF("STARTGAME"); //Send the command
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'STARTGAME'.");
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client to say that the game ended
     */
    public void gameStopped(){
        try{
            for(DataOutputStream out: outList) { //For all output stream saved (broadcast)
                out.writeUTF(Commands.ENDGAME.name()); //Send the command
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'ENDGAME'.");
            e.printStackTrace();
        }
    }

    /**
     * Determines if the game is started or not
     * @return The state of the game
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sends a close server message to all clients and then close the server
     */
    public void closeServer(){
        try{
            for(DataOutputStream out: outList) { //For all output stream saved (broadcast)
                out.writeUTF(Commands.SERVERSTOPPED.name()); //Send the command
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'SERVERSTOPPED'.");
            e.printStackTrace();
        }
        System.out.println("The server is closing...");
        System.exit(0); //Close the server
    }

    public void setGameStarted(boolean state) {
        gameStarted = state;
    }
}
