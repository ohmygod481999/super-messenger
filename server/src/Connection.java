import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {

    private Socket socketConnection;
    private String id;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private ReadThread readThread;
    private WriteThread writeThread;

    public Connection(Socket socketConnection) throws IOException {
        this.socketConnection = socketConnection;
        this.id = null;
        this.dataInputStream = new DataInputStream(socketConnection.getInputStream());
        this.dataOutputStream = new DataOutputStream(socketConnection.getOutputStream());
        readThread = new ReadThread(this);
        writeThread = new WriteThread(this);
        readThread.start();
        writeThread.start();
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

    public void addWriter(Writer writer) {
        writeThread.addWriter(writer);
    }

    public void close() throws IOException {
        this.socketConnection.close();
        readThread.interrupt();
        writeThread.interrupt();
        Server.removeConnection(id);
    }
}
