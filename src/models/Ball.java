package models;

import javafx.scene.shape.Rectangle;
import main.PongConstants;
import main.RandomNumberGenerator;


/**
 * Created by michaelwomack on 4/1/16.
 */
public class Ball implements PongConstants {
    private int width, height, x, y, xVel, yVel;
    private Rectangle rect;

    public Ball(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.xVel = RandomNumberGenerator.getRandIntBetween(3, 4);
        this.yVel = RandomNumberGenerator.getRandIntBetween(-3, 3);
        this.rect = new Rectangle(x, y, width, height);
    }

    public void update() {
        x += xVel;
        y += yVel;
        correctCollision();
        updateRect();
    }

    private void correctCollision() {
        if (y < 0) {
            y = 0;
            yVel = -yVel;
        }
        else if (y + height > GAME_HEIGHT) {
            y = GAME_HEIGHT - height;
            yVel = -yVel;
        }

        if (x < 0) {
            x = 0;
            xVel = -xVel;
        }
        else if (x + width > GAME_WIDTH) {
            x = GAME_WIDTH - width;
            xVel = -xVel;
        }
    }

    public void onCollideWith(Paddle p) {
        if (x < GAME_WIDTH / 2)
            x = p.getX() + p.getWidth();
        else
            x = p.getX() - width;
        xVel = -xVel;
        yVel = yVel == 0 ? RandomNumberGenerator.getRandIntBetween(1, 4): yVel;
    }

    private void updateRect() {
        this.rect.setX(x);
        this.rect.setY(y);
        this.rect.setWidth(width);
        this.rect.setHeight(height);
    }

    public boolean isDead() {
        return (x <= 0 || x + width >= GAME_WIDTH);
    }

    public void reset() {
        rect.setX(GAME_WIDTH / 2 - 12);
        rect.setY(GAME_HEIGHT / 2 - 12);
        xVel = 4;
        yVel = RandomNumberGenerator.getRandIntBetween(2, 3);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getxVel() {
        return xVel;
    }

    public void setxVel(int xVel) {
        this.xVel = xVel;
    }

    public int getyVel() {
        return yVel;
    }

    public void setyVel(int yVel) {
        this.yVel = yVel;
    }

    public Rectangle getRect() {
        return rect;
    }
}
