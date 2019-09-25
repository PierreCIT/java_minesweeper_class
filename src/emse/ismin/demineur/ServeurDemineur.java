package emse.ismin.demineur;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurDemineur extends JFrame {
    private GUIServer guiServer;
    private static final int serverPort = 10000; //Default server port

    ServeurDemineur(){
        System.out.println("Server starting ...");
        //Create GUI
        guiServer = new GUIServer(this);
        setContentPane(guiServer);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        startServer();
    }

    public void startServer(){
        try{
            ServerSocket manageSock = new ServerSocket(serverPort);
            Socket socket = manageSock.accept(); //wait for a connection
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String namePlayer = in.readUTF();
            guiServer.addDialogText("New connexion "+ namePlayer+"\n");
        }catch (IOException e){e.printStackTrace();}
    }

    public static void main(String[] args) {
        new ServeurDemineur();
    }

    public void startGame() {
        guiServer.addDialogText("Game Started\n");
    }
}
