package main.util;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by michaelwomack on 4/13/16.
 */
public class DatagramUtils {
    private DatagramSocket socket;

    public DatagramUtils(DatagramSocket socket) {
        this.socket = socket;
    }

    public void sendData(byte[] data, InetAddress ipAddress, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        socket.send(packet);
    }

    public DatagramPacket receiveData() throws IOException {
        byte[] data = new byte[256];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        socket.receive(packet);
        return packet;
    }

    public Object deserializeData(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
        Object o = input.readObject();
        input.close();
        return o;
    }

    public byte[] serializeData(Object o) throws IOException {
        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
        ObjectOutput outputStream = new ObjectOutputStream(new BufferedOutputStream(outputArray));
        outputStream.writeObject(o);
        outputStream.flush();
        outputArray.close();
        outputStream.close();
        return outputArray.toByteArray();
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
}
