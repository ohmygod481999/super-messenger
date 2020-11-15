import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ShutdownThread extends Thread {
    private Socket connection;
    private DataOutputStream dataOutputStream;
    public ShutdownThread(Socket connection, DataOutputStream dataOutputStream) {
        this.connection = connection;
        this.dataOutputStream = dataOutputStream;
    }
    @Override
    public void run() {
        try {
            dataOutputStream.write(new String("quit").getBytes());
            System.out.println("shutdown...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
