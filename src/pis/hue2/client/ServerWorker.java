package pis.hue2.client;

import pis.hue2.common.BasicMethods;
import pis.hue2.server.MyFile;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


/**
 * @author ardasengenc
 */
public class ServerWorker implements Runnable, Closeable, BasicMethods {

    private static final ArrayList<ServerWorker> clients = new ArrayList<>();
    //List<Thread> serverWorker
    private final Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private FileOutputStream fileOutputStream = null;
    private FileInputStream fileInputStream = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
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
        clients.add(this);
        int workerID = 1;
        workerID++;
        sendMessage("A client has joined.");
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
        dataInputStream = new DataInputStream(socket.getInputStream());
        fileOutputStream = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = dataInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, read);
        }
    }

    @Override
    public void upload(String fileName) throws IOException {
        File file = new File(fileName);
        System.out.println("upload1");
        if (file.exists()) {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("upload2");
            fileInputStream = new FileInputStream(file.getAbsolutePath());
            byte[] buffer = fileName.getBytes();

            int read;
            while ((read = fileInputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, read);
            }
            System.out.println("upload3");
        } else {
            System.out.println("This file does not exist!");
            close();
        }
        System.out.println("upload4");
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
                    case "LST":
                        sendMessage(Instruction.ACK.toString());
                        break;
                    case "PUT":
                        sendMessage(Instruction.ACK.toString());
                        download(arr[1]);
                        break;
                    case "GET":
                        sendMessage(Instruction.ACK.toString());
                        break;
                    case "DEL":
                        sendMessage(Instruction.ACK.toString());
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
