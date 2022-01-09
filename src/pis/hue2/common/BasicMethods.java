package pis.hue2.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public interface BasicMethods {
    void download(String fileName) throws IOException;
    void upload(String fileName) throws IOException;
}
