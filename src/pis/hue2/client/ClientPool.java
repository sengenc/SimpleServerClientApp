package pis.hue2.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author ardasengenc
 */
public class ClientPool implements Runnable, Closeable {
    private static ArrayList<Client> clients = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientPool(Socket socket) {
        this.socket = socket;
    }


    public void removeClient(Client client) {
        if (!clients.isEmpty()) {
            clients.remove(client);
            System.out.println(client.getClientID() + " has left the chat!");
        } else {
            System.out.println("Nobody is in the room!");
        }
    }

    public void uploadFiles() {

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
