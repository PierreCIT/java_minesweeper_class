package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.util.Random;


/**
 * Class which will create the field of the MinesWeeper
 */
class Field {
    private static final int DIMEASY = 5;
    private static final int DIMMEDIUM = 20;
    private static final int DIMHARD = 30;
    private static final int NBMINESEASY = 5;
    private static final int NBMINESMEDIUM = 35;
    private static final int NBMINESHARD = 70;
    private Level level;
    private boolean[][] mines;
    private int DimX;
    private int DimY;
    private Random alea = new Random();
    private int nbMines = 0;
    private int dimXCustom = 5;
    private int dimYCustom = 5;

    /**
     * Constructor that takes as input the level of difficulty
     *
     * @param level Enum level that can be EASY, MEDIUM, HARD
     */
    Field(Level level) {
        this.level = level;
        createChamp(0, 0);
    }

    /**
     * Create the mine field according to the difficulty level or the size given with param
     *
     * @param DIM1 X dimension
     * @param DIM2 Y dimension
     */
    private void createChamp(int DIM1, int DIM2) {
        if (level == Level.CUSTOM) {
            this.mines = new boolean[DIM1][DIM2];
            placesMines(nbMinesForLevel());
            DimX = DIM1;
            DimY = DIM2;
        } else if (level == Level.EASY) {
            this.mines = new boolean[DIMEASY][DIMEASY];
            placesMines(nbMinesForLevel());
            DimX = DIMEASY;
            DimY = DIMEASY;
        } else if (level == Level.MEDIUM) {
            this.mines = new boolean[DIMMEDIUM][DIMMEDIUM];
            placesMines(nbMinesForLevel());
            DimX = DIMMEDIUM;
            DimY = DIMMEDIUM;
        } else {
            this.mines = new boolean[DIMHARD][DIMHARD];
            placesMines(nbMinesForLevel());
            DimX = DIMHARD;
            DimY = DIMHARD;
        }
    }


    /**
     * Determines the number of mines to put in the field regarding the level of difficulty
     *
     * @return Integer representing the number of mine to put
     */
    private int nbMinesForLevel() {
        if (level == Level.EASY) {
            nbMines = NBMINESEASY;
            return nbMines;
        } else if (level == Level.MEDIUM) {
            nbMines = NBMINESMEDIUM;
            return nbMines;
        } else if (level == Level.CUSTOM) {
            nbMines = (dimXCustom * dimYCustom) / 10;
            return nbMines;
        } else if (level == Level.HARD) {
            nbMines = NBMINESHARD;
            return nbMines;
        } else {
            System.out.println("Error number of mines for an unknown type of level requested.");
            return 5;
        }
    }

    /**
     * Get the number of mines
     *
     * @return Integer of the number of mines
     */
    int getNbMines() {
        return nbMines;
    }

    void newGame(Level level) {
        if (level == this.level) {
            placesMines(nbMinesForLevel()); // We just have to place new mines and remove previous ones
            this.print();
        } else {
            System.out.println();
            this.level = level;
            if (this.level == Level.CUSTOM) {
                getDimsOptionPanel();
                createChamp(dimXCustom, dimYCustom);
            } else {
                createChamp(0, 0);
            }
            this.print();
        }
    }

    /**
     * Create a new custom game when playing online
     *
     * @param level      Level, the level of the game
     * @param dimXServer Integer, the dimension of the X axis
     * @param dimYServer Integer, the dimension of the Y axis
     */
    void newGame(Level level, int dimXServer, int dimYServer) {
        dimXCustom = dimXServer;
        dimYCustom = dimYServer;
        this.level = level;
        if (level == Level.CUSTOM) { //This function should only be called in the case of a Custom level
            createChamp(dimXServer, dimYServer);
        } else { //Then we should apply the usual newGame creation
            createChamp(0, 0);
        }
        this.print();
    }

    /**
     * Option panel that will get the customs dimensions of the game and will create champ with those parameters
     */
    void getDimsOptionPanel() {
        JPanel panelOptionDims = new JPanel();
        panelOptionDims.setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        JPanel southPanel = new JPanel();
        northPanel.add(new JLabel("Enter X axis dims [3:100] : "));
        JTextField XAxis = new JTextField(10);
        northPanel.add(XAxis);
        southPanel.add(new JLabel("Enter Y axis dims [3:100] : "));
        JTextField YAxis = new JTextField(10);
        southPanel.add(YAxis);
        panelOptionDims.add(northPanel, BorderLayout.NORTH);
        panelOptionDims.add(southPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(null, panelOptionDims, "Enter custom dimensions",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            dimXCustom = Integer.parseInt(XAxis.getText());
            dimYCustom = Integer.parseInt(YAxis.getText());
            if (dimXCustom > 100 || dimXCustom < 3) {
                dimXCustom = 3;
            }
            if (dimYCustom > 100 || dimYCustom < 3) {
                dimYCustom = 3;
            }
        } else { //Set a default size if the player cancel
            dimXCustom = DIMEASY;
            dimYCustom = DIMEASY;
        }

    }

    /**
     * Return the number of mines surrounding a position (excluding the position itself)
     *
     * @param x Integer of the X position of the case
     * @param y Integer of the Y position of the case
     * @return Number of mines surrounding the position given by x and y
     */
    int numberMinesSurrounding(int x, int y) {
        int nbMines = 0;
        int borneInfX = x == 0 ? 0 : -1;
        int borneSupX = x == this.mines.length - 1 ? 0 : 1;
        int borneInfY = y == 0 ? 0 : -1;
        int borneSupY = y == this.mines[0].length - 1 ? 0 : 1;

        for (int i = borneInfX; i <= borneSupX; i++) {
            for (int k = borneInfY; k <= borneSupY; k++) {
                if (!(i == 0 && k == 0)) { //We don't count the mine itself
                    if (this.mines[x + i][y + k]) {
                        nbMines++;
                    }
                }
            }
        }
        return nbMines;
    }

    /**
     * Select the position of a mine randomly
     *
     * @param nbMines Integer number of mines to place in the field
     */
    private void placesMines(int nbMines) {
        for (int x = 0; x < this.mines.length; x++) {
            for (int y = 0; y < this.mines[0].length; y++) {
                this.mines[x][y] = false;
            }
        }
        for (int i = 0; i < nbMines; i++) {
            boolean test = false;
            do {
                int x = alea.nextInt(this.mines.length);
                int y = alea.nextInt(this.mines[0].length);
                if (!this.mines[x][y]) {
                    this.mines[x][y] = true;
                    test = true;
                }
            } while (!test);
        }
    }

    /**
     * Return the value inside the champ 0 to 8 and x for a mines.
     *
     * @param x Position to ask values from
     * @param y Position to ask values from
     * @return A string value inside the champ 0 to 8 and x for a mines.
     */
    String getValueField(int x, int y) {
        if (mines[x][y]) {
            return "9";
        } else {
            return String.valueOf(numberMinesSurrounding(x, y));
        }

    }

    /**
     * Return a boolean answering the question is there a mine.
     *
     * @param x Integer x position of the mine
     * @param y Integer y position of the mine
     * @return Boolean true if there is a mine false otherwise.
     */
    boolean isMine(int x, int y) {
        return mines[x][y];
    }

    /**
     * Get the X dimension of the mine field
     *
     * @return Integer
     */
    int getDimX() {
        return DimX;
    }

    /**
     * Get the Y dimension of the mine field
     *
     * @return Integer
     */
    int getDimY() {
        return DimY;
    }

    /**
     * Print the mine position in the console.
     */
    void print() {
        for (int x = 0; x < this.mines.length; x++) {
            for (int y = 0; y < this.mines[0].length; y++) {
                if (this.mines[x][y]) {
                    System.out.print("x");
                } else {
                    System.out.print(numberMinesSurrounding(x, y));
                }
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }

    /**
     * Get the information of the X axis send by the server or local user if in local
     *
     * @return Integer of the X axis
     */
    int getDimXCustom() {
        return dimXCustom;
    }

    /**
     * Get the information of the Y axis send by the server or local user if in local
     *
     * @return Integer of the Y axis
     */
    int getDimYCustom() {
        return dimYCustom;
    }
}
