package pis.hue2.client;

import pis.hue2.common.BasicMethods;
import pis.hue2.common.Instruction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

/**
 * Das Objekt der Klasse erzeugt einen Client, der mit dem Server kommuniziert. Dabei kann er
 * auch Daten hoch- und herunterladen, sehen, welche Daten zur Verfuegung stehen und diese Daten loeschen.
 */
public class Client implements Closeable, BasicMethods {
    private static Socket socket;
    private static BufferedReader userMessage;
    private static BufferedReader chatFromClient;
    private static String fileName;
    private static PrintStream printStream;
    public static final int PORT = 5071;

    File fileToSend = null;
    static Date date = new Date();
    private static JLabel statusLabel;
    private static JList jList;

    /**
     * Diese Methode dient dazu, dass der Client sich mit dem Server verbinden kann.
     * Falls der Server nicht antwortet, kommt eine Fehlermeldung und wird das Programm beendet.
     */
    public synchronized void connect() {
        try {
            socket = new Socket("localhost", PORT);
            userMessage = new BufferedReader(new InputStreamReader(System.in));
            printStream = new PrintStream(socket.getOutputStream(), true);
            chatFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }
    }

    /**
     * Diese Methode stellt eine Benutzerueberflaeche fuer den Client zur Verfuegung.
     */
    public void makeClientGUI() {
        try {
            socket = new Socket("localhost", PORT);
            userMessage = new BufferedReader(new InputStreamReader(System.in));
            printStream = new PrintStream(socket.getOutputStream());
            chatFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }


        JFrame jFrame = new JFrame("Client");

        jFrame.setSize(500, 500);
        jFrame.setLayout(new FlowLayout(FlowLayout.CENTER));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] entries = new String[10];

        JButton jbConnect = new JButton("Connect");
        JButton jbChooseFile = new JButton("Choose File");
        JButton jbSend = new JButton("Send");
        JButton jbList = new JButton("List");
        JButton jbDisconnect = new JButton("Disconnect");
        JButton jbRemove = new JButton("Remove");
        JButton jbGet = new JButton("Get");
        JButton jbQuit = new JButton("Quit");

//        statusLabel = new JLabel("Not Completed", JLabel.CENTER);
//        jFrame.add(statusLabel);

        jFrame.add(jbConnect);
        jFrame.add(jbChooseFile);
        jFrame.add(jbSend);
        jFrame.add(jbList);
        jFrame.add(jbDisconnect);
        jFrame.add(jbRemove);
        jFrame.add(jbGet);
        jFrame.add(jbQuit);



        jbConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printStream.println("CON");
                try {
                    String fromServer;
                    while ((fromServer = chatFromClient.readLine()) != null) {
                        JOptionPane.showMessageDialog(jFrame, "Server : " + fromServer);
                        System.out.println(fromServer);
                    }
                } catch (IOException er) {
                    er.printStackTrace();
                }

                connect();
            }
        });

        jbChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file to send");

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend = fileChooser.getSelectedFile();
                }
            }
        });

        jbSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileToSend.getName().isEmpty()) {
                    JOptionPane.showMessageDialog(jFrame, "Please choose a file first", "WARNING", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        printStream.println(Instruction.PUT);
                        printStream.println(fileToSend.getName());
                        //startGUISend(fileToSend.getAbsolutePath());

                        if (Objects.equals(chatFromClient.readLine(), Instruction.ACK.toString())) {
                            System.out.println("PUT ACK VE DAT");
                            printStream.println(Instruction.DAT);
                            upload(fileToSend.getAbsolutePath());
                        }
                        chatFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                        String fromServer = chatFromClient.readLine();

                        if (fromServer.equals(Instruction.ACK.toString())) {
                            System.out.println("SERVER: ACK");
                            long temp = fileToSend.length();
                            JOptionPane.showMessageDialog(jFrame, "Server : " + temp + " Bytes wurden geschickt.\n" + date);
                        } else if (fromServer.equals(Instruction.DND.toString())) {
                            System.out.println("SERVER: DND");

                        } else {
                            System.out.println("Kein feedback erhalten");
                        }

                        connect();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }

            }
        });

        jbList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printStream.println(Instruction.LST);


                String fromServer;
                try {
                    System.out.println("SERVER:" + chatFromClient.readLine());
                    int i = 0;
                    while ((fromServer = chatFromClient.readLine()) != null) {
                        entries[i] = fromServer;
                        i++;
                    }
                } catch (IOException err) {
                    err.printStackTrace();
                }
                jList = new JList(entries);
                SwingUtilities.updateComponentTreeUI(jFrame);
                connect();
            }
        });

        jbRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("C:\\Users\\Berkay\\Desktop\\dir\\");
                fileChooser.setDialogTitle("Choose a file to delete");
                fileChooser.setApproveButtonText("Delete");

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend = fileChooser.getSelectedFile();
                }
                printStream.println("DEL");
                printStream.println(fileToSend.getName());

                try {
                    System.out.println(chatFromClient.readLine());
                    JOptionPane.showMessageDialog(jFrame, "Server : " + fileToSend.getName() + " wurde gelöscht.\n" + date);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                connect();
            }
        });

        jbDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                printStream.println("DSC");

                try {
                    if (chatFromClient.readLine().equals(Instruction.DSC.toString())) {
                        socket.close();
                        System.exit(0);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });

        jbGet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("C:\\Users\\Berkay\\Desktop\\dir\\");
                fileChooser.setDialogTitle("Choose a file to download");
                fileChooser.setApproveButtonText("Download");

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend = fileChooser.getSelectedFile();
                }

                printStream.println("GET");
                printStream.println(fileToSend.getName());

                try {
                    if (Objects.equals(chatFromClient.readLine(), Instruction.ACK.toString())) {
                        System.out.println("get ack ici");
                        printStream.println(Instruction.ACK);

                    }

                    while (Objects.equals(chatFromClient.readLine(), Instruction.DAT.toString())) {
                        System.out.println("DAT ici");
                        download(fileName);
                        break;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                JOptionPane.showMessageDialog(jFrame, "Server : " + fileToSend.getName() + " wurde heruntergeladen.\n" + date);
                connect();
                //startGUIGet();
            }
        });

        jbQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printStream.println("QUIT");
                try {
                    socket.close();
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        jList = new JList(entries);
        jList.setPreferredSize(new Dimension(300, 300));
        jList.setSelectedIndex(0);
        jFrame.add(jList);
        SwingUtilities.updateComponentTreeUI(jFrame);
        jFrame.setVisible(true);
    }

    /**
     * Die Methode ist fuer die Situationen zustaendig, die lange dauern. Deswegen
     * wird diese Methode fuer das Hochladen der Datei benutzt und laeuft im Hintergrund,
     * bis die Datei erfolgreich hochgeladen ist.
     *
     * @param fileName ist der Pfad zu der Datei, die geschickt wird.
     */
    public void startGUISend(String fileName) {
        SwingWorker<Void, DataOutputStream> swingWorkerSend = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                upload(fileName);
                return null;
            }

            @Override
            protected void done() {
                System.out.println("SwingWorker upload done");

                statusLabel.setText("Upload done");


            }
        };
        swingWorkerSend.execute();
    }

    public void startGUIList() {
        SwingWorker<Void, Void> swingWorkerList = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {

                return null;
            }
        };
        swingWorkerList.execute();
    }

    /**
     * Die Methode ist fuer die Situationen zustaendig, die lange dauern. Deswegen
     * wird diese Methode fuer das Herunterladen der Datei benutzt und laeuft im
     * Hintergrund, bis die Datei erfolgreich hochgeladen ist.
     *
     * @param fileName ist der Name der Datei, die der Client herunterladen moechte.
     */
    public void startGUIGet(String fileName) {
        SwingWorker<Void, Void> swingWorkerGet = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                download(fileName);
                return null;
            }
        };
    }

    /**
     * Einfacher Konstruktor fuer die Klasse Client.
     *
     * @param socket ist der Socket, mit dem sich der Client verbinden muss.
     */
    public Client(Socket socket) {
        this.socket = socket;
    }

    /**
     * Diese Methode nimmt als Parameter einen Dateiname (Pfad zu der Datei) und schickt diese Datei
     * mithilfe von Stroemen zu dem Server. Wenn der Transfer erfolgreich ist, kommt eine positive Meldung,
     * wenn nicht, dann kommt eine Fehlermeldung.
     *
     * @param fileName ist der Pfad zu der Datei, die geschickt wird.
     */
    @Override
    public synchronized void upload(String fileName) {
        try {
            File myFile = new File(fileName); //"C:\\Users\\arda\\Desktop\\" +
            byte[] mybytearray = new byte[(int) myFile.length()];


            FileInputStream uploadInputStream = new FileInputStream(myFile);
            BufferedInputStream uploadBufferedInput = new BufferedInputStream(uploadInputStream);


            DataInputStream uploadDataInput = new DataInputStream(uploadBufferedInput);
            uploadDataInput.readFully(mybytearray, 0, mybytearray.length);

            OutputStream outputStream = socket.getOutputStream();


            DataOutputStream uploadDataOutput = new DataOutputStream(outputStream);
            uploadDataOutput.writeUTF(myFile.getName());
            uploadDataOutput.writeLong(mybytearray.length);
            uploadDataOutput.write(mybytearray, 0, mybytearray.length);
            uploadDataOutput.flush();
            System.out.println("File " + fileName + " wurde geschickt.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Diese Methode bekommt als Parameter den Dateinamen der Datei, die der Client vom Server
     * herunterladen moechte. Die Datei wird vom Server durch die Stroemen geschickt und wird von
     * dieser Methode gelesen. Wenn der Transfer erfolgreich ist, kommt eine positive Meldung,
     * wenn nicht, dann kommt eine Fehlermeldung.
     *
     * @param fileName ist der Name der Datei, die der Client herunterladen moechte.
     */
    @Override
    public synchronized void download(String fileName) {
        try {
            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            DataInputStream clientData = new DataInputStream(inputStream);

            fileName = clientData.readUTF();
            OutputStream outputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\client\\" + fileName);
            long fileSize = clientData.readLong();
            byte[] buffer = new byte[8192];

            while (fileSize != 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }

            printStream.println("GET: " + Instruction.ACK);

            outputStream.close();
            inputStream.close();

            System.out.println("File " + fileName + " wurde vom Server heruntergeladen.");
        } catch (IOException ex) {
            printStream.println("GET: " + Instruction.DND);
            ex.printStackTrace();
        }

    }

    /**
     * Mit dieser Methode kann man das Programm ohne GUI benutzen.
     * Der Client kann alle Befehle ins Terminal schreiben, die im Protokoll beschrieben wurden.
     *
     * @throws IOException wenn einer von den Stroemen einen Fehler auftritt
     */
    public void clientFunctions() throws IOException {
        while (true) {
            try {
                socket = new Socket("localhost", PORT);
                userMessage = new BufferedReader(new InputStreamReader(System.in));
            } catch (Exception e) {
                System.err.println("Cannot connect to the server, try again later.");
                System.exit(1);
            }
            printStream = new PrintStream(socket.getOutputStream());
            chatFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
                switch (userMessage.readLine()) {
                    case "CON":
                        printStream.println("CON");

                        String fromServer;
                        while ((fromServer = chatFromClient.readLine()) != null) {
                            System.out.println(fromServer);
                        }
                        continue;
                    case "PUT":
                        printStream.println("PUT");
                        System.out.print("Enter file name: ");
                        fileName = userMessage.readLine();
                        printStream.println(fileName);
                        upload(fileName);

                        while ((fromServer = chatFromClient.readLine()) != null) {
                            System.out.println(fromServer);
                        }
                        continue;
                    case "GET":
                        printStream.println("GET");
                        System.out.print("Enter file name: ");
                        fileName = userMessage.readLine();
                        printStream.println(fileName);
                        download(fileName);
                        continue;
                    case "DEL":
                        printStream.println("DEL");
                        System.out.print("Enter file name: ");
                        fileName = userMessage.readLine();
                        printStream.println(fileName);
                        continue;
                    case "LST":
                        printStream.println("LST");
                        System.out.println("list alti");

                        while ((fromServer = chatFromClient.readLine()) != null) {
                            System.out.println(fromServer);
                        }
                        continue;
                    case "DSC":
                        printStream.println("DSC");
                        socket.close();
                        System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("not valid input");
            }

        }
    }

    /**
     * Eine einfache Methode, die den Socket schliesst.
     * Wenn nicht, dann kommt eine Fehlermeldung.
     * @throws IOException wenn der Stream nicht beendet werden kann.
     */
    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        } else {
            System.err.println("Der Stream konnte nicht beendet werden.");
        }
    }

    /**
     * In der Main-Methode werden die Socket- und Client-Klassen initialisiert, und damit
     * die Klassen Client und Server threadsicher miteinander kommunizieren koennen, wird
     * SwingUtilities.invokeLater() aufgerufen.
     *
     * @param args
     * @throws IOException wenn einer von den Stroemen einen Fehler auftritt
     */
    public static void main(String[] args) throws IOException {


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket("localhost", PORT);
                    Client client = new Client(socket);
                    client.makeClientGUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
