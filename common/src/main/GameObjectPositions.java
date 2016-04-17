package main;


import java.io.Serializable;
import java.time.LocalTime;
import java.util.Date;

/**
 * Created by michaelwomack on 3/31/16.
 */
public class GameObjectPositions implements Serializable {
    private int ballX, ballY, ballXVel, ballYVel, opponentY, opponentYVel, fromPlayer, gameStatus;
    private Long time;
    public GameObjectPositions(int opponentY, int opponentYVel) {
        this.opponentY = opponentY;
        this.opponentYVel = opponentYVel;
        this.fromPlayer = 2;
        this.time = System.nanoTime();
    }

    public GameObjectPositions(int ballX, int ballY, int ballVelX, int ballYVel, int opponentY, int opponentYVel) {
        this.ballX = ballX;
        this.ballXVel = ballVelX;
        this.ballY = ballY;
        this.ballYVel = ballYVel;
        this.opponentY = opponentY;
        this.opponentYVel = opponentYVel;
        this.fromPlayer = 1;
        this.time = System.nanoTime();

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

    public Long getTime() {
        return time;
    }

    public int getFromPlayer() {
        return fromPlayer;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
    }

    @Override
    public String toString() {
        return "GameObjectPositions{" +
                "ballX=" + ballX +
                ", ballY=" + ballY +
                ", ballXVel=" + ballXVel +
                ", ballYVel=" + ballYVel +
                ", opponentY=" + opponentY +
                ", opponentYVel=" + opponentYVel +
                '}';
    }
}
