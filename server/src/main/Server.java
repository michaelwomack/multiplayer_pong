package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import main.util.DatagramUtils;

import java.io.*;
import java.net.*;

import java.util.Date;

public class Server extends Application implements PongConstants {
    private static int sessionNo = 1;
    private static int port = 8000;
    private static TextArea textArea;

    @Override
    public void start(Stage primaryStage) {
        textArea = new TextArea();
        Scene scene = new Scene(new ScrollPane(textArea));
        primaryStage.setTitle("Pong Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                Platform.runLater(() -> textArea.appendText("Server started...\n\n"));
                while (true) {
                    Platform.runLater(() -> textArea.appendText("Current session running on port " + port + "\n"));

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

    public static int getSessionNo() {
        return sessionNo;
    }

    public static int getPort() {
        return port;
    }

    public static TextArea getTextArea() {
        return textArea;
    }

    class SessionHandler implements Runnable, PongConstants {
        private Socket player1, player2;
        private DatagramSocket socket;
        private int port = getPort();
        private int sessionNo = getSessionNo() - 1;

        private ObjectOutputStream toPlayer1;
        private ObjectInputStream fromPlayer1;
        private ObjectOutputStream toPlayer2;
        private ObjectInputStream fromPlayer2;

        public SessionHandler(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.port += this.sessionNo;
        }


        @Override
        public void run() {

            try {

                toPlayer1 = new ObjectOutputStream(player1.getOutputStream());
                toPlayer2 = new ObjectOutputStream(player2.getOutputStream());

                fromPlayer1 = new ObjectInputStream(player1.getInputStream());
                fromPlayer2 = new ObjectInputStream(player2.getInputStream());

                /* Send clients their player number */
                toPlayer1.writeObject(PLAYER1);
                toPlayer2.writeObject(PLAYER2);

                String p1Name = (String) fromPlayer1.readObject();
                String p2Name = (String) fromPlayer2.readObject();

                toPlayer1.writeObject(p2Name);
                toPlayer2.writeObject(p1Name);

                /* write sessionNo to match port of unique server socket */
                toPlayer1.writeObject(this.sessionNo);
                toPlayer2.writeObject(this.sessionNo);

                /* Listen for packets at specified port */
                socket = new DatagramSocket(this.port);
                DatagramUtils util = new DatagramUtils(socket);

                /* Get packets for player1 client and player2 client */
                DatagramPacket player1Packet = util.receiveData();
                int playerNo = (int) util.deserializeData(player1Packet.getData());
                System.out.println("Data from player 1: " + util.deserializeData(player1Packet.getData()));

                DatagramPacket player2Packet = util.receiveData();
                System.out.println("Data from player 2: " + util.deserializeData(player2Packet.getData()));
                int playerNo2 = (int) util.deserializeData(player2Packet.getData());

                // Packets may be delivered out of order, if so, switch players
                if (playerNo == 2 && playerNo2 == 1) {
                    DatagramPacket tempPacket = player2Packet;
                    player2Packet = player1Packet;
                    player1Packet = tempPacket;
                }

                System.out.println("If switch, new order:\n");
                System.out.println("Data from player 1: " + util.deserializeData(player1Packet.getData()));
                System.out.println("Data from player 2: " + util.deserializeData(player2Packet.getData()));

                DatagramPacket receivedPacket1, receivedPacket2;
                GameObjectPositions obj1, obj2;
                int gameStatus = 0;

                while (gameStatus != PLAYER1_WON || gameStatus != PLAYER2_WON) {

                    /* Read data from players packets */
                    receivedPacket1 = util.receiveData();
                    receivedPacket2 = util.receiveData();

                    /* deserialize into objects */
                    //obj1 = (GameObjectPositions) util.deserializeData(receivedPacket1.getData());
                    //obj2 = (GameObjectPositions) util.deserializeData(receivedPacket2.getData());

//                    byte[] data1 = receivedPacket1.getData();
//                    byte[] data2 = receivedPacket2.getData();

                    /* send data from packet1 to packet2 */
//                    util.sendData(util.serializeData(obj1),
//                            receivedPacket2.getAddress(),
//                            receivedPacket2.getPort());
                    util.sendData(receivedPacket1.getData(),
                            receivedPacket2.getAddress(),
                            receivedPacket2.getPort());


                    /* send data from packet2 to packet1 */
//                    util.sendData(util.serializeData(obj2),
//                            receivedPacket1.getAddress(),
//                            receivedPacket1.getPort());

                    util.sendData(receivedPacket2.getData(),
                            receivedPacket1.getAddress(),
                            receivedPacket1.getPort());

                    /* Just in case packet order not correct, use player 1's */
//                    if (obj1.getFromPlayer() == 1)
//                        gameStatus = obj1.getGameStatus();
//                    else
//                        gameStatus = obj2.getGameStatus();

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
