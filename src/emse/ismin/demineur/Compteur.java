package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.text.AttributedCharacterIterator;

public class Compteur extends JPanel implements Runnable {
    private Thread processScore; //Thread associated to the class.
    private int score = 0;


    Compteur() {
        setPreferredSize(new Dimension(35, 25)); //Size of the case
    }

    @Override
    public void run() {
        while (processScore != null) {
            try {
                Thread.sleep(1000); //Sleep for 1s
                if(processScore != null){
                    score++;
                    repaint();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        gc.setColor(new Color(0, 0, 0));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setColor(new Color(200, 0, 0));
        gc.setFont(new Font("default", Font.BOLD, 16));
        gc.drawString(String.valueOf(score), (getWidth() / 2) - 2, (getHeight() / 2) + 5);
    }

    /**
     * Initialize the score value, create a new thread and run it
     */
    public void startCpt(){
        score = 0;
        repaint();
        processScore = new Thread(this);
        processScore.start();
    }

    /**
     * Set the score to zero and repaint to print the new value
     */
    public void stopCpt() {
        processScore = null;
    }

    public int getScore(){
        return score;
    }
}
