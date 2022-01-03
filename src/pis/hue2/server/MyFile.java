package pis.hue2.server;

/**
 * @author ardasengenc
 */
public class MyFile {

    private String name;
    private byte[] data;

    public MyFile(String name, byte[] data) {

        this.name = name;
        this.data = data;
    }


    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


}
