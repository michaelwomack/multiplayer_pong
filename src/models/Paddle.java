package models;

import javafx.scene.shape.Rectangle;
import main.PongClient;

/**
 * Created by michaelwomack on 2/28/16.
 */
public class Paddle {
    private int width, height, x, y, velY;
    private Rectangle rect;
    private final static int MOVE_SPEED_Y = 4;

    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        rect = new Rectangle(x, y , width, height);
        velY = 0;
    }

    public void update() {
        y += velY;

        if (y < 0)
            y = 0;
        else if (y + height > PongClient.GAME_HEIGHT) {
            y = PongClient.GAME_HEIGHT - height;
        }
        updateRect();
    }

    private void updateRect() {
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(width);
        rect.setHeight(height);
    }

    public void accelUp() {
        velY = -MOVE_SPEED_Y;
    }

    public void accelDown() {
        velY = MOVE_SPEED_Y;
    }

    public void stop() {
        velY = 0;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getVelY() { return velY; }

    public void setVelY(int velY) { this.velY = velY; }

    public Rectangle getRect() {
        return rect;
    }
}
