package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class that defines the GUI of the server.
 */
public class GUIServer extends JPanel implements ActionListener {
    private ServerMinesWeeper main;
    private JButton startB; // Button to use to start the server
    private JButton closeServerB; // Button to use to close the server and all connections
    private JTextArea dialog = new JTextArea(20, 35); // Dialog/log's server information
    private JComboBox<String> listLevels;

    GUIServer(ServerMinesWeeper server) {
        main = server;
        setLayout(new BorderLayout());

        //Start button
        startB = new JButton("Start Game");
        startB.addActionListener(this);
        // Stop Server button
        closeServerB = new JButton("Close Server");
        closeServerB.addActionListener(this);
        //DropDown item list of levels
        String[] levels = {Level.EASY.name(), Level.MEDIUM.name(), Level.HARD.name(), Level.CUSTOM.name()};
        listLevels = new JComboBox<>(levels);
        listLevels.addActionListener(this);
        JPanel southButtonsP = new JPanel();
        southButtonsP.add(listLevels);
        southButtonsP.add(startB);
        southButtonsP.add(closeServerB);
        this.add(new JLabel("MinesWeeper server - 2019", SwingConstants.CENTER), BorderLayout.NORTH);
        this.add(southButtonsP, BorderLayout.SOUTH);

        //Dialog / log data of server
        dialog.setEditable(false);
        this.add(dialog, BorderLayout.CENTER);
    }

    /**
     * Add a message to the TextArea of the GUI
     *
     * @param newMsg String, message to add to the GUI
     */
    void addDialogText(String newMsg) {
        dialog.append(newMsg + "\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startB) {
            if (!main.isGameStarted()) {
                main.setGameStarted(true);
                main.startGame();
                main.gameStarted();
                startB.setText("End Game");
                main.broadcastMSG("Game started");
                main.newGameServer();
                listLevels.setEnabled(false);
            } else {
                startB.setText("Start Game");
                listLevels.setEnabled(true);
                main.gameStopped();
            }
        } else if (e.getSource() == closeServerB) {
            main.gameStopped();
            main.closeServer();
        } else if (e.getSource() == listLevels) {
            JComboBox c = (JComboBox) e.getSource();
            handleLevelSelection((String) c.getSelectedItem());
        }
    }

    /**
     * Manage the level selection  of the GUI
     *
     * @param selectedItem The item selected in the ComboBox
     */
    private void handleLevelSelection(String selectedItem) {
        if (!main.isGameStarted()) { //Cannot change game level if game is started
            switch (selectedItem) {
                case "EASY":
                    if (main.getLevel() != Level.EASY) {
                        addDialogText("Level changed to EASY");
                        main.setLevel(Level.EASY);
                        main.newGameServer();
                    }
                    break;
                case "MEDIUM":
                    if (main.getLevel() != Level.MEDIUM) {
                        addDialogText("Level changed to MEDIUM");
                        main.setLevel(Level.MEDIUM);
                        main.newGameServer();
                    }
                    break;
                case "HARD":
                    if (main.getLevel() != Level.HARD) {
                        addDialogText("Level changed to HARD");
                        main.setLevel(Level.HARD);
                        main.newGameServer();
                    }
                    break;
                case "CUSTOM":
                    if (main.getLevel() != Level.CUSTOM) {
                        addDialogText("Level changed to CUSTOM");
                        main.setLevel(Level.CUSTOM);
                        main.getMineField().getDimsOptionPanel();
                        main.newGameServer();
                    }
                    break;
                default:
                    addDialogText("Error, unknown level selected.");
                    break;

            }
        } else {
            addDialogText("Cannot change 'level' : a game is already started.");
        }
    }

    /**
     * Get the start/End game button from the server gui
     * @return The JButton object of the GUI
     */
    public JButton getStartB() {
        return startB;
    }
}
