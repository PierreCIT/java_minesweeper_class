package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIServer extends JPanel implements ActionListener {
    private ServeurDemineur main;
    private JButton startB; // Button to use to start the server
    private JButton closeServerB; // Button to use to close the server and all connections
    private JPanel southButtonsP;
    private JTextArea dialog = new JTextArea(20,35); // Dialog/log's server information

    public GUIServer(ServeurDemineur server){
        main = server;
        setLayout(new BorderLayout());

        //Start button
        startB = new JButton("Start Game");
        startB.addActionListener(this);
        // Stop Server button
        closeServerB = new JButton("Close Server");
        closeServerB.addActionListener(this);
        southButtonsP = new JPanel();
        southButtonsP.add(startB);
        southButtonsP.add(closeServerB);
        this.add(new JLabel("MinesWheeper server 2019"), BorderLayout.NORTH);
        this.add(southButtonsP, BorderLayout.SOUTH);

        //Dialog / log data of server
        dialog.setEditable(false);
        this.add(dialog, BorderLayout.CENTER);
    }

    public void addDialogText(String newMsg){
        dialog.append(newMsg+"\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startB){
            if(!main.isGameStarted()) {
                main.setGameStarted(true);
                main.startGame();
                main.broadcastMSG("Game started");
                main.gameStarted();
                startB.setText("End Game");
            }else{
                startB.setText("Start");
                main.gameStopped();
            }
        }else if(e.getSource() ==  closeServerB){
            main.gameStopped();
            main.closeServer();
        }
    }
}
