package pis.hue2.client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author ardasengenc
 */
public class ClientPool implements Runnable, Closeable {
    private static final ArrayList<ClientPool> clients = new ArrayList<>();
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientPool(Socket socket) throws IOException {
        this.socket = socket;
        try {
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        clients.add(this);
        System.out.println(username + " has entered the chat!");

    }


    public void removeClient(Client client) {
        if (!clients.isEmpty()) {
            clients.remove(client);
            System.out.println(client.getClientID() + " has left the chat!");
        } else {
            System.out.println("Nobody is in the room!");
        }
    }

    public void uploadFiles() throws IOException {
        File[] files = new File[1];
        try {
            FileInputStream fileInputStream = new FileInputStream(files[0].getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(socket.getOutputStream()));
            String fileName = files[0].getName();
            if (fileName.contains(Instruction.ACK.toString())) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }


    }

    public void deleteFiles() {

    }

    public void showFiles() {

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
        if (bufferedReader != null && bufferedWriter != null && socket != null) {
            bufferedReader.close();
            bufferedWriter.close();
            socket.close();
        } else {
            System.err.println("Could not close one of the streams!");
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
}
