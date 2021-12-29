package pis.hue2.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author ardasengenc
 */
public class Server implements Runnable, Closeable {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    static ArrayList<MyFile> myFiles = new ArrayList<>();

    public void startServer() throws IOException {
        int fileId = 0;

        while (true) {

            try {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                int fileNameLength = dataInputStream.readInt();

                if (fileNameLength > 0) {
                    byte[] fileNameBytes = new byte[fileNameLength];
                    dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                    String fileName = new String(fileNameBytes);

                    int fileContentLength = dataInputStream.readInt();
                    byte[] fileContentBytes = new byte[fileContentLength];
                    if (fileContentLength > 0) {

                        dataInputStream.readFully(fileContentBytes, 0, fileContentLength);
                        myFiles.add(new MyFile(fileId, fileName, fileContentBytes, getFileExtension(fileName)));
                        fileId++;
                    }

                    File fileToDownload = new File(fileName);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);

                        fileOutputStream.write(fileContentBytes);
                        fileOutputStream.close();


                    } catch (IOException err) {
                        err.printStackTrace();
                    }

                }
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    public static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');

        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "no extension found";
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
        if (serverSocket != null) {
            serverSocket.close();
        } else {
            System.err.println("Could not close ServerSocket!");
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

    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(new ServerSocket(1881));
        server.startServer();
    }
}
