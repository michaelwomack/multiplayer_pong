package models;

/**
 * Created by michaelwomack on 4/10/16.
 */
public class Player {
    private int playerNo;
    private String name;
    private Paddle paddle;

    public Player() {}

    public Player(int playerNo, String name, Paddle paddle) {
        this.playerNo = playerNo;
        this.name = name;
        this.paddle = paddle;
    }

    public int getPlayerNo() {
        return playerNo;
    }

    public void setPlayerNo(int playerNo) {
        this.playerNo = playerNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public void setPaddle(Paddle paddle) {
        this.paddle = paddle;
    }
}
