package pis.hue2.client;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client implements Closeable {
    private static Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private FileInputStream fileInputStream;
    private DataOutputStream dataOutputStream;
    public static final int PORT = 2023;
    ;
    private static Scanner scanner = new Scanner(System.in);


    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;


    public void clientFunctions() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        System.out.println("clientfun");
        String[] arr = input.split(" ", 2);


        switch (arr[0]) {
            case "CON":
                clientConnected();
                break;
            case "LST":
                break;
            case "PUT":
                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                bufferedWriter.write(input);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                System.out.println("putsw");
                upload("C:\\Users\\Berkay\\Desktop\\" + arr[1]);
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
            System.out.println("Bitte Username Eingeben: ");
            String input = scanner.nextLine();
            socket = new Socket("localhost", PORT);
            System.out.println(input + " ist gerade beigetreten!");

        } catch (IOException e) {
            close();
        }
    }

    public void listFiles() {

    }

    public void upload(String fileName) throws IOException {
        File file = new File(fileName);
        System.out.println("upload1");
        if (file.exists()) {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("upload2");
            fileInputStream = new FileInputStream(file.getAbsolutePath());
            byte[] buffer = fileName.getBytes();

            int read;
            while ((read = fileInputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, read);
            }
            System.out.println("upload3");
        } else {
            System.out.println("This file does not exist!");
        }
        System.out.println("upload4");
        close();
    }


    public void download() {

    }

    public void deleteFile(File file) {
        // get directory?
        String name = file.getName();
        if (file.exists()) {
            file.delete();
            System.out.println(name + " has been deleted!");
        } else {
            System.err.println("Cannot delete the file is not exist!!");
        }
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
            //socket.close();
        } else {
            System.err.println("Could not close one of the streams!");
        }
    }


    public static void main(String[] args) throws IOException {
        Client client = new Client();
        while (true) {
            client.clientFunctions();
        }
    }
}
