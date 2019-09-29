package emse.ismin.demineur;

import java.util.Comparator;

/**
 * Class that will enable to sort the players by descending score.
 */
public class SortByScore implements Comparator<Player> {
    public int compare(Player a, Player b){
        return b.getScore() - a.getScore();
    }
}
