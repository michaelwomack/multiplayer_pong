package main;


import java.io.Serializable;

/**
 * Created by michaelwomack on 3/31/16.
 */
public class GameObjectPositions implements Serializable {
    private int ballX, ballY, ballXVel, ballYVel, opponentY, opponentYVel, gameStatus;

    public GameObjectPositions(int opponentY, int opponentYVel) {
        this.opponentY = opponentY;
        this.opponentYVel = opponentYVel;
    }

    public GameObjectPositions(int ballX, int ballY, int ballVelX, int ballYVel, int opponentY, int opponentYVel) {
        this.ballX = ballX;
        this.ballXVel = ballVelX;
        this.ballY = ballY;
        this.ballYVel = ballYVel;
        this.opponentY = opponentY;
        this.opponentYVel = opponentYVel;
    }

    public int getBallX() {
        return ballX;
    }

    public int getBallY() {
        return ballY;
    }

    public int getBallVelX() {
        return ballXVel;
    }

    public int getBallVelY() {
        return ballYVel;
    }

    public int getOpponentY() {
        return opponentY;
    }

    public int getOpponentVelY() {
        return opponentYVel;
    }

    public int getGameStatus() { return gameStatus; }
}
