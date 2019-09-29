package emse.ismin.demineur;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A class that draw and actualize a JPanel when clicked.
 * It corresponds to one case of the mine field.
 */
class Case extends JPanel implements MouseListener {
    private final static int DIM = 50;
    private boolean clicked = false;
    private int x;
    private int y;
    private MinesWeeper minesWeeperMain;
    private int value = 0; //Only used when playing online. It will contain the value to show or mines if -1
    private int playerIdClicked = 0; //Only used when playing online. It will contain the id of the player who clicked

    private final static int GRAY = 0x9E9E9E; //0
    private final static int BLUE = 0x1547EB; //1
    private final static int DARKGREEN = 0x78C324; //2
    private final static int YELLOW = 0xC7E41C; //3
    private final static int DARKYELLOW = 0xE3CF15; //4
    private final static int ORANGE = 0xF4BD03; //5
    private final static int DARKORANGE = 0xF49E03; //6
    private final static int RED = 0xF43803; //7
    private final static int FLASHRED = 0xFA0303;//8
    private final static int BLACK = 0x333333;//9


    Case(int x, int y, MinesWeeper minesWeeperMain) {
        this.x = x;
        this.y = y;
        this.minesWeeperMain = minesWeeperMain;
        setPreferredSize(new Dimension(DIM, DIM)); //Size of the case
        addMouseListener(this);
    }

    /**
     * How the case should be displayed depending on the state
     *
     * @param gc Default parameter
     */
    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc); //Inherit mother method that clean the previous drawing
        if (!clicked) {
            gc.setColor(new Color(100, 100, 100));
            gc.fillRect(1, 1, getWidth(), getHeight());
        } else {
            if (!minesWeeperMain.isOnlineGame()) { //Behavior when in local mode
                if (minesWeeperMain.getChamp().isMine(x, y)) {
                    try {
                        BufferedImage image = ImageIO.read(new File("img/bomb.png"));
                        gc.drawImage(image, 3, 3, getWidth() - 3, getHeight() - 3, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    gc.setColor(new Color(handleColor(Integer.parseInt(minesWeeperMain.getChamp().getValueField(x, y)))));
                    gc.fillRect(0, 0, getWidth(), getHeight());
                    gc.setColor(new Color(0, 0, 0));
                    if (Integer.parseInt(minesWeeperMain.getChamp().getValueField(x, y)) != 0) {
                        gc.drawString(minesWeeperMain.getChamp().getValueField(x, y), getHeight() / 2, getWidth() / 2);
                    }
                }
            } else { //Behavior when in online mode.
                if (value == -1) { //If the player clicked on a bomb
                    try {
                        gc.setColor(new Color(handleColor(playerIdClicked)));
                        gc.fillRect(0, 0, getWidth(), getHeight());
                        BufferedImage image = ImageIO.read(new File("img/bomb.png"));
                        gc.drawImage(image, 3, 3, getWidth() - 3, getHeight() - 3, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //The color of the background depends on the playerId who clicked the case
                    gc.setColor(new Color(handleColor(playerIdClicked)));
                    gc.fillRect(0, 0, getWidth(), getHeight());
                    gc.setFont(new Font("default", Font.BOLD, 16));
                    gc.setColor(new Color(0, 0, 0));
                    if (value != 0) {
                        gc.drawString(String.valueOf(value), getHeight() / 2, getWidth() / 2);
                    }
                }
            }
        }
    }

    /**
     * Will give a color corresponding to a value
     *
     * @param nbMinesSurrounding Value to use to get the color
     * @return A constant which refers to a color
     */
    int handleColor(int nbMinesSurrounding) {
        int resultColor;
        switch (nbMinesSurrounding) {
            case 0:
                resultColor = GRAY;
                break;
            case 1:
                resultColor = BLUE;
                break;
            case 2:
                resultColor = DARKGREEN;
                break;
            case 3:
                resultColor = YELLOW;
                break;
            case 4:
                resultColor = DARKYELLOW;
                break;
            case 5:
                resultColor = ORANGE;
                break;
            case 6:
                resultColor = DARKORANGE;
                break;
            case 7:
                resultColor = RED;
                break;
            case 8:
                resultColor = FLASHRED;
                break;
            default:
                resultColor = BLACK;
                break;
        }
        return resultColor;
    }

    /**
     * Set the value of clicked to false and repaint the component.
     */
    void newGameCase() {
        clicked = false;
        repaint();
    }

    /**
     * Event
     *
     * @param e Event
     */
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * Handle mouse
     *
     * @param e event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!minesWeeperMain.isOnlineGame()) { //Behavior when in local game mode.
            if (!minesWeeperMain.isLost()) {
                if (!clicked) {
                    minesWeeperMain.setNbCaseClicked(minesWeeperMain.getNbCaseClicked() + 1);
                }
                clicked = true;
                if (!minesWeeperMain.isStarted()) {
                    minesWeeperMain.getGui().getStopWatch().startCpt();
                    minesWeeperMain.setStarted(true);
                }
                repaint(); //Force the call to paintComponents (default behavior)

                if (minesWeeperMain.getChamp().isMine(x, y)) {
                    minesWeeperMain.setLost(true);
                    minesWeeperMain.getGui().getStopWatch().stopCpt();
                    minesWeeperMain.saveScore(minesWeeperMain.getGui().getStopWatch().getScore());
                    int rep = JOptionPane.showConfirmDialog(null, "BOOM ! GAME OVER ! Try " +
                                    "again ! ", "Game Over",
                            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (rep == JOptionPane.YES_OPTION) {
                        minesWeeperMain.getGui().newGame(minesWeeperMain.level);
                    }
                } else {
                    //If the case clicked is empty without close bombs then we call the function to show all adjacentValues
                    if (minesWeeperMain.getChamp().getValueField(x, y).equals("0")) {
                        minesWeeperMain.getGui().adjacentSameValue(x, y);
                    }
                }
                if (minesWeeperMain.isWin()) {
                    minesWeeperMain.getGui().getStopWatch().stopCpt();
                    int rep = JOptionPane.showConfirmDialog(null, "Congratulations ! You WIN " +
                                    "!!! \nYour score is " + minesWeeperMain.getGui().getStopWatch().getScore() +
                                    "\nWould you like to restart ?", "Congratulations",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (rep == JOptionPane.YES_OPTION) {
                        minesWeeperMain.getGui().newGame(minesWeeperMain.level);
                    }
                    minesWeeperMain.saveScore(minesWeeperMain.getGui().getStopWatch().getScore());
                }
            }
        } else { //Behavior in online game mode.
            if (!minesWeeperMain.isLost()) {
                minesWeeperMain.sendClick(x, y);
            }
        }
    }

    /**
     * Event
     *
     * @param e Event
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Event
     *
     * @param e Event
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Event
     *
     * @param e Event
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * Will always set the clicked attribute to true set the number of clicked cases to plus 1
     * And ask for a repaint.
     */
    void setClickedTrue() {
        clicked = true;
        minesWeeperMain.setNbCaseClicked(minesWeeperMain.getNbCaseClicked() + 1);
        repaint();
    }

    /**
     * Return if the case was clicked.
     *
     * @return Get the information that the case was clicked
     */
    boolean getClicked() {
        return clicked;
    }

    /**
     * Set the fact that the case was clicked, by which player and what the case contains
     *
     * @param value    Integer of what the case contains (between -1 and 9)
     * @param playerId Integer of the Id of the player who clicked.
     */
    synchronized void setClickedTrueAndValueAndPlayerId(int value, int playerId) {
        clicked = true;
        this.value = value;
        this.playerIdClicked = playerId;
        repaint();
    }
}
