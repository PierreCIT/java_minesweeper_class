package emse.ismin.demineur;

import java.util.Random;


/**
 * @author Pierre
 * Class which will create the field of the deminor
 */

public class Champ {
    private static final int DIMEASY = 3;
    private static final int DIMMEDIUM = 20;
    private static final int DIMHARD = 30;
    private static final int NBMINESEASY = 1;
    private static final int NBMINESMEDIUM = 35;
    private static final int NBMINESHARD = 70;
    public Level level;
    private boolean[][] mines;
    private int DimX;
    private int DimY;
    private Random alea = new Random();
    private int nbMines = 0;

    /**
     * Default constructor that will initialize a game in the EASY mode
     */
    public Champ() {
        this.level = Level.EASY;
        createChamp(DIMEASY, DIMEASY);
    }

    /**
     * Constructor that takes as input the level of difficulty
     *
     * @param level Enum level that can be EASY, MEDIUM, HARD
     */
    public Champ(Level level) {
        this.level = level;
        createChamp(0, 0);
    }

    /**
     * Constructor that will tkae as input the dimension of the champ to build (it is custom level difficulty)
     *
     * @param DIM1 X dimension
     * @param DIM2 Y dimension
     */
    public Champ(int DIM1, int DIM2) {
        this.level = Level.CUSTOM;
        createChamp(DIM1, DIM2);
    }

    /**
     * Create the mine field according to the difficulty level or the size given with param
     *
     * @param DIM1 X dimension
     * @param DIM2 Y dimension
     */
    public void createChamp(int DIM1, int DIM2) {
        if (level == Level.CUSTOM) {
            this.mines = new boolean[DIM1][DIM2];
            nbMines = (DIM1 + DIM2) / 10;
            placesMines(nbMines);
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

    private int nbMinesForLevel(){
        if(level==Level.EASY){
            nbMines=NBMINESEASY;
            return nbMines;
        }else if(level==Level.MEDIUM){
            nbMines = NBMINESMEDIUM;
            return nbMines;
        }else{
            nbMines=NBMINESHARD;
            return nbMines;
        }
    }

    public int getNbMines(){
        return nbMines;
    }

    public void newParty(Level level){
        if(level==this.level){
            placesMines(nbMinesForLevel());
            this.print();
        }else{
            System.out.println("");
            this.level=level;
            createChamp(0,0);
            placesMines(nbMinesForLevel());
            this.print();
        }
    }
    /**
     * @param x
     * @param y
     * @return Number of mines surrounfing the position given by x and y
     */
    public int numberMinesSurrounding(int x, int y) {
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
     */
    public void placesMines(int nbMines) {
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
     * R
     *
     * @return Champ dimensions
     */
    public int[] getDimChamp() {
        int[] results = new int[2];
        results[0] = DimX;
        results[1] = DimY;
        return results;
    }

    /**
     * Return the value inside the champ 0 to 8 and x for a mines.
     *
     * @param x Position to ask values from
     * @param y Position to ask values from
     * @return A string value inside the champ 0 to 8 and x for a mines.
     */
    public String getValeurChamp(int x, int y) {
        if (mines[x][y]) {
            return "9";
        } else {
            return String.valueOf(numberMinesSurrounding(x, y));
        }

    }

    public boolean isMine(int x, int y) {
        return mines[x][y];
    }

    public int getDimX(){
        return DimX;
    }

    public int getDimY(){
        return DimY;
    }
    /**
     * Convert the mines position to an String with x as the mine position
     *
     * @return The string containing the mines field
     */
    public void print() {
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

}
