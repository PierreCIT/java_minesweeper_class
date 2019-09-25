package emse.ismin.demineur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIServer extends JPanel implements ActionListener {
    private ServeurDemineur main;
    private JButton startB; // Button to use to start the server
    private JTextArea dialog = new JTextArea(20,20); // Dialog/log's server information


    public GUIServer(ServeurDemineur server){
        main = server;
        setLayout(new BorderLayout());

        //Start button
        startB = new JButton("Start");
        startB.addActionListener(this);
        this.add(new JLabel("MinesWheeper server 2019"), BorderLayout.NORTH);
        this.add(startB, BorderLayout.SOUTH);

        //Dialog / lof data of server
        dialog.setEditable(false);
        this.add(dialog, BorderLayout.CENTER);
    }

    public void addDialogText(String newMsg){
        dialog.append(newMsg);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startB){
            main.startGame();
        }
    }
}
