import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ShutdownThread extends Thread {
    private Socket connection;
    private OutputStream dataOutputStream;

    public ShutdownThread(Socket connection, OutputStream dataOutputStream) {
        this.connection = connection;
        this.dataOutputStream = dataOutputStream;
    }

    @Override
    public void run() {
        try {
            dataOutputStream.write(new String("Logout").getBytes());
            System.out.println("shutdown...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
