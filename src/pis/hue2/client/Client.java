package pis.hue2.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Closeable {
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientID;

    public Client(Socket socket, String clientID) {
        this.socket = socket;
        this.clientID = clientID;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        //Instruction ack = Instruction.ACK;
        String exercise = "ACK";
        if (exercise.equals(Instruction.ACK.toString())) {
            System.out.println("SA");
        }

    }


    public void writeMessage() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (socket.isConnected()) {
            String message = scanner.nextLine();
            try {
                bufferedWriter.write(message);
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error occurred while sending a message!!");
            } finally {
                close();
            }
        }
    }


    public void receiveMessage() throws IOException {
        String receivedMessage;
        while (socket.isConnected()) {
            try {
                receivedMessage = bufferedReader.readLine();
                System.out.println(receivedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }

        }
    }

    @Override
    public void close() throws IOException {
        if (bufferedReader != null && bufferedWriter != null && socket != null) {
            bufferedReader.close();
            bufferedWriter.close();
            socket.close();
        } else {
            System.err.println("Could not close one of the streams!");
        }
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
