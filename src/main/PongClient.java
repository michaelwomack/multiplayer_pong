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

import java.io.*;
import java.net.Socket;

public class PongClient extends Application implements PongConstants {
    private int playerNo, opponentNo;
    private String host = "localhost";
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;
    private Socket clientSocket;
    private Label p1ScoreLabel, p2ScoreLabel;
    private int p1Score, p2Score;
    private Paddle p1Paddle, p2Paddle, clientPaddle, opponentPaddle;
    private Ball ball;
    private boolean gameOver;
    private Pane root;

    @Override
    public void start(Stage primaryStage) {
        initObjects();
        render();

        Scene scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        scene.getStylesheets().add("resources/styles.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pong");
        primaryStage.show();

        scene.setOnKeyPressed(event -> {
            onKeyPress(event);
        });
        scene.setOnKeyReleased(event -> {
            clientPaddle.stop();
        });

        new Thread(() -> {
            try {
                /* Client connects to server */
                clientSocket = new Socket(host, 8000);

                toServer = new ObjectOutputStream(clientSocket.getOutputStream());
                fromServer = new ObjectInputStream(clientSocket.getInputStream());

                playerNo = (Integer) fromServer.readObject() == PLAYER1 ? PLAYER1 : PLAYER2;

                System.out.println("Player: " + playerNo);
                opponentNo = playerNo == PLAYER1 ? PLAYER2 : PLAYER1;
                clientPaddle = playerNo == PLAYER1 ? p1Paddle : p2Paddle;
                opponentPaddle = opponentNo == PLAYER1 ? p1Paddle : p2Paddle;

                System.out.println("You are player " + playerNo + ". Opponent: " + opponentNo);
                Platform.runLater(() -> primaryStage.setTitle("You are player " + playerNo));


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            gameOver = false;


            GameObjectPositions sendPositions, updatedPositions;
            while (!gameOver) {
                update();
                try {

                    if (playerNo == PLAYER1)
                        sendPositions = new GameObjectPositions(ball.getX(), ball.getY(),
                                ball.getxVel(), ball.getyVel(), p1Paddle.getY(),
                                p1Paddle.getVelY());
                    else {
                        sendPositions = new GameObjectPositions(p2Paddle.getY(),
                                p2Paddle.getVelY());
                    }
                    toServer.writeObject(sendPositions);

                    updatedPositions = (GameObjectPositions) fromServer.readObject();

                    if (playerNo == PLAYER2) {
                        ball.setX(updatedPositions.getBallX());
                        ball.setY(updatedPositions.getBallY());
                        ball.setxVel(updatedPositions.getBallVelX());
                        ball.setyVel(updatedPositions.getBallVelY());
                    }

                    opponentPaddle.getRect().setY(updatedPositions.getOpponentY());
                    opponentPaddle.setVelY(updatedPositions.getOpponentVelY());

                    /* So frame rate is smooth */
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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

    public void initObjects() {
        root = new Pane();
        root.getStyleClass().add("background");
        ImageView lineImage = new ImageView(new Image("resources/line.png"));
        lineImage.setX(GAME_WIDTH / 2);

        p1ScoreLabel = new Label("Player 1: " + p1Score);
        p2ScoreLabel = new Label("Player 2: " + p2Score);

        p1ScoreLabel.getStyleClass().add("p1-score");
        p2ScoreLabel.getStyleClass().add("p2-score");
        p2ScoreLabel.setLayoutX(GAME_WIDTH - 160);
        root.getChildren().addAll(lineImage, p1ScoreLabel, p2ScoreLabel);

        ball = new Ball(25, 25, GAME_WIDTH / 2 - 12, GAME_HEIGHT / 2 - 12);
        ball.getRect().getStyleClass().add("ball");
        p1Paddle = new Paddle(0, GAME_HEIGHT / 2 - 40, PADDLE_WIDTH, PADDLE_HEIGHT);
        p2Paddle = new Paddle(GAME_WIDTH - PADDLE_WIDTH, GAME_HEIGHT / 2 - 40, PADDLE_WIDTH, PADDLE_HEIGHT);
        p1Paddle.getRect().getStyleClass().add("paddle");
        p2Paddle.getRect().getStyleClass().add("paddle");
    }

    public void update() {
        p1Paddle.update();
        p2Paddle.update();
        ball.update();


        if (ballCollide(p1Paddle)) {
            p1Score++;
            ball.onCollideWith(p1Paddle);
        } else if (ballCollide(p2Paddle)) {
            p2Score++;
            ball.onCollideWith(p2Paddle);
        } else if (ball.isDead()) {
            if (ball.getX() < GAME_WIDTH / 2)
                p1Score -= 1;
            else
                p2Score -= 1;

        }
        checkGameStatus();
        render();
    }

    public void checkGameStatus() {
        if (p1Score >= 10 || p2Score >= 10) {
            gameOver = true;
        }
    }

    private boolean ballCollide(Paddle p) {
        return ball.getRect().intersects(p.getX(), p.getY(), p.getWidth(), p.getHeight());
    }

    public void render() {
        Platform.runLater(() -> {
            p1ScoreLabel.setText("Player 1: " + p1Score);
            p2ScoreLabel.setText("Player 2: " + p2Score);
            root.getChildren().removeAll(p1Paddle.getRect(), p2Paddle.getRect(), ball.getRect());
            root.getChildren().addAll(p1Paddle.getRect(), p2Paddle.getRect(), ball.getRect());
        });
    }
}
