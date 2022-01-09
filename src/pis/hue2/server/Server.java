package pis.hue2.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class Server implements Closeable {

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final int PORT = 5024;
    private static boolean isRunning = true;


    public static void main(String[] args) {
        Server server = new Server();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                server.makeServerGUI();
            }
        });

    }

    public void makeServerGUI() {
        JFrame jFrame = new JFrame("Server");


        jFrame.setSize(300, 300);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel jLabel = new JLabel();
        JButton jbStartServer = new JButton("Start Server");
        JButton jbRunServer = new JButton("Run Server");

        jbStartServer.setMaximumSize(new Dimension(300, 100));
        jbRunServer.setMaximumSize(new Dimension(300, 100));
        jLabel.setMaximumSize(new Dimension(300, 100));


        jFrame.add(jbStartServer, Component.CENTER_ALIGNMENT);
        jFrame.add(jbRunServer, Component.CENTER_ALIGNMENT);
        jFrame.add(jLabel, Component.CENTER_ALIGNMENT);


        jFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isRunning = false;
            }
        });

        jbStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        jbRunServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runServer();
            }
        });

        jFrame.setVisible(true);
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runServer() {

        while (isRunning) {
            try {
                System.out.println("Server started");
                clientSocket = serverSocket.accept();

//                ExecutorService executorService  =  Executors.newFixedThreadPool(3);
//                executorService.execute(new ServerWorker(clientSocket));

                ServerWorker serverWorker = new ServerWorker(clientSocket);
                Thread thread = new Thread(serverWorker);
                thread.start();

            } catch (IOException err) {
                System.out.println("Error in connection");
            }
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


