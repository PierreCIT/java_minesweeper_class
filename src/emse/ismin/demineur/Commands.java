package emse.ismin.demineur;

/**
 * Enum of all possible commands exchanged between the server and client.
 */
public enum Commands {
    MSG, POSITION, ENDGAME, STARTGAME, SERVERSTOPPED, CLIENTDISCONNECT, LEVEL, LOST, WIN, FINISHGAME,
    NEWGAME, PLAYERID, CHATIN
}
