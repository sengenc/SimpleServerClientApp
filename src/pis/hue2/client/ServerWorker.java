package pis.hue2.client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


/**
 * @author ardasengenc
 */
public class ServerWorker implements Runnable, Closeable {

    private static final ArrayList<ServerWorker> clients = new ArrayList<>();
    private final Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private FileOutputStream fileOutputStream = null;
    private FileInputStream fileInputStream = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    /**
     * muss thread sein, brauch eine WorkerID mit einer Liste (threadsicher)
     * im clientpool gibt es auch ne run() mit switch-case anweisungen und die lesen Instructions
     * wenn zb dsc kommt, macht brake und socket.close()
     * wenn DEL, brake und wieder while() loop
     * nutzt von der gemeins. Interface nutzt die operationen wie zb readFile() sendFile()
     * @param socket
     * @throws IOException
     */

    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            clients.add(this);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }


    }

    public void removeClient() {
        if (!clients.isEmpty()) {
            clients.remove(this);
        } else {
            System.out.println("Nobody is in the room!");
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
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    //   socket.close();
                    return;
                } else {
                    out.writeBytes(line + "\n\r");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

//    public void run() {
//        while (socket.isConnected()) {
//            try {
//                dataOutputStream = new DataOutputStream(socket.getOutputStream());
//
//                fileInputStream = new FileInputStream(socket.getInputStream().toString());
//
//
//                byte[] buffer = new byte[8192];
//                int read;
//                try {
//                    while ((read = fileInputStream.read(buffer)) > 0) {
//                        dataOutputStream.write(buffer, 0, read);
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }catch (IOException eee) {
//                    eee.printStackTrace();
//                }
//            } catch (IOException err)  {
//                err.printStackTrace();
//            }
//        }
//    }



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
