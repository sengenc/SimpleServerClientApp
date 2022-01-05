package pis.hue2.server;

import pis.hue2.client.ServerWorker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server implements Closeable, Runnable {
    private static DataInputStream dataInputStream = null;
    private static FileOutputStream fileOutputStream = null;
    private FileInputStream fileInputStream;
    private DataOutputStream dataOutputStream;
    private static ServerSocket serverSocket = null;
    private static Socket socket = null;

    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;
    public static final int PORT = 2239;


    private final ArrayList<MyFile> filesStored = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.run();
    }


//    public void regulateServer() throws IOException {
//        System.out.println("regulateservercalled");
//        inputStreamReader = new InputStreamReader(socket.getInputStream());
//        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//        bufferedReader = new BufferedReader(inputStreamReader);
//        bufferedWriter = new BufferedWriter(outputStreamWriter);
//        String input = bufferedReader.readLine();
//        String[] arr = input.split(" ",2);
//            switch (arr[0]) {
//                case "LST":
//                    System.out.println("list server giris");
//                    listFiles();
//                    break;
//                case "PUT":
//                    System.out.println("Server regulate server");
//                    System.out.println(arr[1] + "do we come here");
//                    saveFiles(arr[1]);
//                    break;
//                case "GET":
//                    serverUpload("C:\\Users\\Berkay\\Desktop\\" + arr[1]);
//                default:
//                    System.out.println("DEFAULT!!!");
//
//            }
//        close();
//    }

    @Override
    public void run(){
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                System.out.println("Server started");
                socket = serverSocket.accept();
                System.out.println("startservercalled");
                ServerWorker serverWorker = new ServerWorker(socket);

                Thread thread = new Thread(serverWorker);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void listFiles() throws IOException {
        try {
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        for (int i = 0; i < filesStored.size(); i++) {
            bufferedWriter.write(filesStored.get(i).getName());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
    }

//    public void saveFiles(String path) throws IOException {
//        try {
//            dataInputStream = new DataInputStream(socket.getInputStream());
//            fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + path);
//            byte[] buffer = new byte[8192];
//            int read;
//            while ((read = dataInputStream.read(buffer)) > 0) {
//                fileOutputStream.write(buffer, 0, read);
//            }
//            filesStored.add(new MyFile(path, new byte[8192]));
//        } catch (IOException e) {
//            e.printStackTrace();
//            close();
//        }
//        close();
//    }
//
//    public void serverUpload(String fileName) throws IOException {
//        File file = new File(fileName);
//        System.out.println("upload1");
//        if (file.exists()) {
//            dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            System.out.println("upload2");
//            fileInputStream = new FileInputStream(file.getAbsolutePath());
//            byte[] buffer = fileName.getBytes();
//            int read;
//            while ((read = fileInputStream.read(buffer)) > 0) {
//                dataOutputStream.write(buffer, 0, read);
//            }
//            System.out.println("upload3");
//        } else {
//            System.out.println("This file does not exist!");
//        }
//        System.out.println("upload4");
//        close();
//    }

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


