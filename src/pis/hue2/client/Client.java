package pis.hue2.client;

import pis.hue2.common.BasicMethods;
import pis.hue2.server.MyFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


public class Client implements Closeable, BasicMethods {
    BufferedOutputStream outToClient = null;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public static final int PORT = 2239;
    private static Scanner scanner = new Scanner(System.in);

    static JList jList;
    File fileToSend = null;

    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;

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
                if (fileToSend.getName().isEmpty()) {
                    JOptionPane.showMessageDialog(jFrame, "Please choose a file first", "WARNING", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        sendMessage("PUT " + fileToSend.getName());
                        upload(fileToSend.getAbsolutePath());
                        System.out.println(fileToSend.getAbsolutePath());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                SwingUtilities.updateComponentTreeUI(jFrame);
            }
        });

        jbList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                    bufferedWriter = new BufferedWriter(outputStreamWriter);
                    bufferedWriter.write(Instruction.LST.toString());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    String output;
                    int i = 0;
                    while ((output = bufferedReader.readLine()) != null) {
                        entries[i] = output;
                        i++;
                    }

                    System.out.println(Arrays.toString(entries));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                jList = new JList(entries);
                // System.out.println(Arrays.toString(entries) + " try disi");
                SwingUtilities.updateComponentTreeUI(jFrame);

            }
        });

        jbRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.updateComponentTreeUI(jFrame); //bunu yazanin allahini yerim
                try {
                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                    bufferedWriter = new BufferedWriter(outputStreamWriter);
                    System.out.println("remove buton");
                    String temp = (String) jList.getSelectedValue();
                    int num = jList.getSelectedIndex() + 1;
                    System.out.println(temp);
                    System.out.println(num);
//                        bufferedWriter.write(Instruction.DEL + " " + num);
//                        bufferedWriter.newLine();
//                        bufferedWriter.flush();
                } catch (IOException er) {
                    er.printStackTrace();
                }

            }
        });

        jbDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                    bufferedWriter = new BufferedWriter(outputStreamWriter);

                    bufferedWriter.write(Instruction.DSC.toString());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    String output = bufferedReader.readLine();

                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        });
        jList = new JList(entries);
        jList.setPreferredSize(new Dimension(300, 300));
        jList.setSelectedIndex(0);
        jFrame.add(jList);
        //SwingUtilities.updateComponentTreeUI(jFrame); //swing.invokeLater eventDispatchThread (auch klausurrelevant)
        jFrame.setVisible(true);

    }

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    @Override
    public String receiveMessage() {
        String input = null;
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            input = bufferedReader.readLine();
        } catch (IOException err) {
            err.printStackTrace();
        }
        return input;
    }

    @Override
    public void download(String fileName) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\client\\" + fileName);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = dataInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, read);
        }
    }

//    @Override
//    public void upload(String fileName) throws IOException {
//        File file = new File(fileName);
//        System.out.println("upload1");
//        if (file.exists()) {
//            dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            System.out.println("upload2");
//            fileInputStream = new FileInputStream(file.getAbsolutePath());
//            byte[] buffer = fileName.getBytes();
//
//            int read;
//            while ((read = fileInputStream.read(buffer)) > 0) {
//                dataOutputStream.write(buffer, 0, read);
//            }
//            System.out.println("upload3");
//        } else {
//            System.out.println("This file does not exist!");
//            close();
//        }
//        System.out.println("upload4");
//    }


    public void uploadNew(String fileName) throws IOException {
        outToClient = new BufferedOutputStream(socket.getOutputStream());
        File file = new File(fileName);
        byte[] mybytearray = new byte[(int) file.length()];

        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fileInputStream);
        bis.read(mybytearray, 0, mybytearray.length);
        outToClient.write(mybytearray,0,mybytearray.length);
        outToClient.flush();
        return;
    }

    @Override
    public void upload(String fileName) throws IOException {
        File file = new File(fileName);
        System.out.println("upload1");
        if (file.exists()) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("upload2");
            FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
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
    }


    public void clientFunctions() throws IOException {
        while (true) {
            listenForMessage();
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            System.out.println("clientfun");
            String[] arr = input.split(" ", 2);
            switch (arr[0]) {
                case "CON":
                    sendMessage(Instruction.CON.toString());
                    clientConnected();
                    break;
                case "LST":
                    sendMessage("LST");
                    listFiles();
                    break;
                case "PUT":
                    sendMessage(input);
                    System.out.println("putsw");
                    uploadNew("C:\\Users\\Berkay\\Desktop\\" + arr[1]);
                    break;
                case "GET":
                    sendMessage(input);
                    download(arr[1]);
                    break;
                case "DEL":
                    sendMessage(input);
                    break;
                case "DAT":
                    break;
                default:
                    break;
            }
        }
    }

    public void clientConnected() throws IOException {
        try {
            socket = new Socket("localhost", PORT);
            System.out.println("Joined");
        } catch (IOException e) {
            //close();
        }
    }

    public void listFiles() throws IOException {
        System.out.println("listfiles");
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        String output;
        while ((output = bufferedReader.readLine()) != null) {
            System.out.println(output);
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

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        try {
                            close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }).start();
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
