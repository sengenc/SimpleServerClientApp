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
 * Diese Klasse uebernimmt die Verantwortung und Funktionalitaet der Server-Klasse,
 * sodass der Server-Klasse Flexibilitaet und Parallelitaet geboten wird. Diese Klasse implementiert 3 Schnittstellen.
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

    /**
     * Diese Methode akzeptiert die Anfrage vom Client und speichert die gewuenschte Datei in der Serverdatei.
     * Wenn ein Client also eine Datei hochladen moechte, liest diese Methode zuerst die Daten und schreibt
     * dann die Datei in ein bestimmtes Verzeichnis. Es ist eine entsprechende Methode für den Client-Upload
     *
     * @param fileName String, ist ein String Objekt das den Namen der Datei darstellt
     */
    @Override
    public synchronized void download(String fileName) {
        try {
            int bytesRead;

            DataInputStream clientData = new DataInputStream(socket.getInputStream());

            fileName = clientData.readUTF();

            OutputStream output = new FileOutputStream("C:\\Users\\arda\\Desktop\\dir\\" + fileName);
            long fileSize = clientData.readLong();
            long temp = fileSize;
            byte[] buffer = new byte[8192];
            while (fileSize != 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                output.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }


            chatPrintWriter.println(Instruction.ACK);

            output.close();
            clientData.close();

            System.out.println("Datei " + fileName + " mit " + temp + " Bytes wurden empfangen. " + date);
        } catch (IOException err) {
            chatPrintWriter.println(Instruction.DND);
            System.out.println("Exception: " + err);

        }

    }

    /**
     * Diese Methode akzeptiert die Anfrage vom Client und speichert die gewuenschte Datei in der Serverdatei.
     * Wenn ein Client also eine Datei herunterladen moechte, liest diese Methode zuerst die Daten und schreibt
     * dann die Datei in ein bestimmtes Verzeichnis. Es ist eine entsprechende Methode für den Client-Download
     *
     * @param fileName ist ein String Objekt, das den Namen der Datei darstellt
     */
    @Override
    public synchronized void upload(String fileName) {
        try {
            File myFile = new File("C:\\Users\\arda\\Desktop\\dir\\" + fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);


            OutputStream os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("Datei " + fileName + " wurde geschickt. " + date);
        } catch (Exception e) {
            System.err.println("Datei existiert nicht!");
        }
    }


    /**
     * Diese Methode gibt dem Client eine Funktionalitaet, um Dateien vom Server loeschen zu koennen, solange
     * die Datei existiert.
     *
     * @param fileName ist ein String Objekt das den Namen der Datei darstellt
     * @throws IOException
     */
    public synchronized void deleteFile(String fileName) throws IOException {
        File file = new File("C:\\Users\\arda\\Desktop\\dir\\" + fileName);
        if (file.exists()) {
            Files.delete(file.toPath());
            System.out.println("Datei " + fileName + " wurde gelöscht. " + date);
        } else {
            System.err.println("Cannot delete the file is not exist!!");
        }
    }


    /**
     * Diese Methode verknuepft alle relevanten Server-Client-Methoden und regelt diese in Bezug auf TCP/UDP-Protokolle.
     * Für jede Funktionalitaet gibt es eine entsprechende Kommunikation zwischen Benutzer und Client. Die Funktion
     * befindet sich in der Run-Methode, die einen Thread bereitstellt, sodass die mehreren Clients unterschiedliche
     * Funktionen ausfuehren koennen und nicht aufeinander warten
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

                            download(file);
                            break;
                        }
                        continue;
                    case "GET":
                        String fileName;

                        while ((fileName = bufferedReader.readLine()) != null) {

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

        }
    }


    /**
     * Schliesst diesen Stream und gibt alle damit verbundenen Systemressourcen frei. Wenn der Stream bereits
     * geschlossen ist, hat das Aufrufen dieser Methode keine Auswirkung.
     *
     * @throws IOException, wenn ein I/O Fehler aufgetreten ist
     */
    @Override
    public void close() throws IOException {
        if (bufferedReader != null && chatPrintWriter != null && socket != null) {
            bufferedReader.close();
            chatPrintWriter.close();
            socket.close();
        } else {
            System.err.println("Ein Problem ist aufgetreten!");
        }
    }

}
