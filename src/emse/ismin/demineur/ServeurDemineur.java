package emse.ismin.demineur;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServeurDemineur extends JFrame implements Runnable {
    private GUIServer guiServer;
    private Thread manageCo;
    private ServerSocket manageSock;
    private static final int serverPort = 10000; //Default server port
    private int playerId = 0; //To id players by a number
    List<DataInputStream> inList = new ArrayList<DataInputStream>();
    List<DataOutputStream> outList = new ArrayList<DataOutputStream>();

    ServeurDemineur() {
        System.out.println("Server starting ...");
        //Create GUI

        guiServer = new GUIServer(this);
        setContentPane(guiServer);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        startServer();
    }

    private void startServer() {
        try {
            //Start socket management
            manageSock = new ServerSocket(serverPort);
            //Thread to wait for client
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServeurDemineur();
    }

    public void startGame() {
        guiServer.addDialogText("Game Started");
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread " + playerId + " Started");
            Socket socket = manageSock.accept(); //New client connected
            new Thread(this).start(); //Wait for new client
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String namePlayer = in.readUTF(); //Wait for first message which shall contain the player's name
            inList.add(in);
            outList.add(out);
            guiServer.addDialogText("New connexion " + namePlayer + ", set as player " + playerId);
            playerId++;
            while (true) {

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
