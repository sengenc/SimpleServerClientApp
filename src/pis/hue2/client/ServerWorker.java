package pis.hue2.client;

import pis.hue2.common.BasicMethods;
import pis.hue2.server.MyFile;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;


/**
 * @author ardasengenc
 */
public class ServerWorker implements Runnable, Closeable, BasicMethods {

    private static final ArrayList<ServerWorker> clients = new ArrayList<>();
    private static ArrayList<MyFile> myFiles = new ArrayList<>();
    //List<Thread> serverWorker
    private final Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    /**
     * muss thread sein, brauch eine WorkerID mit einer Liste (threadsicher)
     * im clientpool gibt es auch ne run() mit switch-case anweisungen und die lesen Instructions
     * wenn zb dsc kommt, macht brake und socket.close()
     * wenn DEL, brake und wieder while() loop
     * nutzt von der gemeins. Interface nutzt die operationen wie zb readFile() sendFile()
     *
     * @param socket
     * @throws IOException
     */

    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendMessage("A client has joined.");
        clients.add(this);
        int workerID = 1;
        workerID++;

    }

    public void removeClient() {
        if (!clients.isEmpty()) {
            clients.remove(this);
        } else {
            System.out.println("Nobody is in the room!");
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
            input = bufferedReader.readLine();
        } catch (IOException err) {
            err.printStackTrace();
        }
        return input;
    }

//    @Override
//    public void download(String fileName) throws IOException {
//        try {
//            dataInputStream = new DataInputStream(socket.getInputStream());
//            fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
//            byte[] buffer = new byte[8192];
//            int read = dataInputStream.read(buffer);
//
//            sendMessage("mesaj server down dis " + read);
//            while (read > 0) {
//                System.out.println("server down");
//                sendMessage("mesaj server down ic");
//                fileOutputStream.write(buffer, 0, read);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            close();
//        }
//    }

    @Override
    public void download(String fileName) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
        byte[] buffer = new byte[8192];
        int read;
        myFiles.add(new MyFile(fileName, new byte[dataInputStream.readInt()]));
        while ((read = dataInputStream.read(buffer)) > -1) {
            fileOutputStream.write(buffer, 0, read);
        }
        dataInputStream.close();  // yeni eklendi
        fileOutputStream.close(); //yeni eklendi
        //run();
    }


    public void downloadNew() throws IOException {
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        long fileLength = dis.readLong();
        String fileName = dis.readUTF();

        File file = new File("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);

        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        int theByte = 0;
        while((theByte = bis.read()) != -1)
            bos.write(theByte);

        bos.flush();

//        bos.close();
//        dis.close();
    }

    @Override
    public void upload(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
            byte[] buffer = fileName.getBytes();
            int read;
            while ((read = fileInputStream.read(buffer)) > -1) {
                dataOutputStream.write(buffer, 0, read);
            }
            sendMessage(Instruction.ACK.toString());
            dataOutputStream.close(); //yeni eklendi
            fileInputStream.close(); //yeni eklendi
        } else {
            System.out.println("This file does not exist!");
            sendMessage(Instruction.DND.toString());
        }

    }


    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            while (true) {
                String input = receiveMessage();
                String[] arr = input.split(" ", 2);
                switch (arr[0]) {
                    case "CON":
                        if (socket.isConnected()) {
                            sendMessage(Instruction.ACK.toString());
                        } else {
                            sendMessage(Instruction.DND.toString());
                            try {
                                socket.close();
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                        }
                        break;
                    case "DSC":
                        sendMessage(Instruction.DSC.toString());
                        socket.close();
                        break;
                    case "ACK":
                        //ACK GET/LST
                        break;
                    case "LST":                                                     //SONSUZ DONGUYE GIRIYOR
                        //sendMessage(Instruction.ACK.toString());
                        listFiles();
                        break;
                    case "PUT":
                        sendMessage(Instruction.ACK.toString());
                        downloadNew();
                        break;
                    case "GET":
                        upload("C:\\Users\\Berkay\\Desktop\\dir\\" + arr[1]);
                        break;
                    case "DEL":
                       // sendMessage(Instruction.ACK.toString());
                        deleteFile(arr[1]);
                        break;
                    case "DAT":
                        sendMessage(Instruction.ACK.toString());
                        break;
                }
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    public void deleteFile(String fileName) throws IOException {
        File file = new File("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
        if (file.exists()) {
            Files.delete(file.toPath());
            for (int i = 0; i<myFiles.size(); i++) {
                if (myFiles.get(i).getName().equals(fileName)) {
                    myFiles.remove(i);
                }
            }
            System.out.println(fileName + " has been deleted!");
            sendMessage(Instruction.ACK.toString());
        } else {
            System.err.println("Cannot delete the file is not exist!!");
            sendMessage(Instruction.DND.toString());
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
        for (int i = 0; i < myFiles.size(); i++) {
            bufferedWriter.write(myFiles.get(i).getName());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        removeClient();
        if (inputStreamReader != null && outputStreamWriter != null && socket != null) {
            inputStreamReader.close();
            outputStreamWriter.close();
            socket.close();
        } else {
            System.err.println("Problem in Clientpool!");
        }
    }

}
