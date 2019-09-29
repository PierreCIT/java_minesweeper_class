# MinesWeeper
 This code is the result of a school lesson and evaluation from the `Java course`
 at [Mines Saint Étienne, ISMIN cursus](https://www.mines-stetienne.fr/en/formation/master-degree-in-microelectronics-and-computer-science/).

---
## Objectives
 The objectives of this course was to teach Java's fundamentals and evaluate it through
 the creation of an online (with a server) MinesWeeper game as we could previously find in windows' games.
 The game has to be playable without a server (in local).

---
## Rules
The rules are different depending on the type of game (online of local):

### Local
In local mode the rules are the same as the 'usual' MinesWeeper.
You need to click all cases that are not bombs. If you click on a bomb you loose. The objective is then to click all no-bomb cases in a minimum of time (a counter in red show the time taken in seconds).

### Online
The online rule are different from the usual MinesWeeper game.
- In this version the objective is to click the maximum of case without touching a bomb. Contrary to the usual game if you click an empty case (not close from a bomb) only the case clicked will be discovered.
- If everyone hits a bomb the last one alive is the winner.
- Every player has an ID and a color (limited 10 players, after all player a dark gray).
---
## How to play
### Local
In local just start clicking a case and the game will start.
If you want to restart at the same level of difficulty just click the `Restart` button.
If you want to change the level of difficulty go to the `Game` menu and then click on `New Game` and then select the level of difficulty you want to start a game at.

##### Score
The scores of all games will be saved in a `Scores.dat` file which will contain the date of the game and the score.
The score represent the time it took you to finish the game. If you finish the game by exploding it will be specified.

### Online
In online mode the server decide when to start the game and at which difficulty every client must play. This is done by selecting the level of difficulty in the 'dropdown' menu. Start/End the game by clicking the `Start`/`End Game` button.
It is impossible to change the level of difficulty while in game.
You can close the server (and all connections to it) by clicking the `Close server` button.
A player cannot join a party while a game is in progress.
**You can't play alone on the online version of the game.**

##### Score
The scores of all games will be saved in a `ScoresOnline.dat` file which will contain the date of the game and the score of every player that participated in the game, even if they disconnected (during the game) but in this case it will be specified.
The score represent the time it took you to finish the game. If you finish the game by exploding it will be specified.
