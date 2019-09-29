package emse.ismin.demineur;

import java.util.Comparator;

/**
 * Class that will enable to sort the players by descending score.
 */
public class SortByScore implements Comparator<Player> {
    /**
     * Will compare two player scores in a way that will create descending order when using it to sort a player list
     * @param a Player a
     * @param b Player b
     * @return The difference between player a's score and b's.
     */
    public int compare(Player a, Player b){
        return b.getScore() - a.getScore();
    }
}
