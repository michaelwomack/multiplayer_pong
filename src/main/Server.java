package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
                    new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

                    Socket player2 = serverSocket.accept();
                    Platform.runLater(() -> textArea.appendText("Player 2's IP address: "
                            + player2.getInetAddress().getHostAddress() + '\n'));

                    new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                    Platform.runLater(() -> textArea.appendText(new Date() +
                            ": Starting a new thread for Session " + sessionNo++ + '\n'));

                    //Session handler in new thread
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

        private boolean continuePlaying = true;

        public SessionHandler(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }


        @Override
        public void run() {
            try {
                fromPlayer1 = new DataInputStream(player1.getInputStream());
                toPlayer1 = new DataOutputStream(player1.getOutputStream());
                fromPlayer2 = new DataInputStream(player2.getInputStream());
                toPlayer2 = new DataOutputStream(player2.getOutputStream());

                //Send notification to start countdown on client side
                toPlayer1.writeInt(1);
                toPlayer2.writeInt(1);


                /*
                 * Should continously run to check for game completion
                 * and send updated positions to clients
                 */


                while (true) {

                    int player1YPos = fromPlayer1.readInt();
                    int player2YPos = fromPlayer2.readInt();
                    int player1VelY = fromPlayer1.readInt();
                    int player2VelY = fromPlayer2.readInt();

                    toPlayer1.writeInt(player2YPos);
                    toPlayer2.writeInt(player1YPos);
                    toPlayer1.writeInt(player2VelY);
                    toPlayer2.writeInt(player1VelY);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //implement method for checking game win.

    }

    public static void main(String[] args) {
        launch(args);
    }
}
