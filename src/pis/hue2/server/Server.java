package pis.hue2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Closeable {

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final int PORT = 5024;
    public static int clientID = 1;


    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
        server.run();
    }

    public void startServer() throws IOException {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }


    public void run() throws IOException {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            while (true) {
                System.out.println("Server started");
                clientSocket = serverSocket.accept();
                System.out.println(clientID + ". Client is here.");
                clientID++;
                // ServerWorker serverWorker = new ServerWorker(socket);
                //Thread thread = new Thread(new ServerWorker(socket));
                // thread.start();
                executorService.execute(new ServerWorker(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }


    @Override
    public void close() throws IOException {
        if (serverSocket != null && clientSocket != null) {
            serverSocket.close();
            clientSocket.close();
        }
    }
}


