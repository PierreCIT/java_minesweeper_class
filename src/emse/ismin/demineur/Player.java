package emse.ismin.demineur;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Class that will manage a player, by keeping track of its state (connected or not), its score, its nickname
 * and if he lost the game by clicking on a mine.
 */
public class Player {
    private int score = 0;
    private int playerId; //Unique identifier of a player
    private boolean connected = true;
    private boolean exploded = false;
    private boolean inGame = false; // Can make the distinction between player who joined at the beginning or not
    private String nickname;
    private DataOutputStream out; //Output stream to the client
    private DataInputStream in; //Input stream from the client

    Player(String name, int playerId, DataInputStream in, DataOutputStream out) {
        nickname = name;
        this.playerId = playerId;
        this.in = in;
        this.out = out;
    }

    Player(String name, int playerId) {
        nickname = name;
        this.playerId = playerId;
    }

    /**
     * Increase the score of the player by one point (1 more case clicked) and return the new value
     *
     */
    void increaseScore() {
        score++;
    }

    /**
     * Set the player state as disconnected. We keep its score to print at the end of the game
     */
    void disconnected() {
        connected = false;
    }

    /**
     * Return the state of the player (if he is connected or not)
     *
     * @return Boolean the state of the player (if he is connected or not)
     */
    boolean isConnected() {
        return connected;
    }

    /**
     * @return Integer of the player's score
     */
    int getScore() {
        return score;
    }

    /**
     * @return Boolean the state of the player (true = clicked on a mine, false = still in game)
     */
    boolean isExploded() {
        return exploded;
    }

    /**
     * @return String, nickname of the player
     */
    String getNickname() {
        return nickname;
    }

    /**
     * Set that the player exploded
     *
     * @param exploded State of the player : dead or alive
     */
    void setExploded(boolean exploded) {
        this.exploded = exploded;
    }

    /**
     * Set the score of the player (to be used to reinitialize for a new game)
     *
     * @param score Integer positive representing the score of the player (
     */
    void setScore(int score) {
        this.score = score;
    }

    /**
     * Set if the player is in a game or if he joined while a game was started
     *
     * @param inGame Boolean of the player state
     */
    void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    /**
     * To know if the player was in the game or not
     *
     * @return A boolean of the state of the player (inGame or not)
     */
    boolean isInGame() {
        return inGame;
    }

    /**
     * Get the player Id
     *
     * @return Integer of the player ID
     */
    int getPlayerId() {
        return playerId;
    }

    /**
     * Get the output data stream of this player
     * @return The output data stream to the client
     */
    DataOutputStream getOut() {
        return out;
    }

    /**
     * Get the input data stream of this player
     * @return The input data stream to the client
     */
    public DataInputStream getIn() {
        return in;
    }
}