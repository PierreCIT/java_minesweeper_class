package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Class that contains the MineWeeper interface. It handles online and local game mode.
 */
public class GUI extends JPanel implements ActionListener {
    private MinesWeeper main;
    private JButton butQuit;
    private JButton butRestart;
    private JLabel labelLevel;
    private JPanel gridPanel = new JPanel();

    private JMenuItem mQuitter;
    private JMenuItem mAbout;
    private JMenuItem mEasy;
    private JMenuItem mMedium;
    private JMenuItem mHard;
    private JMenuItem mCustom;

    private Case[][] tabCase; //Array that will store the cases in the gridLayer
    private StopWatch stopWatch;

    //Connection features
    private TextField ipTF; //IP server to connect to
    private TextField portTF; //Server's port to connect to
    private TextField nicknameTF; //Player's nickname
    private JButton coDiscoButton; // Button that will be used to either connect or disconnect from server
    private JPanel connectPanel; //Panel that will contain connection components
    private TextArea msgServer = new TextArea(5, 35); //Text area in which messages from the server will be printed
    private JTextField inputChatField = new JTextField(20); //Input message for the chat.

    /**
     * Create the panels information inside the frame
     *
     * @param main MinesWeeper class which is the main of MineWeeper's game
     */
    GUI(MinesWeeper main) {
        this.main = main;
        //StopWatch
        stopWatch = new StopWatch();

        setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        JPanel northPanelLabels = new JPanel();

        //NorthPanel's label
        JLabel labWelcome = new JLabel("Welcome on the connected Minesweeper !");
        labelLevel = new JLabel("Level : " + main.level, SwingConstants.CENTER);
        northPanel.setLayout(new BorderLayout());
        northPanelLabels.add(labWelcome);
        northPanelLabels.add(stopWatch); //Adding stopwatch to northPanel
        northPanelLabels.add(labelLevel);
        northPanel.add(northPanelLabels, BorderLayout.NORTH);
        labWelcome.setFont(new Font("Papyrus", Font.ITALIC, 12));
        labelLevel.setFont(new Font("Papyrus", Font.ITALIC, 12));

        //Connection features
        connectPanel = new JPanel();
        ipTF = new TextField(main.ipDefault);
        portTF = new TextField(String.valueOf(main.portDefault));
        nicknameTF = new TextField("nickname", 10);
        coDiscoButton = new JButton("  Connect  ");
        coDiscoButton.addActionListener(this);
        connectPanel.add(ipTF);
        connectPanel.add(portTF);
        connectPanel.add(nicknameTF);
        connectPanel.add(coDiscoButton);
        northPanel.add(connectPanel, BorderLayout.SOUTH);


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
        //Game state features
        JPanel buttonSouth = new JPanel();
        buttonSouth.setLayout(new BorderLayout());
        msgServer.setEditable(false);
        inputChatField.addActionListener(this);
        buttonSouth.add(msgServer, BorderLayout.WEST);
        buttonSouth.add(butRestart, BorderLayout.CENTER);
        buttonSouth.add(butQuit, BorderLayout.EAST);
        buttonSouth.add(inputChatField, BorderLayout.SOUTH);
        this.add(buttonSouth, BorderLayout.SOUTH);

        //Menu bar
        JMenuBar menuBar = new JMenuBar();
        //Game menu
        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);

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
        mCustom = new JMenuItem("Custom");
        mCustom.addActionListener(this);
        mNewParty.add(mCustom);
        gameMenu.add(mNewParty);


        //Button quit in the "game" menu
        mQuitter = new JMenuItem("Leave", KeyEvent.VK_Q);
        mQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        gameMenu.add(mQuitter);
        mQuitter.setToolTipText("The end");
        mQuitter.addActionListener(this);


        //Glue to put the 'About' on the right
        menuBar.add(Box.createGlue());
        //Help item
        JMenu mHelp = new JMenu("Help");
        menuBar.add(mHelp);
        mAbout = new JMenuItem("About", KeyEvent.VK_A);
        mAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
        mAbout.setToolTipText("About the program");
        mHelp.add(mAbout);
        mAbout.addActionListener(this);
        main.setJMenuBar(menuBar);
    }

    /**
     * Get the label that describes the level that must contain "Label : *LEVEL*"
     *
     * @return A JLabel object
     */
    JLabel getLabelLevel() {
        return labelLevel;
    }

    /**
     * Dialog window to quit the game
     */
    private void quit() {
        int rep = JOptionPane.showConfirmDialog(null, "Are you sure ?", "Close confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            System.out.println("Closing... See you soon !");
            System.exit(0);
        }
    }

    private void about() {
        JOptionPane.showConfirmDialog(null,"It is a school project at the 'Mines Saint-Etienne' cursus ISMIN.\n" +
                        "It includes regular MinesWeeper rules but also an online version when connected\n" +
                        "to a server. The server can be launch in local and play multiplayer on the same network.\n" +
                        "Game created by Pierre Seite.",
                "About", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == butQuit) {
            if (main.isOnlineGame())
                main.disconnect();
            quit();
        } else if (e.getSource() == butRestart) {
            main.getField().newGame(main.level);
            newGame();
        } else if (e.getSource() == mQuitter) {
            if (main.isOnlineGame())
                main.disconnect();
            quit();
        } else if (e.getSource() == mAbout) {
            about();
        } else if (e.getSource() == mEasy) {
            main.getField().newGame(Level.EASY);
            newGame(Level.EASY);
        } else if (e.getSource() == mMedium) {
            main.getField().newGame(Level.MEDIUM);
            newGame(Level.MEDIUM);
        } else if (e.getSource() == mHard) {
            main.getField().newGame(Level.HARD);
            newGame(Level.HARD);
        } else if (e.getSource() == mCustom) {
            main.getField().newGame(Level.CUSTOM);
            newGame(Level.CUSTOM);
        } else if (e.getSource() == coDiscoButton)
            if (!main.connected) {
                main.connectServer(ipTF.getText(), Integer.parseInt(portTF.getText()), nicknameTF.getText());
            } else {
                main.disconnect();
                stopWatch.stopCpt();
                playerIdUpdate(0);
            }
        else if (e.getSource() == inputChatField) {
            if (main.isOnlineGame()) {
                main.sendMessageChat(inputChatField.getText());
                inputChatField.setText("");
            }
        }
    }

    private void placeCases() {
        gridPanel.setLayout(new GridLayout(main.getField().getDimX(), main.getField().getDimY()));
        add(gridPanel, BorderLayout.CENTER);

        tabCase = new Case[main.getField().getDimX()][main.getField().getDimY()];
        for (int x = 0; x < main.getField().getDimX(); x++) {
            for (int y = 0; y < main.getField().getDimY(); y++) {
                tabCase[x][y] = new Case(x, y, main);
                gridPanel.add(tabCase[x][y]);
            }
        }
    }

    /**
     * Asked to all cases to start to reinitialize. (The mine position is not changed here)
     */
    private void newGame() {
        for (int x = 0; x < main.getField().getDimX(); x++) {
            for (int y = 0; y < main.getField().getDimY(); y++) {
                tabCase[x][y].newGameCase();
            }
        }
        stopWatch.stopCpt();
        main.newGame();
    }

    /**
     * Update the GUI with the player ID information, indicate players color and ID
     *
     * @param playerId Integer
     */
    void playerIdUpdate(int playerId) {
        if (main.isOnlineGame()) {
            ipTF.setEnabled(false);
            portTF.setEnabled(false);
            nicknameTF.setEnabled(false);
            connectPanel.setBackground(new Color(tabCase[0][0].handleColor(playerId)));
        } else {
            ipTF.setEnabled(true);
            portTF.setEnabled(true);
            nicknameTF.setEnabled(true);
            connectPanel.setBackground(null); //default color
        }
    }

    /**
     * Asked to all cases to start a new party, and reposition the mines.
     * Take as input level when the new game is to be launch with a new level.
     *
     * @param level level enum of the new level of the game
     */
    void newGame(Level level) {
        gridPanel.removeAll();
        if (main.isOnlineGame()) {
            if (level != Level.CUSTOM) {
                main.getField().newGame(level);
            } else {
                main.getField().newGame(level, main.getDimXCustom(), main.getDimYCustom());
            }
        }
        placeCases();
        main.pack();
        stopWatch.stopCpt();
        main.newGame(level);
    }

    StopWatch getStopWatch() {
        return stopWatch;
    }

    /**
     * Will see if the adjacent cases are of the same value of the one clicked and if it is set
     * these cases to clicked through the method setClickedTrue.
     * The given case at the position x,y is already set to be clicked.
     *
     * @param x Integer x position of the case to find adjacent values from
     * @param y Integer y position of the case to find adjacent values from
     */
    void adjacentSameValue(int x, int y) {
        int borneInfX = x == 0 ? 0 : -1;
        int borneSupX = x == main.getField().getDimX() - 1 ? 0 : 1;
        int borneInfY = y == 0 ? 0 : -1;
        int borneSupY = y == main.getField().getDimY() - 1 ? 0 : 1;

        for (int i = borneInfX; i <= borneSupX; i++) {
            for (int k = borneInfY; k <= borneSupY; k++) {
                if (!(i == 0 && k == 0)) { //We don't count the mine itself
                    if (!tabCase[x + i][y + k].getClicked()) { //If the case is not already clicked
                        tabCase[x + i][y + k].setClickedTrue();
                        if (main.getField().numberMinesSurrounding(x + i, y + k) == 0) {
                            adjacentSameValue(x + i, y + k);
                        }
                    }
                }
            }
        }
    }

    void coDecoButtonChangeText() {
        if (main.connected)
            coDiscoButton.setText("Disconnect");
        else
            coDiscoButton.setText("  Connect  ");
        coDiscoButton.repaint();
    }

    void addMsgGui(String msg) {
        msgServer.append(msg + "\n");
    }

    /**
     * Enable or disable the "new Game" button on the GUI
     *
     * @param state A boolean
     */
    void setNewGameButtonState(boolean state) {
        butRestart.setEnabled(state);
        butRestart.repaint();
    }

    /**
     * get case X,Y from GUI
     *
     * @param X X axis position of the case to get from gridlayout
     * @param Y Y axis position of the case to get from gridlayout
     * @return The case from the grid layout at the position X, Y
     */
    Case getCaseXY(int X, int Y) {
        return tabCase[X][Y];
    }

    /**
     * Get the restart button from GUI
     *
     * @return The Restart button from GUI
     */
    JButton getButRestart() {
        return butRestart;
    }
}
