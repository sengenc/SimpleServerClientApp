package pis.hue2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server implements Closeable {
    private static DataInputStream dataInputStream = null;
    private static FileOutputStream fileOutputStream = null;
    private static ServerSocket serverSocket = null;
    private static Socket socket = null;

    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;
    public static final int PORT = 2023;


    private final ArrayList<MyFile> filesStored = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }


    public void regulateServer() throws IOException {
        System.out.println("regulateservercalled");
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);
        String input = bufferedReader.readLine();
        String[] arr = input.split(" ",2);
        System.out.println(arr[0]+ "  arr[0]");
            switch (arr[0]) {
                case "LST":
                    listFiles();
                    break;
                case "PUT":
                    System.out.println("Server regulate server");
                    System.out.println(arr[1] + "do we come here");
                    saveFiles(arr[1]);
                    break;
                default:
                    System.out.println("DEFAULT!!!");

            }
        close();
    }

    public void startServer() throws IOException {
        try {
            while (true) {
                System.out.println("Server started");
                serverSocket = new ServerSocket(PORT);
                socket = serverSocket.accept();
                System.out.println("startservercalled");
                regulateServer();


            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }


    }

    public void listFiles() throws IOException {
        try {
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            inputStreamReader = new InputStreamReader(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);
        for (MyFile myFile : filesStored) {
            bufferedWriter.write(myFile.getName());
            bufferedWriter.newLine();
            bufferedWriter.flush();

        }
    }

    public void saveFiles(String path) throws IOException {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + path);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = dataInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, read);
            }
            filesStored.add(new MyFile(path, new byte[dataInputStream.readInt()]));

        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        close();
    }

    @Override
    public void close() throws IOException {
        if (dataInputStream != null && fileOutputStream != null && serverSocket != null && socket != null) {
            dataInputStream.close();
            fileOutputStream.close();
            serverSocket.close();
            socket.close();
        }
    }
}


