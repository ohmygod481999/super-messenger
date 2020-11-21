import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket socketConnection;
    private String id;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public Connection(Socket socketConnection) throws IOException {
        this.socketConnection = socketConnection;
        this.id = null;
        this.dataInputStream = new DataInputStream(socketConnection.getInputStream());
        this.dataOutputStream = new DataOutputStream(socketConnection.getOutputStream());
    }

    public String getIpAddress() {
        return this.socketConnection.getInetAddress().toString();
    }

    public void login(String id) {
        this.id = id;
    }

    public Boolean isLogin() {
        return this.id != null;
    }

    public String getId() {
        return id;
    }

    public DataOutputStream getDataOutputStream() {
        return this.dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return this.dataInputStream;
    }

    public void write(String string) throws IOException {
        this.dataOutputStream.write(string.getBytes());
    }

    public int read(byte[] buffer) throws IOException {
        return this.dataInputStream.read(buffer);
    }

    public void close() throws IOException {
        this.socketConnection.close();
    }
}
