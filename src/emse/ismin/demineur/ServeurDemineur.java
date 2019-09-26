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
    private List<DataInputStream> inList = new ArrayList<DataInputStream>();
    private List<DataOutputStream> outList = new ArrayList<DataOutputStream>();
    private List<Boolean> playerSateList = new ArrayList<Boolean>(); //Will contain the state of the player connection

    //Game Variables
    private boolean gameStarted = false;
    private Level level = Level.EASY;
    private Champ mineField = new Champ(level); //Start a new mine field with easy parameter by default
    private boolean[][] caseClicked; //Array that will represent the mine field and which field was already clicked
    private List<Integer> playerScoreList = new ArrayList<Integer>(); //Will contain a score for each player (0 initially)


    ServeurDemineur() {
        System.out.println("Server starting ...");
        //Create GUI
        guiServer = new GUIServer(this);
        setContentPane(guiServer);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        caseClicked = new boolean[mineField.getDimX()][mineField.getDimY()]; //Initialized to all false
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
            playerScoreList.add(0);
            playerSateList.add(true);
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

            while (playerSateList.get(playerId)) { //While the player sate is alive keep waiting for data
                command = in.readUTF();
                manageCommands(command, playerId);
            }
        } catch (IOException e) {
            if (playerSateList.get(playerId)) { //If the error is not cause due to an intentional disconnection
                guiServer.addDialogText("Error while receiving information from player " + playerId + ". " +
                        "Thread killed.");
                e.printStackTrace();
            }
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
    synchronized private void manageCommands(String cmd, int playerId) {
        switch (cmd) {
            case "POSITION":
                caseClicked(playerId); //Will wait for position information
                break;
            case "CLIENTDISCONNECT":
                playerSateList.set(playerId, false); //Set the player state to dead.
                guiServer.addDialogText("Player " + playerId + " has disconnected.");
            default:
                break;
        }
    }

    synchronized private void caseClicked(int playerId) {
        if (playerScoreList.get(playerId) != -1) { // If the player didn't lose
            try {
                int X = inList.get(playerId).readInt();
                int Y = inList.get(playerId).readInt();
                if (!caseClicked[X][Y]) {//If the case has not been clicked already
                    guiServer.addDialogText("Player " + playerId + " clicked on (X : " + X + ", Y : " + Y + ").");
                    caseClicked[X][Y] = true; //Set the case as clicked.
                    broadcastClick(playerId, X, Y);
                    if (!mineField.isMine(X, Y)) //If the player didn't clicked on a mine is score is increase
                        playerScoreList.set(playerId, playerScoreList.get(playerId) + 1); // The score of this player is increased
                    else
                        playerScoreList.set(playerId, -1); //Score of -1 means he lost
                    //TODO: What to do if everyone has lost
                } else {
                    guiServer.addDialogText("Case already clicked : Player " + playerId + " clicked on (X : " + X +
                            ", Y : " + Y + ").");
                }
            } catch (IOException e) {
                guiServer.addDialogText("Error while receiving position information from client " + playerId);
                e.printStackTrace();
            }
        } else {
            guiServer.addDialogText("Error: player" + playerId + " lost but is still sending click data.");
        }
    }

    /**
     * Send a broadcast message to all connected client
     *
     * @param msg The message (String) to send
     */
    public void broadcastMSG(String msg) {
        try {
            int i = 0;
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                if (playerSateList.get(i)) { //If the player state is still alive
                    out.writeUTF("MSG"); //Send the command
                    out.writeUTF(msg); //send the message
                }
                i++;
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast message.");
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client to say that the game started
     */
    public void gameStarted() {
        try {
            int i = 0;
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                if (playerSateList.get(i)) { //If the player state is still alive
                    out.writeUTF("STARTGAME"); //Send the command
                }
                i++;
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'STARTGAME'.");
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client to say that the game ended
     */
    public void gameStopped() {
        try {
            gameStarted = false;
            int i = 0;
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                if (playerSateList.get(i)) { //If the player state is still alive
                    out.writeUTF(Commands.ENDGAME.name()); //Send the command
                }
                i++;
            }
            guiServer.addDialogText("Game ended by server.");
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'ENDGAME'.");
            e.printStackTrace();
        }
    }

    /**
     * Determines if the game is started or not
     *
     * @return The state of the game
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sends a close server message to all clients and then close the server
     */
    public void closeServer() {
        try {
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                out.writeUTF(Commands.SERVERSTOPPED.name()); //Send the command
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'SERVERSTOPPED'.");
            e.printStackTrace();
        }
        System.out.println("The server is closing...");
        System.exit(0); //Close the server
    }

    /**
     * Set the state of the game. Either started or not.
     *
     * @param state
     */
    public void setGameStarted(boolean state) {
        gameStarted = state;
    }

    /**
     * Get the current level of mine field
     *
     * @return The current level of mine field
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Set the game level
     *
     * @param level A Level enum
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Will send to all 'alive' player first the x information of the position of the click then the y then the value of
     * the case and then the playerId that initiated the click.
     *
     * @param playerId Integer unique identifying a player
     * @param x        Integer X position of the click
     * @param y        Integer Y position of the click
     */
    private void broadcastClick(int playerId, int x, int y) {
        try {
            int i = 0;
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                if (playerSateList.get(i)) { //If the player state is still alive
                    out.writeUTF("POSITION"); //Send the command
                    out.writeInt(x);
                    out.writeInt(y);
                    if (mineField.isMine(x, y))
                        out.writeInt(-1); //-1 mines there is a Mine
                    else
                        out.writeInt(mineField.numberMinesSurrounding(x, y));
                    out.writeInt(playerId);
                }
                i++;
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast of a click information.");
            e.printStackTrace();
        }
    }
}
