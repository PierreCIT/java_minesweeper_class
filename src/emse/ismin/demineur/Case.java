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
 * A class that draw and acualize a Jpanel when clicked
 */
class Case extends JPanel implements MouseListener {
    private final static int DIM = 50;
    private boolean clicked = false;
    private int x;
    private int y;
    private Demineur demin;
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


    public Case(int x, int y, Demineur demin) {
        this.x = x;
        this.y = y;
        this.demin = demin;
        setPreferredSize(new Dimension(DIM, DIM)); //Size of the case
        addMouseListener(this);
    }

    /**
     * The cas drawing
     */
    @Override
    public void paintComponent(Graphics gc) {
        super.paintComponent(gc); //Inherit mother method that clean the previous drawing
        if (!clicked) {
            gc.setColor(new Color(100, 100, 100));
            gc.fillRect(1, 1, getWidth(), getHeight());
        } else {
            if (!demin.isOnlineGame()) { //Behavior when in local mode
                if (demin.getChamp().isMine(x, y)) {
                    try {
                        BufferedImage image = ImageIO.read(new File("img/bomb.png"));
                        gc.drawImage(image, 3, 3, getWidth() - 3, getHeight() - 3, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    gc.setColor(new Color(handleColor(Integer.parseInt(demin.getChamp().getValeurChamp(x, y)))));
                    gc.fillRect(0, 0, getWidth(), getHeight());
                    gc.setColor(new Color(0, 0, 0));
                    if (Integer.parseInt(demin.getChamp().getValeurChamp(x, y)) != 0) {
                        gc.drawString(demin.getChamp().getValeurChamp(x, y), getHeight() / 2, getWidth() / 2);
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

    public int handleColor(int nbMinesSurrounding) {
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
    public void newParty() {
        clicked = false;
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * Handle mouse
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!demin.isOnlineGame()) { //Behavior when in local game mode.
            if (!demin.isLost()) {
                if (!clicked) {
                    demin.setNbCaseClicked(demin.getNbCaseClicked() + 1);
                }
                clicked = true;
                if (!demin.isStarted()) {
                    demin.getGui().getCompteur().startCpt();
                    demin.setStarted(true);
                }
                repaint(); //Force the call to paintComponents (default behavior)

                if (demin.getChamp().isMine(x, y)) {
                    demin.setLost(true);
                    demin.getGui().getCompteur().stopCpt();
                    int rep = JOptionPane.showConfirmDialog(null, "BOOM ! GAME OVER ! Try " +
                                    "again ! ", "Game Over",
                            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (rep == JOptionPane.YES_OPTION) {
                        demin.getGui().newGame(demin.level);
                    }
                } else {
                    //If the case clicked is empty without close bombs then we call the function to show all adjacentValues
                    if (demin.getChamp().getValeurChamp(x, y).equals("0")) {
                        demin.getGui().adjacentSameValue(x, y);
                    }
                }
                if (demin.isWin()) {
                    demin.getGui().getCompteur().stopCpt();
                    int rep = JOptionPane.showConfirmDialog(null, "Congratulations ! You WIN " +
                                    "!!! \nYour score is " + demin.getGui().getCompteur().getScore() +
                                    "\nWould you like to restart ?", "Congratulations",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (rep == JOptionPane.YES_OPTION) {
                        demin.getGui().newGame(demin.level);
                    }
                    demin.saveScore();
                }
            }
        } else { //Behavior in online game mode.
            if (!demin.isLost()) {
                demin.sendClick(x, y);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * Will always set the clicked attribute to true set the number of clicked cases to plus 1
     * And ask for a repaint.
     */
    public void setClickedTrue() {
        clicked = true;
        demin.setNbCaseClicked(demin.getNbCaseClicked() + 1);
        repaint();
    }

    /**
     * Return if the case was clicked.
     */
    public boolean getClicked() {
        return clicked;
    }

    synchronized public void setClickedTrueAndValueAndPlayerId(int value, int playerId) {
        clicked = true;
        this.value = value;
        this.playerIdClicked = playerId;
        repaint();
    }
}
