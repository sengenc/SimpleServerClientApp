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

/**
 * Der Klassenserver erstellt ein Objekt, das die Grundprinzipien der TCP/UDP-Verbindung festlegt.
 * Die Klasse implementiert ein Interface namens 'Closeable'.
 */
public class Server implements Closeable {

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final int PORT = 5071;
    private static boolean isRunning = true;

    /**
     * Main-Methode mit der GUI mit Swing-Funktionen InvokeLater ermoeglicht es dem Benutzer,
     * Methoden in Swing-Klassen sicher aufzurufen, wenn der Benutzer anfangs nicht in der EventQueue laeuft.
     *
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                server.makeServerGUI();
            }
        });

    }

    /**
     * Diese Methode erstellt eine Server-GUI mit Swing-Komponenten. Es hat zwei JButtons, um den Server zu 'starten'
     * und 'auszufuehren'. Jlabel war unten geplant, um als Informationsdiagramm zu verwenden, das die Informationen
     * der gesendeten und empfangenen Dateien anzeigt
     */
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

    /**
     * Diese Methode startet den Server mit einer passenden Portnummer
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Diese Methode erstellt Verbindungen mit Clients. Es befindet sich in einer Endlosschleife und innerhalb
     * der Schleife akzeptieren die Methoden die Thread-Klasse, um den Clients mehrere Clients
     * und mehrere Funktionen bereitzustellen
     */
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


    /**
     * Schliesst diesen Stream und gibt alle damit verbundenen Systemressourcen frei. Wenn der Stream bereits
     * geschlossen ist, hat das Aufrufen dieser Methode keine Auswirkung.
     *
     * @throws IOException, wenn ein I/O Fehler aufgetreten ist
     */
    @Override
    public void close() throws IOException {
        if (serverSocket != null && clientSocket != null) {
            serverSocket.close();
            clientSocket.close();
        }
    }
}


