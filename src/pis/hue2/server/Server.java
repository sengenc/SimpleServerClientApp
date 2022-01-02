package pis.hue2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Closeable {
    private static DataInputStream dataInputStream = null;
    private static FileOutputStream fileOutputStream = null;
    private static ServerSocket serverSocket = null;
    private static Socket socket = null;

    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }

    public void startServer() throws IOException {
        try {
            while (true) {
                System.out.println("Server started");
                serverSocket = new ServerSocket(1992);
                socket = serverSocket.accept();
                saveFiles();
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }


    }

    public void saveFiles() throws IOException {
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            String fileName = bufferedReader.readLine();
            dataInputStream = new DataInputStream(socket.getInputStream());
            fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = dataInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, read);
            }

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


