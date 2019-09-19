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

    public boolean isLost() {
        return lost;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public void setNbCaseClicked(int nbCaseClicked) {
        this.nbCaseClicked = nbCaseClicked;
    }

    public int getNbCaseClicked() {
        return this.nbCaseClicked;
    }

    public boolean isWin() {
        return getNbCaseClicked() + getChamp().getNbMines() == getChamp().getDimY() * getChamp().getDimX();
    }

    public void newGame(){
        setStarted(false);
        setLost(false);
        setNbCaseClicked(0);
    }

    public void saveScore() {
        Path path = Paths.get(FILENAME);

        if(!Files.exists(path)){
            for(int i=0; i<Level.values().length;i++){
            }
        }
    }
}

