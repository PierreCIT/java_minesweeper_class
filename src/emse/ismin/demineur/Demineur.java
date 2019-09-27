package emse.ismin.demineur;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that will handle local and online game of the MinesWeeper
 */
public class Demineur extends JFrame implements Runnable {
    public final static String FILENAME = "score.dat";
    public int score = 20;
    public Level level = Level.EASY;
    private Champ champMines = new Champ(level);
    private boolean started = false;
    private GUI panel;
    private boolean lost;
    private int nbCaseClicked = 0;
    //Connection
    public String ipDefault = "127.0.0.1";
    public int portDefault = 10000;
    public boolean connected = false;
    private Socket sock;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread process; //Thread that will listen for server messages
    private String commmand; // String that will receive commands from the server
    //Online game
    private boolean gameStarted = false; //Boolean that describes the state of the online game
    private boolean onlineGame = false; //Boolean that set is the game is to be played with a server or in local.

    /**
     * Constructor of the 'Demineur' (MinesWeeper) which will initialize the game
     */
    public Demineur() {
        super("Demineur connecté");
        champMines.print();
        panel = new GUI(this);
        setContentPane(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Method called to exist the program and print an exit message to the player
     */
    public void quit() {
        System.out.println("Closing.... See you soon !");
        System.exit(0);
    }

    /**
     * Main of MineWeeper game and the interface.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("Welcome in the MineWeeper.");
        new Demineur();
    }

    /**
     * @return The private variable containing the
     */
    public Champ getChamp() {
        return champMines;
    }

    /**
     * @return The state of the game, if true the game is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Set if the game is started or not.
     *
     * @param started A boolean if true the game is started and the opposite otherwise.
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Return the interface object
     *
     * @return
     */
    public GUI getGui() {
        return this.panel;
    }

    /**
     * @return The game state.
     */
    public boolean isLost() {
        return lost;
    }

    /**
     * Set the game state to the given value. True means lost false means the game keeps going.
     *
     * @param lost A boolean representing the game state (alive or dead).
     */
    public void setLost(boolean lost) {
        this.lost = lost;
    }

    /**
     * Set the attribute variable which represents the number of clicked cases to the given number.
     *
     * @param nbCaseClicked Value to set the number of cases clicked to.
     */
    public void setNbCaseClicked(int nbCaseClicked) {
        this.nbCaseClicked = nbCaseClicked;
    }

    /**
     * @return the number of cases clicked.
     */
    public int getNbCaseClicked() {
        return this.nbCaseClicked;
    }

    /**
     * By computing the number of discovered cases and comparing to the total number of cases and mines
     * tests if the game is win.
     *
     * @return true if the game is won, false otherwise.
     */
    public boolean isWin() {
        return getNbCaseClicked() + getChamp().getNbMines() == getChamp().getDimY() * getChamp().getDimX();
    }

    /**
     * Reset the game when the level has to be same as the previous one in the next game.
     */
    public void newGame() {
        setStarted(false);
        setLost(false);
        setNbCaseClicked(0);
    }

    /**
     * Reset the game and set the level to the leveled passed in parameters
     *
     * @param level Level of the new game.
     */
    public void newGame(Level level) {
        this.level = level;
        setStarted(false);
        setLost(false);
        setNbCaseClicked(0);
    }

    /**
     * Function that will save the game score into a file.
     */
    public void saveScore() {
        //TODO: Finish saving score in a file.
        Path path = Paths.get(FILENAME);

        if (!Files.exists(path)) {
            for (int i = 0; i < Level.values().length; i++) {
            }
        }
    }

    /**
     * Initializes connection with the server given the address of the server and create in and out dataStream.
     * It will also send a first message containing the pseudo of the player. It will then launch a thread that*
     * will listen for data from the server.
     * @param ip String containing the ip of the server to connect to.
     * @param port Integer that will the port number used to connect to the server.
     * @param nickname String containing the pseudo/nickname of the player.
     */
    public void connectServer(String ip, int port, String nickname) {
        onlineGame = true; //Connection to server the game will be set to online.
        try {
            sock = new Socket(ip, port);
            out = new DataOutputStream(sock.getOutputStream());
            in = new DataInputStream(sock.getInputStream());
            if (nickname.length() > 0) {
                out.writeUTF(nickname);
            } else {
                out.writeUTF("DefaultNickname");
            }
            connected = true;
            panel.coDecoButtonChangeText();
            process = new Thread(this);
            process.start();
        } catch (UnknownHostException e) {
            System.out.println("Impossible to connect to " + ip + ":" + port + " with nickname:" + nickname);
            JOptionPane.showConfirmDialog(null, "Impossible to connect to " + ip + ":" +
                            port + " with nickname:" + nickname, "Close confirmation",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Impossible to connect to " + ip + ":" + port + " with nickname:" + nickname);
            JOptionPane.showConfirmDialog(null, "Impossible to connect to " + ip + ":" +
                            port + " with nickname:" + nickname, "Close confirmation",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Disconnect from the server, sends the server the information and reset the interface to be ready to reconnect
     * or to play local game.
     */
    public void disconnect() {
        onlineGame = false; //Disconnecting from server so the type of game returns to default : local.
        if(connected) { //We don't need to disconnect if we are not connected
            connected = false; //Indicates that we are no longer connected to a server.
            try {
                out.writeUTF(Commands.CLIENTDISCONNECT.name());
                in.close();
                out.close();
                sock.close();
                System.out.println("Disconnected from server.\n");
                panel.coDecoButtonChangeText();
                panel.setNewGameButtonState(true);
                process = null; //We kill the process that was waiting for messages from the server
                panel.addMsgGui("You disconnected from server.");
            } catch (IOException e) {
                panel.addMsgGui("Error while disconnecting from server.");
                System.out.println("Error while disconnecting from server.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Thread that will have for task to listen to input from server
     * The input will always start by a command. So the information read will be send to processCommands.
     * It also handles intentional and  unintentional disconnection with the server.
     */
    @Override
    public void run() {
        //boucle infini

        //lecture dans in
        //lecture de la commande
        //en fct de la datareceived on affiche/mines ...
        //lecture du joueur qui a cliqué en XY
        while (process != null) {
            try {
                commmand = in.readUTF();
                processCommands(commmand);
            } catch (IOException e) {
                if (connected) { //If the state says that we are supposed to be connected then it's a real error
                    e.printStackTrace();
                } else { //Otherwise it's just that we closes connection.
                    System.out.println("Connection with server closed (message from error management of waiting " +
                            "servers's input");
                }
            }
        }
    }

    /**
     * From the command given in parameters will select what action to do
     *
     * @param cmd A string which must corresponds to one of the possibilities of the command ENUM
     */
    private void processCommands(String cmd) {
        switch (cmd) {
            case "MSG":
                readAndPrintServerMessage();
                break;
            case "POSITION":
                caseClickedBy();
            case "STARTGAME":
                gameStarted = true;
                panel.setNewGameButtonState(false);
                break;
            case "ENDGAME":
                gameStarted = false;
                panel.setNewGameButtonState(true);
                panel.addMsgGui("Game ended by the server.");
                break;
            case "SERVERSTOPPED":
                gameStarted = false;
                connected = false;
                panel.addMsgGui("The server has closed.");
                panel.setNewGameButtonState(true);
                process = null; //Close listening thread
                try {
                    in.close();
                    out.close();
                    sock.close();
                } catch (IOException e) {
                    panel.addMsgGui("Error while closing communications with server.");
                    System.out.println("Error while closing communications with server.");
                    e.printStackTrace();
                }
                break;
            case "LOST":
                lostExploded();
                break;
            case "FINISHGAME":
                finishGamePopUp();
                break;
            case "WIN":
                won();
                break;
            default:
                System.out.println("Error: command from server not understood: " + cmd + "\n");
                panel.addMsgGui("Error: command from server not understood : " + cmd);
        }
    }

    /**
     * Popup to inform player the game is finished.
     */
    private void finishGamePopUp() {
        lost=true;
        JOptionPane.showConfirmDialog(null, "The game is finished. Check scores to see your " +
                        "rank", "End of the game", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * The player won we will show a popup to inform him. And we will handle game states.
     */
    private void won() {
        JOptionPane.showConfirmDialog(null, "Congratulations ! You WON ! "
                , "End of the game", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Action to perform when player exploded by clicking on a mine
     */
    private void lostExploded() {
        lost=true;
        panel.addMsgGui("You lost !");
        JOptionPane.showConfirmDialog(null, "BOOM ! GAME OVER !", "Game Over",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A player was the first to clicked on a case. The server will send the position of the case, what it contains
     * and which player clicked on it. This will be handled here.
     */
    private void caseClickedBy() {
        try{
            int x = in.readInt();
            int y = in.readInt();
            int value = in.readInt();
            int player = in.readInt();
            panel.addMsgGui("Player "+ player+ " clicked at the position (X : "+x+", Y : "+y+") :"+value);
            panel.getCaseXY(x,y).setClickedTrueAndValueAndPlayerId(value, player);
        }catch (IOException e){
            panel.addMsgGui("Error while receiving case click information.");
            e.printStackTrace();
        }
    }

    /**
     * After having receiving the command saying the server will send a message, this function will start waiting for
     * the server's message. As soon as the message is received it will printed on the GUI.
     */
    private void readAndPrintServerMessage() {
        try {
            String msg = in.readUTF();
            panel.addMsgGui(msg);
        } catch (IOException e) {
            System.out.println("Error when receiving server data about 'message'.");
            panel.addMsgGui("Error when receiving server data about 'message'.");
        }
    }

    /**
     * Make possible to know the type of the game : online or local.
     * @return The state of the game. True means that the game is with a server (online)
     */
    public boolean isOnlineGame() {
        return onlineGame;
    }

    /**
     * Set the type of game (online or local)
     * @param onlineGame Boolean, true means the state will be put to online game and false to local.
     */
    public void setOnlineGame(boolean onlineGame) {
        this.onlineGame = onlineGame;
    }

    /**
     * Will send the command and the information of the position (x,y) of the case clicked
     * @param x Integer of the horizontal axe value of the clicked case.
     * @param y Integer of the vertical axe value of the clicked case.
     */
    public void sendClick(int x, int y) {
        try{
            out.writeUTF(Commands.POSITION.name()); //Send the command that describe the information that will follow
            out.writeInt(x);
            out.writeInt(y);
        }catch (IOException e){
            System.out.println("Error while sending click position information (X : "+x+", Y : "+y+").");
            panel.addMsgGui("Error while sending click position information (X : "+x+", Y : "+y+").");
            e.printStackTrace();
        }
    }
}

