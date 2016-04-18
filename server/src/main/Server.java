package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server extends Application implements PongConstants {
    private int sessionNo = 1;
    private TextArea textArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new ScrollPane(textArea));
        primaryStage.setTitle("Pong Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> textArea.appendText("Server running at port 8000\n"));

                while (true) {
                    Platform.runLater(() -> textArea.appendText(new Date()
                            + " : Waiting for players to join session " + sessionNo + '\n'));

                    Socket player1 = serverSocket.accept();
                    Platform.runLater(() -> textArea.appendText("Player 1's IP address: "
                            + player1.getInetAddress().getHostAddress() + '\n'));

                    Socket player2 = serverSocket.accept();
                    Platform.runLater(() -> textArea.appendText("Player 2's IP address: "
                            + player2.getInetAddress().getHostAddress() + '\n'));

                    Platform.runLater(() -> textArea.appendText(new Date() +
                            ": Starting a new thread for Session " + ++sessionNo + '\n'));

                    new Thread(new SessionHandler(player1, player2)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    class SessionHandler implements Runnable, PongConstants {
        private Socket player1, player2;

        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer1;
        private DataOutputStream toPlayer2;
        private DataInputStream fromPlayer2;

        public SessionHandler(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }


        @Override
        public void run() {

            try {
                toPlayer1 = new DataOutputStream(new BufferedOutputStream(player1.getOutputStream()));
                toPlayer2 = new DataOutputStream(new BufferedOutputStream(player2.getOutputStream()));

                toPlayer1.flush();

                System.out.println("Output streams created");

                fromPlayer1 = new DataInputStream(new BufferedInputStream(player1.getInputStream()));
                fromPlayer2 = new DataInputStream(new BufferedInputStream(player2.getInputStream()));

                System.out.println("Input streams created");
                toPlayer1.writeInt(PLAYER1);
                toPlayer1.flush();
                System.out.println("Player 1 no sent");
                toPlayer2.writeInt(PLAYER2);
                toPlayer2.flush();
                System.out.println("Player 2 no sent");

                String p1Name = fromPlayer1.readUTF();
                String p2Name = fromPlayer2.readUTF();

                toPlayer1.writeUTF(p2Name);
                toPlayer1.flush();
                toPlayer2.writeUTF(p1Name);
                toPlayer2.flush();

                int gameStatus = 0;
                while (gameStatus != PLAYER1_WON || gameStatus != PLAYER2_WON) {
                    /* Read Opponent Coordinates from both and Ball Data from Player 1 */
                    //ball x
                    toPlayer2.writeInt(fromPlayer1.readInt());
                    toPlayer2.flush();

                    //ball y
                    toPlayer2.writeInt(fromPlayer1.readInt());
                    toPlayer2.flush();

                    //ball x vel
                    toPlayer2.writeInt(fromPlayer1.readInt());
                    toPlayer2.flush();

                    //bal y vel
                    toPlayer2.writeInt(fromPlayer1.readInt());
                    toPlayer2.flush();

                    //p1
//                    int p1Score = fromPlayer1.readInt();
//                    int p2Score = fromPlayer2.readInt();

                    //paddle y from player 1
                    toPlayer2.writeInt(fromPlayer1.readInt());
                    toPlayer2.flush();

                    //paddle y vel from player 1
                    toPlayer2.writeInt(fromPlayer1.readInt());
                    toPlayer2.flush();

                    //paddle y from player 2
                    toPlayer1.writeInt(fromPlayer2.readInt());
                    toPlayer1.flush();

                    //paddle y vel from player 2
                    toPlayer1.writeInt(fromPlayer2.readInt());
                    toPlayer1.flush();

//
//                    gameStatus = p1Score >= 10 ? PLAYER1_WON : 0;
//                    gameStatus = p2Score >= 10 ? PLAYER2_WON : gameStatus;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
