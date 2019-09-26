package emse.ismin.demineur;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Pierre
 * <p>Projet de démineur connecté</p>
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

    /**
     * Constructor of the Demineur which will initialize the game
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
     * Main du programme de démineur et affichage de 2 champs de mines
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("Bienvenue dans le demineur");
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
     * Rest the game and set the level to the leveled passed in parameters
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
        Path path = Paths.get(FILENAME);

        if (!Files.exists(path)) {
            for (int i = 0; i < Level.values().length; i++) {
            }
        }
    }

    public void connectServer(String ip, int port, String nickname) {
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
            JOptionPane.showConfirmDialog(null, "Impossible to connect to " + ip + ":" + port + " with nickname:" + nickname, "Close confirmation",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Impossible to connect to " + ip + ":" + port + " with nickname:" + nickname);
            JOptionPane.showConfirmDialog(null, "Impossible to connect to " + ip + ":" + port + " with nickname:" + nickname, "Close confirmation",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void disconnect() {
        connected = false;
        try {
            in.close();
            out.close();
            sock.close();
            System.out.println("Disconnected from server.\n");
            panel.coDecoButtonChangeText();
            process = null; //We kill the process that was waiting for messages from the server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                e.printStackTrace();
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
            case "STARTGAME":
                gameStarted = true;
                panel.setNewGameButtonState(false);
                break;
            case "ENDGAME":
                gameStarted = false;
                break;
            case "SERVERSTOPPED":
                gameStarted = false;
                panel.addMsgGui("The server has closed.");
                panel.setNewGameButtonState(true);
                process=null; //Close listening thread
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
            default:
                System.out.println("Error: command from server not understood: " + cmd + "\n");
                panel.addMsgGui("Error: command from server not understood : " + cmd);
        }
    }

    private void readAndPrintServerMessage() {
        try {
            String msg = in.readUTF();
            panel.addMsgGui(msg);
        } catch (IOException e) {
            System.out.println("Error when receiving server data about 'message'.");
            panel.addMsgGui("Error when receiving server data about 'message'.");
        }
    }
}

