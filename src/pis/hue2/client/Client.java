package pis.hue2.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static pis.hue2.client.ClientPool.scanner;

public class Client implements Closeable {
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;



    public Client(Socket socket) {
        this.socket = socket;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientFunctions() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] arr = input.split("\\s+");


        switch (arr[0]) {
            case "CON":
                clientConnected();
                break;
            case "LST":
                break;
            case "PUT":
                upload(arr[1]);
                break;
            case "GET":
                break;
            case "DEL":
                break;
            case "DAT":
                break;
            default:
                break;
        }

    }

    public void clientConnected() throws IOException {
        try {
            while (socket.isConnected()) {
                String messageToSend = Instruction.CON.toString();
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            close();
        }
    }

    public void listFiles() {

    }

    public void upload(String fileName) {
        FileInputStream fileInputStream;
        DataOutputStream dataOutputStream;
        File file = new File(fileName);

        while (socket.isConnected()) {
            try {
                fileInputStream = new FileInputStream(file);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                String name = file.getName();
                byte[] fileNameBytes = name.getBytes();

                byte[] fileContentBytes = new byte[(int) file.length()];
                fileInputStream.read(fileContentBytes);

                dataOutputStream.writeInt(fileNameBytes.length);
                dataOutputStream.write(fileNameBytes);

                dataOutputStream.writeInt(fileContentBytes.length);
                dataOutputStream.write(fileContentBytes);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    public void download() {

    }

    public void deleteFile() {

    }

    public void toByte() {

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


    public static void main(String[] args) throws IOException {
        //Instruction ack = Instruction.ACK;
        String exercise = "ACK";
        if (exercise.equals(Instruction.ACK.toString())) {
            System.out.println("SA");
        }
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket);


    }
}
