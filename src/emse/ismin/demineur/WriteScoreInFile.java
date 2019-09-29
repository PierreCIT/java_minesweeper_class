package emse.ismin.demineur;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Stores the score in a file. The file will be located in the same folder as the program.
 *
 * @author Pierre
 */
class WriteScoreInFile {
    private final static String dashLine = "---------------------------------------";

    WriteScoreInFile() {
    }

    /**
     * Save the scores of the players, when playing online
     * @param playerList The list of all players
     * @param level Level of the game of the scores to save
     */
    void writeOnlineScoreInScoreOnlineFile(List<Player> playerList, Level level) {
        File dir = new File("."); //Current directory
        String loc = null;
        try {
            loc = dir.getCanonicalPath() + File.separator + "ScoresOnline.dat";
            FileWriter fstream = new FileWriter(loc, true);
            BufferedWriter out = new BufferedWriter(fstream);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            out.write(dashLine);
            out.newLine();
            out.write("Game of the : " + dtf.format(now) + ", level : " + level.name());
            out.newLine();
            for (Player player : playerList) {
                if (player.isInGame()) {
                    String msg = "PlayerId: " + player.getPlayerId() + ", Nickname: " + player.getNickname() + ", Score: " +
                            player.getScore();
                    if (player.isExploded()) {
                        msg += ", Exploded";
                    }
                    if (!player.isConnected()) {
                        msg += ", Disconnected in game";
                    }
                    out.write(msg);
                    out.newLine();
                }
            }
            out.newLine();
            out.write(dashLine);
            out.newLine();
            out.newLine();
            //close buffer writer
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves score in a file when playing in local
     * @param score Score to save
     * @param exploded Boolean to say if the player exploded (lost) or not
     * @param level Level of the game to saves the scores
     */
    void writeLocalScoreInScoreFile(int score, boolean exploded, Level level) {
        File dir = new File("."); //Current directory
        String loc = null;
        try {
            loc = dir.getCanonicalPath() + File.separator + "Scores.dat";
            FileWriter fstream = new FileWriter(loc, true);
            BufferedWriter out = new BufferedWriter(fstream);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            out.write(dashLine);
            out.newLine();
            out.write("Game of the : " + dtf.format(now) + ", level : " + level.name());
            out.newLine();
            String msg = " Score: " + score;
            if (exploded) {
                msg += ", Exploded";
            }
            out.write(msg);
            out.newLine();
            out.newLine();
            out.write(dashLine);
            out.newLine();
            out.newLine();
            //close buffer writer
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
