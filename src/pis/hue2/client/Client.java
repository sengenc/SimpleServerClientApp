package pis.hue2.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientID;

    public Client(Socket socket, String clientID) {
        this.socket = socket;
        this.clientID = clientID;
    }

    public static void main(String[] args) {
        //Instruction ack = Instruction.ACK;
        String exercise = "ACK";
        if (exercise.equals(Instruction.ACK.toString())) {
            System.out.println("SA");
        }

    }
}
