package emse.ismin.demineur;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Pierre
 * <p>Projet de démineur connecté</p>
 */

public class Demineur extends JFrame {
    public final static String FILENAME = "score.dat";
    public int score = 20;
    public Level level = Level.EASY;
    private Champ champMines = new Champ(level);
    private boolean started = false;
    private GUI panel;
    private boolean lost;
    private int nbCaseClicked = 0;

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
     * @param started A boolean if true the game is started and the opposite otherwise.
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Return the interface object
     * @return
     */
    public GUI getGui() {
        return this.panel;
    }

    /**
     *
     * @return The game state.
      */
    public boolean isLost() {
        return lost;
    }

    /**
     * Set the game state to the given value. True means lost false means the game keeps going.
     * @param lost A boolean representing the game state (alive or dead).
     */
    public void setLost(boolean lost) {
        this.lost = lost;
    }

    /**
     * Set the attribute variable which represents the number of clicked cases to the given number.
     * @param nbCaseClicked Value to set the number of cases clicked to.
     */
    public void setNbCaseClicked(int nbCaseClicked) {
        this.nbCaseClicked = nbCaseClicked;
    }

    /**
     *
     * @return the number of cases clicked.
     */
    public int getNbCaseClicked() {
        return this.nbCaseClicked;
    }

    /**
     * By computing the number of discovered cases and comparing to the total number of cases and mines
     * tests if the game is win.
     * @return true if the game is won, false otherwise.
     */
    public boolean isWin() {
        return getNbCaseClicked() + getChamp().getNbMines() == getChamp().getDimY() * getChamp().getDimX();
    }

    /**
     * Reset the game when the level has to be same as the previous one in the next game.
     */
    public void newGame(){
        setStarted(false);
        setLost(false);
        setNbCaseClicked(0);
    }

    /**
     * Rest the game and set the level to the leveled passed in parameters
     * @param level Level of the new game.
     */
    public void newGame(Level level){
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

        if(!Files.exists(path)){
            for(int i=0; i<Level.values().length;i++){
            }
        }
    }
}

