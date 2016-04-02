package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import models.Ball;
import models.Paddle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PongClient extends Application implements PongConstants {
    private int playerNo, opponentNo;
    private String host = "localhost";
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private Socket clientSocket;
    private Label p1ScoreLabel, p2ScoreLabel;
    private int p1Score, p2Score;
    private Paddle p1Paddle, p2Paddle, clientPaddle, opponentPaddle;
    private Ball ball;
    private boolean gameOver;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.getStyleClass().add("background");

        ImageView lineImage = new ImageView(new Image("resources/line.png"));
        lineImage.setX(GAME_WIDTH / 2);

        p1ScoreLabel = new Label("Player 1: " + p1Score);
        p2ScoreLabel = new Label("Player 2: " + p2Score);

        p1ScoreLabel.getStyleClass().add("p1-score");
        p2ScoreLabel.getStyleClass().add("p2-score");
        p2ScoreLabel.setLayoutX(GAME_WIDTH - 150);
        root.getChildren().addAll(lineImage, p1ScoreLabel, p2ScoreLabel);

        ball = new Ball(25, 25, GAME_WIDTH / 2 - 12, GAME_HEIGHT / 2 - 12);
        ball.getRect().getStyleClass().add("ball");
        p1Paddle = new Paddle(0, GAME_HEIGHT / 2 - 40, PADDLE_WIDTH, PADDLE_HEIGHT);
        p2Paddle = new Paddle(GAME_WIDTH - PADDLE_WIDTH, GAME_HEIGHT / 2 - 40, PADDLE_WIDTH, PADDLE_HEIGHT);
        p1Paddle.getRect().getStyleClass().add("paddle");
        p2Paddle.getRect().getStyleClass().add("paddle");
        root.getChildren().addAll(p1Paddle.getRect(), p2Paddle.getRect(), ball.getRect());

        Scene scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        scene.getStylesheets().add("resources/styles.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pong");
        primaryStage.show();

        /* In a new thread, update game mechanics
         * Platform.runLater() for rendering?
         */

        new Thread(() -> {
            try {
                /* Client connects to server */
                clientSocket = new Socket(host, 8000);
                fromServer = new DataInputStream(clientSocket.getInputStream());
                toServer = new DataOutputStream(clientSocket.getOutputStream());
                playerNo = fromServer.readInt() == PLAYER1 ? PLAYER1: PLAYER2;
                opponentNo = playerNo == PLAYER1 ? PLAYER2: PLAYER1;
                clientPaddle = playerNo == PLAYER1 ? p1Paddle: p2Paddle;
                opponentPaddle = opponentNo == PLAYER1 ? p1Paddle: p2Paddle;

                System.out.println("You are player " + playerNo + ". Opponent: " + opponentNo);
                Platform.runLater(() -> primaryStage.setTitle("You are player " + playerNo));

                int gameInit = fromServer.readInt();
                Thread.sleep(2000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameOver = false;

            while (!gameOver) {
                update();
                try {
                /* Send this client's paddle coordinates to server.
                 * Send current velocity
                 */

                    toServer.writeInt(clientPaddle.getY());
                    toServer.writeInt(clientPaddle.getVelY());

                    //Send ball x, y, xVel, yVel
                    //Send client and ball data in an object containing all data.

                    int opponentYPos = fromServer.readInt();
                    int opponentVelY = fromServer.readInt();
                    opponentPaddle.getRect().setY(opponentYPos);
                    opponentPaddle.setVelY(opponentVelY);
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        scene.setOnKeyPressed(event -> {
            onKeyPress(event);
        });
        scene.setOnKeyReleased(event -> {
            clientPaddle.stop();
        });
    }

    private void onKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case UP:
                clientPaddle.accelUp();
                break;
            case DOWN:
                clientPaddle.accelDown();
                break;
        }
    }

    public void init() {
        /* TODO
         * initializes instances of game objects
         * */
    }

    public void update() {
        /* TODO
         * updates both paddles and ball
         * and checks collisions
         */
        p1Paddle.update();
        p2Paddle.update();
        ball.update();

        if (ballCollide(p1Paddle)) {
            p1Score++;
            ball.onCollideWith(p2Paddle);
        } else if (ballCollide(p2Paddle)) {
            p2Score++;
            ball.onCollideWith(p2Paddle);
        } else if (ball.isDead()) {
            if (ball.getX() < GAME_WIDTH / 2)
                p2Score -= 5;
            else
                p1Score -= 5;
            ball.reset();
        }
        updateScores();
    }

    private void updateScores() {
         p1ScoreLabel.setText("Player 1: " + p1Score);
         p2ScoreLabel.setText("Player 2: " + p2Score);
    }

    private boolean ballCollide(Paddle p) {
        return ball.getRect().intersects(p.getX(), p.getY(), p.getWidth(), p.getHeight());
    }

    public void render() {
        /* TODO */
    }
}
