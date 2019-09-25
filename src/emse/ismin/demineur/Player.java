package emse.ismin.demineur;

public class Player implements Runnable {
    private Thread playerTh;
    private ServeurDemineur serveurDemineur;

    Player(ServeurDemineur main){
        serveurDemineur = main;
    }

    @Override
    public void run() {

    }
}
