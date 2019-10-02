package emse.ismin.demineur;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Class of the server that manages connections, the players and the game : the entire server.
 */
public class ServerMinesWeeper extends JFrame implements Runnable {
    private GUIServer guiServer;
    private ServerSocket manageSock;
    private static final int serverPort = 10000; //Default server port
    private int playerIds = 0; //To id players by a number
    private WriteScoreInFile fileScoreWriter = new WriteScoreInFile();

    //Game Variables
    private boolean gameStarted = false;
    private Level level = Level.EASY;
    private Field mineField = new Field(level); //Start a new mine field with easy parameter by default
    private boolean[][] caseClicked; //Array that will represent the mine field and which field was already clicked
    //List that will contain all the information of all the players
    private List<Player> playersList = new ArrayList<>();

    private ServerMinesWeeper() {
        System.out.print("Server starting ... ");
        //Create GUI
        guiServer = new GUIServer(this);
        setContentPane(guiServer);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        caseClicked = new boolean[mineField.getDimX()][mineField.getDimY()]; //Initialized to all false
        startServer();

        //To do when clicking on the window's 'X' button.
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                closeServer();
            }
        });
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
        new ServerMinesWeeper();
    }

    void startGame() {
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
            new Thread(this).start(); //Wait for new client
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String namePlayer = in.readUTF(); //Wait for first message which shall contain the player's name
            Player player = new Player(namePlayer, playerId, in, out);
            player.setInGame(!gameStarted); //If a player join while the game is started he is set to not be inGame
            playersList.add(player);
            guiServer.addDialogText("New connexion " + namePlayer + ", set as player " + playerId);
            out.writeUTF(Commands.MSG.name());//Send the command message to the client
            out.writeUTF("Connected as player " + playerId + "."); //Sent info connection confirmation to the client
            out.writeUTF(Commands.PLAYERID.name());
            out.writeInt(playerId);

            while (getPlayerById(playerId).isConnected()) { //While the player sate is connected keep waiting for data
                command = in.readUTF();
                manageCommands(command, playerId);
            }
        } catch (IOException e) {
            if (getPlayerById(playerId).isConnected()) { //If the error is not due to an intentional disconnection
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
                getPlayerById(playerId).disconnected(); //Set the player state to disconnected.
                guiServer.addDialogText("Player " + playerId + " has disconnected.");
            default:
                break;
        }
    }

    /**
     * A getter of the mine field
     *
     * @return Champ: The current mines filed object
     */
    Field getMineField() {
        return mineField;
    }

    /**
     * Managethe information of a click from a player. It will manage if the case was already clicked, if the player
     * lost, won or simply finished the game and lauch the followinf process accordingly.
     *
     * @param playerId Integer which is the Id of the player who clicked.
     */
    synchronized private void caseClicked(int playerId) {
        if (gameStarted) {
            if (!getPlayerById(playerId).isExploded()) { // If the player didn't exploded/loose
                try {
                    int X = getPlayerById(playerId).getIn().readInt();
                    int Y = getPlayerById(playerId).getIn().readInt();
                    if (!caseClicked[X][Y]) {//If the case has not been clicked already
                        guiServer.addDialogText("Player " + playerId + " clicked on (X : " + X + ", Y : " + Y + ").");
                        caseClicked[X][Y] = true; //Set the case as clicked.
                        broadcastClick(playerId, X, Y);
                        if (!lastPlayerAlive()) {
                            if (!mineField.isMine(X, Y)) { //If the player didn't clicked on a mine is score is increase
                                getPlayerById(playerId).increaseScore(); // The score of this player is increased
                                if (isWin()) {
                                    playerFinishedGame(playerId);
                                }
                            } else {
                                getPlayerById(playerId).setExploded(true);
                                youLostClient(playerId);
                            }
                        } else {
                            playerFinishedGame(playerId);
                        }
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
        } else {
            guiServer.addDialogText("Error " + playerId + " send click position information but the game is" +
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
        for (Player player : playersList) {
            if (player.isInGame() && player.isConnected() && !player.isExploded()) {
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
            for (Player player : playersList) { //For all output stream saved (broadcast)
                if (player.isInGame() && !player.isExploded() && player.isConnected()) { //If the player state is still alive
                    player.getOut().writeUTF(Commands.FINISHGAME.name()); //Send the command
                }
                //Send to all players in game the information
                if (player.isInGame() && player.isConnected()) {
                    player.getOut().writeUTF(Commands.MSG.name());
                    player.getOut().writeUTF(player.getNickname() + "Player " + playerId + " finished the game !");
                }
            }
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
        playersList.sort(new SortByScore());

        //Is there Ex-Aequo
        int maxScore = playersList.get(0).getScore(); //Since the list is sorted the first player has the maximum score
        int nbMaxValue = 0;
        for (Player value : playersList) {
            if (value.getScore() == maxScore && value.isInGame()) {
                nbMaxValue++;
            }
        }
        //Send to all player with maximum score the information that they won.
        for (int i = 0; i < nbMaxValue; i++) {
            try {
                if(!playersList.get(i).isExploded())
                    playersList.get(i).getOut().writeUTF(Commands.WIN.name());
            } catch (IOException e) {
                guiServer.addDialogText("Error while sending 'WIN' command.");
                e.printStackTrace();
            }
        }
        //Broadcast ranking
        for (Player player : playersList) {
            if (player.isInGame() && player.isConnected()) { //Only broadcast to player who played in the game
                for (Player value : playersList) {
                    String msg = value.getNickname() + " (" + value.getPlayerId() + ") : " +
                            value.getScore() + ".";
                    if (value.isExploded()) {
                        msg += " Exploded.";
                    }
                    if (!value.isConnected()) {
                        msg += " Disconnected in game.";
                    }
                    try {
                        player.getOut().writeUTF(Commands.MSG.name());
                        player.getOut().writeUTF(msg);
                    } catch (IOException e) {
                        guiServer.addDialogText("Error while sending the rank.");
                        e.printStackTrace();
                    }
                }
            }
        }
        fileScoreWriter.writeOnlineScoreInScoreOnlineFile(playersList, level);
        deleteDisconnectedPlayers();
    }

    /**
     * Will delete disconnected players of the list of players
     */
    synchronized private void deleteDisconnectedPlayers() {
        playersList.removeIf(player -> !player.isConnected()); //remove all disconnected players
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
            getPlayerById(playerId).getOut().writeUTF(Commands.LOST.name());
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
    void broadcastMSG(String msg) {
        try {
            for (Player player : playersList) {
                if (player.isConnected()) {
                    player.getOut().writeUTF("MSG"); //Send the command
                    player.getOut().writeUTF(msg); //send the message
                }
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast message.");
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client to say that the game started
     */
    void gameStarted() {
        try {
            for (Player player : playersList) {
                if (player.isConnected()) {
                    player.getOut().writeUTF(Commands.STARTGAME.name()); //Send the command
                }
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast :'STARTGAME'.");
            e.printStackTrace();
        }
    }

    /**
     * Send a broadcast message to all connected client to say that the game ended
     */
    synchronized void gameStopped() {
        try {
            gameStarted = false;
            for (Player player : playersList) {
                if (player.isConnected() && player.isInGame()) {
                    player.getOut().writeUTF(Commands.ENDGAME.name()); //Send the command
                }
                deleteDisconnectedPlayers();
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
    boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Sends a close server message to all clients and then close the server
     */
    synchronized void closeServer() {
        try {
            for (Player player : playersList) {
                if (player.isConnected()) {
                    player.getOut().writeUTF(Commands.SERVERSTOPPED.name()); //Send the command
                    player.disconnected();
                }
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
     * @param state Boolean to put the state of the game to.
     */
    void setGameStarted(boolean state) {
        gameStarted = state;
    }

    /**
     * Get the current level of mine field
     *
     * @return The current level of mine field
     */
    Level getLevel() {
        return level;
    }

    /**
     * Set the game level
     *
     * @param level A Level enum
     */
    void setLevel(Level level) {
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
            for (Player player : playersList) {
                if (player.isConnected() && player.isInGame()) {
                    player.getOut().writeUTF("POSITION"); //Send the command
                    player.getOut().writeInt(x);
                    player.getOut().writeInt(y);
                    if (mineField.isMine(x, y))
                        player.getOut().writeInt(-1); //-1 means there is a Mine
                    else
                        player.getOut().writeInt(mineField.numberMinesSurrounding(x, y));
                    player.getOut().writeInt(playerId);
                }
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending broadcast of a click information.");
            e.printStackTrace();
        }
    }

    /**
     * Place new mines according to the level and send the information to the client.
     */
    synchronized void newGameServer() {
        try {
            if (level == Level.CUSTOM) {
                mineField.newGame(level, mineField.getDimXCustom(), mineField.getDimYCustom());
            } else {
                mineField.newGame(level);
            }
            caseClicked = new boolean[mineField.getDimX()][mineField.getDimY()]; //Set a new case clicked
            for (Player player : playersList) {
                if (player.isConnected()) {
                    //We set the state to in Game because we are now sure the player was connected when the game started
                    player.setInGame(true);
                    player.setScore(0); //Reset player score to 0
                    player.setExploded(false);
                    player.getOut().writeUTF(Commands.LEVEL.name()); //Send the command
                    player.getOut().writeUTF(level.name()); //Send the command
                    if (level == Level.CUSTOM) {
                        player.getOut().writeInt(mineField.getDimXCustom());
                        player.getOut().writeInt(mineField.getDimYCustom());
                    }
                    player.getOut().writeUTF(Commands.NEWGAME.name()); //Send the command
                }
            }
        } catch (IOException e) {
            guiServer.addDialogText("Error while sending new game information : " + level);
            e.printStackTrace();
        }
    }

    private Player getPlayerById(int playerIdToLookFor) {
        for (Player player : playersList) {
            if (player.getPlayerId() == playerIdToLookFor) {
                return player;
            }
        }
        System.out.println("Error : server looking for a playerId that is not in the list of the players.");
        guiServer.addDialogText("Error : server looking for a playerId that is not in the list of the players.");
        return notFoundPlayer(); //We return a false player that will be as he was dead, disconnected, not in Game, negative score
    }

    /**
     * Create a virtual player that will fail every verification
     *
     * @return A Player object that is made to fail every test.
     */
    private Player notFoundPlayer() {
        Player notFound = new Player("notFound", -1);
        notFound.setInGame(false);
        notFound.disconnected();
        notFound.setExploded(true);
        notFound.setScore(-2);
        return notFound;
    }
}
