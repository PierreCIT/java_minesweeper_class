package emse.ismin.demineur;

import java.util.Comparator;

/**
 * Class that will manage a player, by keeping track of its state (connected or not), its score, its nickname
 * and if he lost the game by clicking on a mine.
 */
public class Player {
    private int score = 0;
    private int playerId; //Unique identifier of a player
    private boolean connected = true;
    private boolean exploded = false;
    private boolean inGame = false; // Can make the distinction between player who joined at the begining or not
    private String nickname;

    Player(String name, int playerId) {
        nickname = name;
        this.playerId = playerId;
    }

    /**
     * Increase the score of the player by one point (1 more case clicked) and return the new value
     *
     * @return Integer value of the score after the increase.
     */
    public int increaseScore() {
        return score++;
    }

    /**
     * Set the player state as deconnected. We keep its score to print at the end of the game
     */
    public void disconnected() {
        connected = false;
    }

    /**
     * Return the state of the player (if he is connected or not)
     *
     * @return Boolean the state of the player (if he is connected or not)
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return Integer of the player's score
     */
    public int getScore() {
        return score;
    }

    /**
     * @return Boolean the state of the player (true = clicked on a mine, false = still in game)
     */
    public boolean isExploded() {
        return exploded;
    }

    /**
     * @return String, nickname of the player
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Set that the player exploded
     *
     * @param exploded State of the player : dead or alive
     */
    public void setExploded(boolean exploded) {
        this.exploded = exploded;
    }

    /**
     * Set the score of the player (to be used to reinitialize for a new game)
     *
     * @param score Integer positive representing the score of the player (
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Set the state of the player to connected or disconnected
     *
     * @param connected A boolean true means that the player is connected, false deconnected
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Set if the player is in a game or if he joined while a game was started
     *
     * @param inGame Boolean of the player state
     */
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    /**
     * To know if the player was in the game or not
     *
     * @return A boolean of the state of the player (inGame or not)
     */
    public boolean isInGame() {
        return inGame;
    }

    /**
     * Get the player Id
     *
     * @return Intger of the player ID
     */
    public int getPlayerId() {
        return playerId;
    }
}