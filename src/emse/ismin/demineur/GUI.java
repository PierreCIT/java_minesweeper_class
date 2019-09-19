package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


public class GUI extends JPanel implements ActionListener {
    private Demineur main;
    private JButton butQuit;
    private JButton butRestart;
    private JLabel labWelcom;
    private JLabel labScoreLevel;
    private JPanel northPanel;
    private JPanel gridPannel = new JPanel();
    ;
    private JPanel ButtonSouth;
    private JMenuItem mQuitter;
    private JMenuItem mAbout;
    private JMenuItem mEasy;
    private JMenuItem mMedium;
    private JMenuItem mHard;
    private Case[][] tabCase; //Array that will store the cases in the gridLayer
    private Compteur compteur;

    /**
     * Create the pannels information inside the frame
     *
     * @param main Main of Demineur
     */
    public GUI(Demineur main) {
        this.main = main;
        //Compteur
        compteur = new Compteur();

        setLayout(new BorderLayout());
        northPanel = new JPanel();

        //NorthPanel's label
        labWelcom = new JLabel("Welcome on the connected Minesweeper !");
        labScoreLevel = new JLabel("Score : " + main.score + " , Level : " + main.level, SwingConstants.CENTER);
        northPanel.add(labWelcom);
        northPanel.add(compteur); //Adding compteur to northPanel
        northPanel.add(labScoreLevel);
        labWelcom.setFont(new Font("Papyrus", Font.ITALIC, 12));
        labScoreLevel.setFont(new Font("Papyrus", Font.ITALIC, 12));
        this.add(northPanel, BorderLayout.NORTH);

        setVisible(true);

        placeCases();

        //Button Quit
        butQuit = new JButton("Quit");
        butQuit.setForeground(Color.white);
        butQuit.setBackground(Color.darkGray);
        butQuit.addActionListener(this);
        butQuit.setFont(new Font("Papyrus", Font.ITALIC, 12));

        //Button Restart
        butRestart = new JButton("Restart game");
        butRestart.setForeground(Color.white);
        butRestart.setBackground(Color.darkGray);
        butRestart.addActionListener(this);
        butRestart.setFont(new Font("Papyrus", Font.ITALIC, 12));

        //Button at the bottom of the grid
        ButtonSouth = new JPanel();
        ButtonSouth.add(butRestart);
        ButtonSouth.add(butQuit);
        this.add(ButtonSouth, BorderLayout.SOUTH);

        //Menu bar
        JMenuBar menuBar = new JMenuBar();
        //Menu partie
        JMenu menuPartie = new JMenu("Partie");
        menuBar.add(menuPartie);

        //Button new game
        JMenu mNewParty = new JMenu("New Game");
        //Button Easy,Medium,Hard
        mEasy = new JMenuItem("Easy");
        mNewParty.add(mEasy);
        mEasy.addActionListener(this);
        mMedium = new JMenuItem("Medium");
        mNewParty.add(mMedium);
        mMedium.addActionListener(this);
        mHard = new JMenuItem("Hard");
        mHard.addActionListener(this);
        mNewParty.add(mHard);
        menuPartie.add(mNewParty);


        //Button quit in the "game" menu
        mQuitter = new JMenuItem("Quitter", KeyEvent.VK_Q);
        mQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        menuPartie.add(mQuitter);
        mQuitter.setToolTipText("The end");
        mQuitter.addActionListener(this);


        //Glue to put the 'About' on the right
        menuBar.add(Box.createGlue());
        //Help item
        JMenu mHelp = new JMenu("Help");
        menuBar.add(mHelp);
        mAbout = new JMenuItem("About", KeyEvent.VK_A);
        mAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        mAbout.setToolTipText("About the program");
        mHelp.add(mAbout);
        mAbout.addActionListener(this);
        main.setJMenuBar(menuBar);
    }

    /**
     * Dialog window to quit the game
     */
    public void quit() {
        int rep = JOptionPane.showConfirmDialog(null, "Are you sure ?", "Close confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void about() {
        JOptionPane.showConfirmDialog(null, "This is awesome !!!",
                "About", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Listener of events in the pannel
     *
     * @param e ActionEvent variable
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == butQuit) {
            System.out.println("Closing... See you soon !");
            quit();
        } else if (e.getSource() == butRestart) {
            main.getChamp().newParty(main.level);
            newParty();
        } else if (e.getSource() == mQuitter) {
            quit();
        } else if (e.getSource() == mAbout) {
            about();
        } else if (e.getSource() == mEasy) {
            main.getChamp().newParty(Level.EASY);
            newParty(Level.EASY);
        } else if (e.getSource() == mMedium) {
            main.getChamp().newParty(Level.MEDIUM);
            newParty(Level.MEDIUM);
        } else if (e.getSource() == mHard) {
            main.getChamp().newParty(Level.HARD);
            newParty(Level.HARD);
        }
    }

    private void placeCases() {
        gridPannel.setLayout(new GridLayout(main.getChamp().getDimX(), main.getChamp().getDimY()));
        add(gridPannel, BorderLayout.CENTER);

        tabCase = new Case[main.getChamp().getDimX()][main.getChamp().getDimY()];
        for (int x = 0; x < main.getChamp().getDimX(); x++) {
            for (int y = 0; y < main.getChamp().getDimY(); y++) {
                tabCase[x][y] = new Case(x, y, main);
                gridPannel.add(tabCase[x][y]);
            }
        }
    }

    /**
     * Asked to all cases to start to reinitialize. (The mine position is not changed here)
     */
    private void newParty() {
        for (int x = 0; x < main.getChamp().getDimX(); x++) {
            for (int y = 0; y < main.getChamp().getDimY(); y++) {
                tabCase[x][y].newParty();
            }
        }
        compteur.stopCpt();
        main.newGame();
    }

    /**
     * Asked to all cases to start a new party, and reposition the mines.
     * Take as input level when the new game is to be launch with a new level.
     */
    public void newParty(Level level) {
        gridPannel.removeAll();
        main.getChamp().newParty(level);
        placeCases();
        main.pack();
        compteur.stopCpt();
        main.newGame();
    }

    public Compteur getCompteur() {
        return compteur;
    }

    /**
     * Will see if the adjacent cases are of the same value of the one clicked and if it is set
     * these cases to clicked through the method setClickedTrue.
     * The given case at the position x,y is already set to be clicked.
     */
    public void adjacentSameValue(int x, int y){
        int borneInfX = x == 0 ? 0 : -1;
        int borneSupX = x == main.getChamp().getDimX() - 1 ? 0 : 1;
        int borneInfY = y == 0 ? 0 : -1;
        int borneSupY = y == main.getChamp().getDimY()  - 1 ? 0 : 1;

        for (int i = borneInfX; i <= borneSupX; i++) {
            for (int k = borneInfY; k <= borneSupY; k++) {
                if (!(i == 0 && k == 0)) { //We don't count the mine itself
                    if (!tabCase[x+i][y+k].getClicked()) { //If the case is not already clicked
                        tabCase[x+i][y+k].setClickedTrue();
                        if(main.getChamp().numberMinesSurrounding(x+i,y+k)==0){
                            adjacentSameValue(x+i,y+k);
                        }
                    }
                }
            }
        }
    }

}
