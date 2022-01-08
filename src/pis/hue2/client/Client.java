package pis.hue2.client;

import pis.hue2.common.BasicMethods;
import pis.hue2.common.Instruction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


public class Client implements Closeable, BasicMethods {
    private Socket socket;
    public static final int PORT = 5024;
    static JList jList;
    File fileToSend = null;

    private static BufferedReader userMessage;
    private static BufferedReader chatFromClient;
    private static String fileName;
    private static PrintStream printStream;

    public Client(Socket socket) {
        this.socket = socket;
    }


    //ACK YOLLARKEN GET/LST ayir
    public void makeGUI() {
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


        jFrame.add(jbConnect);
        jFrame.add(jbChooseFile);
        jFrame.add(jbSend);
        jFrame.add(jbList);
        jFrame.add(jbDisconnect);
        jFrame.add(jbRemove);
        jFrame.add(jbGet);

        jbConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientConnected();
                    sendMessage(Instruction.CON.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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

            }
        });

        jbList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jList = new JList(entries);
                // System.out.println(Arrays.toString(entries) + " try disi");
                SwingUtilities.updateComponentTreeUI(jFrame);

            }
        });

        jbRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.updateComponentTreeUI(jFrame); //bunu yazanin allahini yerim


            }
        });

        jbDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        jList = new JList(entries);
        jList.setPreferredSize(new Dimension(300, 300));
        jList.setSelectedIndex(0);
        jFrame.add(jList);
        //SwingUtilities.updateComponentTreeUI(jFrame); //swing.invokeLater eventDispatchThread (auch klausurrelevant)
        jFrame.setVisible(true);

    }

    @Override
    public void sendMessage(String message) {
//        try {
//            inputStreamReader = new InputStreamReader(socket.getInputStream());
//            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//
//            bufferedReader = new BufferedReader(inputStreamReader);
//            bufferedWriter = new BufferedWriter(outputStreamWriter);
//            bufferedWriter.write(message);
//            bufferedWriter.newLine();
//            bufferedWriter.flush();
//        } catch (IOException err) {
//            err.printStackTrace();
//        }
    }

    @Override
    public String receiveMessage() {
        String input = null;
//        try {
//            inputStreamReader = new InputStreamReader(socket.getInputStream());
//            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//
//            bufferedReader = new BufferedReader(inputStreamReader);
//            bufferedWriter = new BufferedWriter(outputStreamWriter);
//
//            input = bufferedReader.readLine();
//        } catch (IOException err) {
//            err.printStackTrace();
//        }
        return input;
    }

    @Override
    public void download(String fileName) {
        try {
            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            DataInputStream clientData = new DataInputStream(inputStream);

            fileName = clientData.readUTF();
            OutputStream outputStream = new FileOutputStream("C:\\Users\\arda\\Desktop\\client\\" + fileName);
            long fileSize = clientData.readLong();
            byte[] downloadBufferClient = new byte[8192];

            while (fileSize != 0 && (bytesRead = clientData.read(downloadBufferClient, 0, (int) Math.min(downloadBufferClient.length, fileSize))) != -1) {
                outputStream.write(downloadBufferClient, 0, bytesRead);
                fileSize -= bytesRead;
            }

            outputStream.close();
            inputStream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }



    public void uploadNew(String fileName) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);

        File file = new File(fileName);

        long length = file.length();
        dos.writeLong(length);

        String name = file.getName();
        dos.writeUTF(name);

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        int theByte = 0;
        while ((theByte = bis.read()) != -1)
            bos.write(theByte);

        dos.flush();

        //dos.close();
        // bis.close();

    }

    @Override
    public void upload(String fileName) {
        try {
            File myFile = new File(fileName); //"C:\\Users\\arda\\Desktop\\" +
            byte[] uploadBufferClient = new byte[(int) myFile.length()];


            FileInputStream uploadInputStream = new FileInputStream(myFile);
            BufferedInputStream uploadBufferedInput = new BufferedInputStream(uploadInputStream);


            DataInputStream uploadDataInput = new DataInputStream(uploadBufferedInput);
            uploadDataInput.readFully(uploadBufferClient, 0, uploadBufferClient.length);

            OutputStream outputStream = socket.getOutputStream();


            DataOutputStream uploadDataOutput = new DataOutputStream(outputStream);
            uploadDataOutput.writeUTF(myFile.getName());
            uploadDataOutput.writeLong(uploadBufferClient.length);
            uploadDataOutput.write(uploadBufferClient, 0, uploadBufferClient.length);
            uploadDataOutput.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

            switch (userMessage.readLine()) {
                case "CON":
                    printStream.println("CON");

                    String fromServer;
                    while ((fromServer = chatFromClient.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                    continue;
                case "LST":
                    printStream.println("LST");
                    System.out.println("list alti");

                    while ((fromServer = chatFromClient.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                    continue;
                case "PUT":
                    printStream.println("PUT");
                    fileName = userMessage.readLine();
                    printStream.println(fileName);
                    upload(fileName);

                    while ((fromServer = chatFromClient.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                    continue;
                case "GET":
                    printStream.println("GET");
                    fileName = userMessage.readLine();
                    printStream.println(fileName);
                    download(fileName);
                    continue;
                case "DEL":
                    printStream.println("DEL");
                    fileName = userMessage.readLine();
                    printStream.println(fileName);
                    continue;
                case "DSC":
                    printStream.println("DSC");
                    socket.close();
                    System.exit(0);
                default:
                    System.out.println("DEFAULT");
                    break;
            }
        }
    }

    public void clientConnected() throws IOException {
        try {
            socket = new Socket("localhost", PORT);
            System.out.println("Joined");
        } catch (IOException e) {
            close();
        }
    }


    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        } else {
            System.err.println("Could not close one of the streams!");
        }
    }

    public void listenForMessage() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String msgFromGroupChat;
//
//                while (socket.isConnected()) {
//                    try {
//                        msgFromGroupChat = bufferedReader.readLine();
//                        System.out.println(msgFromGroupChat);
//                    } catch (IOException e) {
//                        try {
//                            close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }).start();
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", PORT);
        Client client = new Client(socket);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    client.clientFunctions();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //client.makeGUI();
            }
        });
    }
}
