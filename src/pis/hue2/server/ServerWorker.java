package pis.hue2.server;

import pis.hue2.common.Instruction;
import pis.hue2.common.BasicMethods;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


/**
 * @author ardasengenc
 */
public class ServerWorker implements Runnable, Closeable, BasicMethods {

    private static final ArrayList<ServerWorker> clients = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader = null;
    private PrintStream chatPrintWriter = null;


    static Date date = new Date();


    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
    }

    @Override
    public synchronized void download(String fileName) {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            fileName = clientData.readUTF();

            OutputStream output = new FileOutputStream("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
            long fileSize = clientData.readLong();
            long temp = fileSize;
            byte[] buffer = new byte[8192];
            while (fileSize != 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                output.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }
            System.out.println("download() while sonrasi");

            chatPrintWriter.println(Instruction.ACK);

            output.close();
            clientData.close();

            System.out.println("Datei " + fileName + " mit " + temp + " Bytes wurden empfangen. " + date);
        } catch (IOException err) {
            chatPrintWriter.println(Instruction.DND);
            System.out.println("Exception: " + err);

        }

    }

    @Override
    public synchronized void upload(String fileName) {
        try {
            File myFile = new File("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);  //handle file reading
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);


            OutputStream os = socket.getOutputStream();  //handle file send over socket

            DataOutputStream dos = new DataOutputStream(os); //Sending file name and file size to the server
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("Datei " + fileName + " wurde geschickt. " + date);
        } catch (Exception e) {
            System.err.println("File does not exist!");
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
    public synchronized void run() {
        try {
            System.out.println(socket);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            chatPrintWriter = new PrintStream(socket.getOutputStream(), true);
            String message;
            while ((message = bufferedReader.readLine()) != null) {
                switch (message) {
                    case "CON":
                        chatPrintWriter = new PrintStream(socket.getOutputStream(), true);
                        if (socket.isConnected()) {
                            chatPrintWriter.println(Instruction.ACK);
                        } else {
                            chatPrintWriter.println(Instruction.DND);
                        }
                        chatPrintWriter.close();
                        continue;
                    case "PUT":
                        String file;
                        while ((file = bufferedReader.readLine()) != null) {
                            System.out.println("PUT ACK ici");
                            //download(file);
                            break;
                        }
                        chatPrintWriter.println(Instruction.ACK);

                        while (Objects.equals(bufferedReader.readLine(), Instruction.DAT.toString())) {
                            System.out.println("DAT ici");
                            download(file);
                            break;
                        }

                        System.out.println("ack ustu");

                        System.out.println("ack alti");

                        System.out.println("ack close");

                        continue;
                    case "GET":
                        String fileName;

                        while ((fileName = bufferedReader.readLine()) != null) {
                            System.out.println("GET ici");
                            break;
                        }

                        chatPrintWriter.println(Instruction.ACK);

                        if (bufferedReader.readLine().equals(Instruction.ACK.toString())) {
                            chatPrintWriter.println(Instruction.DAT);
                            upload(fileName);
                        }

                        System.out.println(bufferedReader.readLine());

                        continue;
                    case "DEL":
                        chatPrintWriter = new PrintStream(socket.getOutputStream(), true);
                        while ((fileName = bufferedReader.readLine()) != null) {
                            deleteFile(fileName);
                            break;
                        }
                        chatPrintWriter.println("DEL: " + Instruction.ACK);
                        chatPrintWriter.close();
                        continue;
                    case "LST":
                        //chatPrintWriter = new PrintWriter(socket.getOutputStream(), true);
                        //chatPrintWriter.println(Instruction.ACK);
                        System.out.println("LST alti");
                        chatPrintWriter.println(Instruction.ACK);
                        File files = new File("C:\\Users\\Berkay\\Desktop\\dir\\");

                        String[] list = files.list();

                        for (int i = 0; i<list.length; i++) {
                            chatPrintWriter.println(list[i]);
                        }

                        chatPrintWriter.close();
                        continue;
                    case "DSC":
                        chatPrintWriter.println(Instruction.DSC);

                        break;
                    case "QUIT":
                        close();
                        System.exit(0);
                        break;
                }
            }
        } catch (IOException err) {

        }
    }

    public synchronized void deleteFile(String fileName) throws IOException {
        File file = new File("C:\\Users\\Berkay\\Desktop\\dir\\" + fileName);
        if (file.exists()) {
            Files.delete(file.toPath());
            System.out.println("Datei " + fileName + " wurde gelöscht. " + date);
        } else {
            System.err.println("Cannot delete the file is not exist!!");
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
        if (bufferedReader != null && chatPrintWriter != null && socket != null) {
            bufferedReader.close();
            chatPrintWriter.close();
            socket.close();
        } else {
            System.err.println("Problem in Clientpool!");
        }
    }

}
