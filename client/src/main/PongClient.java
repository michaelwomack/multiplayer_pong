package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import main.model.Ball;
import main.model.Paddle;
import main.model.Player;
import main.util.DatagramUtils;

import java.io.*;
import java.net.*;

public class PongClient extends Application implements PongConstants {
    private String winner;
    private InetAddress host;
    private int port, sessionNo;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;
    private Socket clientSocket;
    private Label p1ScoreLabel, p2ScoreLabel, errorLabel;
    private int p1Score, p2Score;
    private String p1Name, p2Name;
    private Paddle p1Paddle, p2Paddle;
    private Ball ball;
    private boolean gameOver;
    private Pane root;
    private Player player, opponent;

    @Override
    public void start(Stage primaryStage) {

        setup(primaryStage);
    }

    private void onKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case UP:
                player.getPaddle().accelUp();
                break;
            case DOWN:
                player.getPaddle().accelDown();
                break;
        }
    }

    public void setup(Stage stage) {
        Label hostLabel = new Label("Host");
        TextField hostTextField = new TextField();

        Label portLabel = new Label("Port");
        TextField portTextField = new TextField();

        Label nameLabel = new Label("Name");
        TextField nameTextField = new TextField();

        Button submitBtn = new Button("Play");
        submitBtn.setMinWidth(100);
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error");

        GridPane formPane = new GridPane();
        formPane.setAlignment(Pos.CENTER);
        formPane.setHgap(10);
        formPane.setVgap(10);

        formPane.add(hostLabel, 0, 0);
        formPane.add(hostTextField, 1, 0);
        formPane.add(portLabel, 0, 1);
        formPane.add(portTextField, 1, 1);
        formPane.add(nameLabel, 0, 2);
        formPane.add(nameTextField, 1, 2);
        formPane.add(submitBtn, 1, 3);
        formPane.add(errorLabel, 1, 4);

        submitBtn.setOnAction(e -> {
            String hostStr = hostTextField.getText().trim();
            String portStr = portTextField.getText().trim();
            String playerName = nameTextField.getText().trim();
            try {
                if ((hostStr.equals("") || portStr.equals("") || playerName.equals("")))
                    throw new IllegalArgumentException();
                else if (playerName.length() > 8) {
                    errorLabel.setText("Name has max length of 8");
                } else {
                    port = Integer.parseInt(portStr);
                    host = InetAddress.getByName(hostStr);
                    clientSocket = new Socket(host, port);
                    System.out.println("Time out: " + clientSocket.getSoTimeout());

                    initObjects();
                    player.setName(playerName);
                    render();
                    startGame(stage);
                }
            } catch (IOException e1) {
                errorLabel.setText("Couldn't connect to " + host + ": " + port);
                e1.printStackTrace();
            } catch (NumberFormatException e2) {
                errorLabel.setText("Port must be an integer.");
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                errorLabel.setText("Fields cannot be empty");
                e3.printStackTrace();
            }
        });

        Scene initScene = new Scene(formPane, 400, 200);
        initScene.getStylesheets().add("styles.css");

        stage.setScene(initScene);
        stage.show();
    }

    public void startGame(Stage stage) {
        Scene scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        scene.getStylesheets().add("styles.css");
        stage.setScene(scene);
        stage.setTitle("Pong");
        stage.centerOnScreen();
        stage.show();

        scene.setOnKeyPressed(event -> {
            onKeyPress(event);
        });
        scene.setOnKeyReleased(event -> {
            player.getPaddle().stop();
        });

        new Thread(() -> {
            DatagramSocket datagramSocket = null;
            DatagramUtils util = null;
            try {

                /* Initial Request to start game */
                toServer = new ObjectOutputStream(clientSocket.getOutputStream());
                fromServer = new ObjectInputStream(clientSocket.getInputStream());

                /* Get player number via TCP socket stream */
                int playerNo = (Integer) fromServer.readObject() == PLAYER1 ? PLAYER1 : PLAYER2;
                player.setPlayerNo(playerNo);

                System.out.println("Player: " + playerNo);
                int opponentNo = playerNo == PLAYER1 ? PLAYER2 : PLAYER1;
                opponent.setPlayerNo(opponentNo);

                toServer.writeObject(player.getName());
                opponent.setName((String) fromServer.readObject());

                Paddle clientPaddle = playerNo == PLAYER1 ? p1Paddle : p2Paddle;
                Paddle opponentPaddle = opponentNo == PLAYER1 ? p1Paddle : p2Paddle;

                p1Name = playerNo == PLAYER1 ? player.getName() : opponent.getName();
                p2Name = playerNo != PLAYER1 ? player.getName() : opponent.getName();

                /* Matches unique datagram Socket port for each session */
                sessionNo = (int) fromServer.readObject();
                port += sessionNo;

                player.setPaddle(clientPaddle);
                opponent.setPaddle(opponentPaddle);

                System.out.println("You are player "
                        + player.getPlayerNo()
                        + ". Opponent: " + opponent.getPlayerNo());

                /* Pass in datagram socket representing this client */
                datagramSocket = new DatagramSocket();
                util = new DatagramUtils(datagramSocket);

                /* Send player number via datagram so server Datagram can identify p1 and p2 */
                util.sendData(util.serializeData(playerNo), host, port);

                System.out.println("SERVER PORT: " + port);

                Platform.runLater(() -> stage.setTitle("You are player " + playerNo));
                countDownToStart();
                datagramSocket.setSoTimeout(10);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gameOver = false;
            GameObjectPositions sendPositions, updatedPositions;
            DatagramPacket receivedPacket;

            while (!gameOver) {
                update();
                try {

                    if (player.getPlayerNo() == PLAYER1)
                        sendPositions = new GameObjectPositions(ball.getX(), ball.getY(),
                                ball.getxVel(), ball.getyVel(), player.getPaddle().getY(),
                                player.getPaddle().getVelY());
                    else {
                        sendPositions = new GameObjectPositions(player.getPaddle().getY(),
                                player.getPaddle().getVelY());

                    }

                    util.sendData(util.serializeData(sendPositions), host, port);

                    receivedPacket = util.receiveData();
                    updatedPositions = (GameObjectPositions) util.deserializeData(receivedPacket.getData());

                    if (player.getPlayerNo() == PLAYER2) {
                        ball.setX(updatedPositions.getBallX());
                        ball.setY(updatedPositions.getBallY());
                        ball.setxVel(updatedPositions.getBallVelX());
                        ball.setyVel(updatedPositions.getBallVelY());
                    }

                    opponent.getPaddle().getRect().setY(updatedPositions.getOpponentY());
                    opponent.getPaddle().setVelY(updatedPositions.getOpponentVelY());

                    /* So frame rate is smooth */
                    Thread.sleep(10);
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                checkGameStatus();
            }

            displayWinner();

        }).start();
    }

    public void initObjects() {
        root = new Pane();
        root.getStyleClass().add("background");
        ImageView lineImage = new ImageView(new Image("line.png"));
        lineImage.setX(GAME_WIDTH / 2);

        p1ScoreLabel = new Label(p1Name + p1Score);
        p2ScoreLabel = new Label(p2Name + p2Score);

        player = new Player();
        opponent = new Player();

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

        render();
    }

    public void countDownToStart() throws InterruptedException {
        System.out.println("CountDown Started!!!!");
        Label numLabel = new Label();
        numLabel.getStyleClass().add("timer-text");
        numLabel.setLayoutX(GAME_WIDTH / 2 - 25);
        numLabel.setLayoutY(GAME_HEIGHT / 2 - 25);

        Platform.runLater(() -> root.getChildren().add(numLabel));
        for (int i = 5; i > 0; i--) {
            displayNum(numLabel, i);
            Thread.sleep(1000);
        }
        Platform.runLater(() -> root.getChildren().remove(numLabel));
    }

    private void displayNum(Label numLabel, int currentNum) {
        Platform.runLater(() -> {
            root.getChildren().remove(numLabel);
            numLabel.setText(String.valueOf(currentNum));
            root.getChildren().add(numLabel);
        });
    }

    public void checkGameStatus() {
        if (p1Score >= 10 || p2Score >= 10) {
            gameOver = true;
            winner = p1Score >= 10 ? p1Name : p2Name;
        }
    }

    public void displayWinner() {
        Label winnerLabel = new Label(winner + " Wins!");
        winnerLabel.getStyleClass().add("winner-text");
        winnerLabel.setLayoutX(GAME_WIDTH / 2 - winnerLabel.getWidth() / 2);
        winnerLabel.setLayoutY(GAME_HEIGHT / 2);
        Platform.runLater(() -> root.getChildren().add(winnerLabel));
    }

    private boolean ballCollide(Paddle p) {
        return ball.getRect().intersects(p.getX(), p.getY(), p.getWidth(), p.getHeight());
    }

    public void render() {
        if (p1Name == null) {
            p1Name = "Player 1";
            p2Name = "Player 2";
        }
        Platform.runLater(() -> {
            p1ScoreLabel.setText(p1Name + ": " + p1Score);
            p2ScoreLabel.setText(p2Name + ": " + p2Score);
            root.getChildren().removeAll(p1Paddle.getRect(), p2Paddle.getRect(), ball.getRect());
            root.getChildren().addAll(p1Paddle.getRect(), p2Paddle.getRect(), ball.getRect());
        });
    }

}
