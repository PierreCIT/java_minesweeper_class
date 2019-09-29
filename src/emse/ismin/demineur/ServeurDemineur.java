package emse.ismin.demineur;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
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
    //List that will contain all the information of all the players
    private List<Player> playersList= new ArrayList<Player>();

    ServeurDemineur() {
        System.out.println("Server starting ...");
        //Create GUI
        guiServer = new GUIServer(this);
        setContentPane(guiServer);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        //cheat to test
        mineField.print();
        caseClicked = new boolean[mineField.getDimX()][mineField.getDimY()]; //Initialized to all false
        startServer();
    }

    private void startServer() {
        try {
            //Start socket management
            manageSock = new ServerSocket(serverPort);
            //Thread to wait for client
            System.out.println("Server started.");
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
            playerId = playerIds;
            playerIds++;
            Socket socket = manageSock.accept(); //New client connected
            playerSateList.add(true);
            new Thread(this).start(); //Wait for new client
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String namePlayer = in.readUTF(); //Wait for first message which shall contain the player's name
            Player player = new Player(namePlayer, playerId);
            player.setInGame(!gameStarted); //If a player join while the game is started he is set to not be inGame
            playersList.add(player);
            inList.add(in);
            outList.add(out);
            guiServer.addDialogText("New connexion " + namePlayer + ", set as player " + playerId);
            out.writeUTF(Commands.MSG.name());//Send the command message to the client
            out.writeUTF("Connected as player " + playerId + "."); //Sent info connection confirmation to the client
            out.writeUTF(Commands.PLAYERID.name());
            out.writeInt(playerId);

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

    /**
     * A getter of the mine field
     * @return Champ: The current mines filed object
     */
    public Champ getMineField() {
        return mineField;
    }

    /**
     * Managethe information of a click from a player. It will manage if the case was already clicked, if the player
     * lost, won or simply finished the game and lauch the followinf process accordingly.
     * @param playerId  Integer which is the Id of the player who clicked.
     */
    synchronized private void caseClicked(int playerId) {
        if(gameStarted) {
            if (playerScoreList.get(playerId) != -1) { // If the player didn't lose
                try {
                    int X = inList.get(playerId).readInt();
                    int Y = inList.get(playerId).readInt();
                    if (!caseClicked[X][Y]) {//If the case has not been clicked already
                        guiServer.addDialogText("Player " + playerId + " clicked on (X : " + X + ", Y : " + Y + ").");
                        caseClicked[X][Y] = true; //Set the case as clicked.
                        broadcastClick(playerId, X, Y);
                        if (!lastPlayerAlive()) {
                            if (!mineField.isMine(X, Y)) { //If the player didn't clicked on a mine is score is increase
                                playerScoreList.set(playerId, playerScoreList.get(playerId) + 1); // The score of this player is increased
                                if (isWin()) {
                                    playerFinishedGame(playerId);
                                }
                            } else {
                                playerScoreList.set(playerId, -1); //Score of -1 means he lost
                                youLostClient(playerId);
                            }
                        } else {
                            playerFinishedGame(playerId);
                        }
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
        }else{
            guiServer.addDialogText("Error "+ playerId + " send click position information but the game is" +
                    " not started.");
        }
    }

    /**
     * Give the information that if the person that click is the last one alive then he is the winner.
     *
     * @return A boolean true if only one player is connected and didn't already lost/exploded/score = -1
     */
    private boolean lastPlayerAlive() {
        int playerConnectedAndAlive = 0;
        for (int i = 0; i < playerSateList.size(); i++) {
            if (playerSateList.get(i) && playerScoreList.get(i) != -1) {
                playerConnectedAndAlive++;
            }
        }
        return playerConnectedAndAlive == 1;
    }

    /**
     * Actions to perform when a player finished the game. We will then compute the scores and show the ranking
     * as well as inform the player who won
     *
     * @param playerId Integer of the playerId who finished the game.
     */
    private void playerFinishedGame(int playerId) {
        //Send to all players the information that the game is finished
        try {
            int i = 0;
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                if (playerSateList.get(i)) { //If the player state is still alive
                    out.writeUTF(Commands.FINISHGAME.name()); //Send the command
                }
                i++;
            }
            broadcastMSG("Player " + playerId + " finished the game !"); //Broadcast the information
            computeScores();
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending information that a player " + playerId + " finished" +
                    "the game.");
            e.printStackTrace();
        }
    }

    /**
     * Compute the ranking for each player and broadcast it. Sends a special command to the winner.
     */
    private void computeScores() {
        int max = Collections.max(playerScoreList);
        int nbExAequo = Collections.frequency(playerScoreList, max);
        if (nbExAequo == 1) {
            int playerIdWinner = playerScoreList.indexOf(max);
            try {
                outList.get(playerIdWinner).writeUTF("WIN");
                broadcastMSG("Player " + playerIdWinner + " won !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { // If several player with the same score
            List<Integer> listWinnners = new ArrayList<Integer>();
            for (int i = 0; i < playerScoreList.size(); i++) {
                if (playerScoreList.get(i) == max) {
                    listWinnners.add(i);
                }
            }
            broadcastMSG("Several winners : ");
            for (int i = 0; i < listWinnners.size(); i++) {
                try {
                    outList.get(listWinnners.get(i)).writeUTF("WIN");
                    broadcastMSG("Player " + listWinnners.get(i) + " won !");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Check the state of the game and answer to the question : Is it a victory ? True then the last click was the click
     * of victory and false otherwise
     *
     * @return A boolean answering the question is the game won ?
     */
    private boolean isWin() {
        return mineField.getNbMines() + nbCaseClicked() >= mineField.getDimY() * mineField.getDimX();
    }

    /**
     * Sum the all the score of the game
     *
     * @return The number of case clicked
     */
    private int nbCaseClicked() {
        int sum = 0;
        for (int x = 0; x < mineField.getDimX(); x++) {
            for (int y = 0; y < mineField.getDimY(); y++) {
                if (caseClicked[x][y]) {
                    sum++;
                }
            }
        }
        return sum;
    }

    /**
     * Send to the player the information that he lost
     *
     * @param playerId Integer of the id of the player who the message you lost must be send
     */
    private void youLostClient(int playerId) {
        try {
            outList.get(playerId).writeUTF(Commands.LOST.name());
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending information that a player " + playerId + " lost.");
            e.printStackTrace();
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

    /**
     * Place new mines according to the level and send the information to the client.
     */
    synchronized public void newGameServer() {
        try {
            if(level==Level.CUSTOM){
                mineField.newGame(level, mineField.getDimXCustom(), mineField.getDimYCustom());
            }else{
                mineField.newGame(level);
            }
            caseClicked = new boolean[mineField.getDimX()][mineField.getDimY()]; //Set a new case clicked
            int i = 0;
            for (DataOutputStream out : outList) { //For all output stream saved (broadcast)
                if (playerSateList.get(i)) { //If the player state is still alive
                    out.writeUTF("LEVEL"); //Send the command
                    out.writeUTF(level.name()); //Send the command
                    if(level==Level.CUSTOM){
                        out.writeInt(mineField.getDimXCustom());
                        out.writeInt(mineField.getDimYCustom());
                    }
                    out.writeUTF(Commands.NEWGAME.name()); //Send the command
                    playerScoreList.set(i, 0); //Reset values scores.
                }
                i++;
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending new game information : " + level);
            e.printStackTrace();
        }
    }

    public Player getPlayerById(int playerIdToLookFor){
        for (int i = 0; i < playersList.size(); i++) {
            if(playersList.get(i).getPlayerId() == playerIdToLookFor){
                return playersList.get(i);
            }
        }
        System.out.println("Error : server looking for a playerId that is not in the list of the players.");
        guiServer.addDialogText("Error : server looking for a playerId that is not in the list of the players.");
        return notFoundPlayer(); //We return a false player that will be as he was dead, disconnected, not in Game, negative score
    }

    /**
     * Create a virtual player that will fail every verification
     * @return
     */
    private Player notFoundPlayer() {
        Player notFound = new Player("notFound", -1 );
        notFound.setInGame(false);
        notFound.disconnected();
        notFound.setExploded(true);
        notFound.setScore(-2);
        return notFound;
    }
}
