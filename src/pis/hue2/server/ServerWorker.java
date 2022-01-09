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
    private final Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    private PrintStream chatPrintWriter = null;
    private static Date date = new Date();


    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
        clients.add(this);
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


    @Override
    public void download(String fileName) {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            fileName = clientData.readUTF();

            OutputStream output = new FileOutputStream("C:\\Users\\arda\\Desktop\\dir\\" + fileName);
            long fileSize = clientData.readLong();
            byte[] downloadBufferServer = new byte[8192];
            while (fileSize != 0 && (bytesRead = clientData.read(downloadBufferServer, 0, (int) Math.min(downloadBufferServer.length, fileSize))) != -1) {
                output.write(downloadBufferServer, 0, bytesRead);
                fileSize -= bytesRead;
            }

            output.close();
            clientData.close();


        } catch (IOException err) {
            System.out.println("Exception: " + err);
        }

    }


    @Override
    public void upload(String fileName) {
        try {
            File myFile = new File("C:\\Users\\arda\\Desktop\\dir\\" + fileName);
            byte[] uploadBufferServer = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(uploadBufferServer, 0, uploadBufferServer.length);


            OutputStream os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(uploadBufferServer.length);
            dos.write(uploadBufferServer, 0, uploadBufferServer.length);
            dos.flush();
        } catch (Exception e) {
            System.err.println("File existiert nicht!");
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
            System.out.println(socket);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            chatPrintWriter = new PrintStream(socket.getOutputStream());
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
                        File files = new File("C:\\Users\\arda\\Desktop\\dir\\");

                        String[] list = files.list();

                        for (int i = 0; i < list.length; i++) {
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
            err.printStackTrace();
        }
    }

    public synchronized void deleteFile(String fileName) throws IOException {
        File file = new File("C:\\Users\\arda\\Desktop\\dir\\" + fileName);
        if (file.exists()) {
            Files.delete(file.toPath());
            System.out.println("Datei " + fileName + " wurde gelöscht. " + date);
        } else {
            System.err.println("Datei existiert nicht!");
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
