import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ReadThread extends Thread {
    private Socket connectionSocket;
    private DataInputStream dataInputStream;
    public ReadThread(Socket connectionSocket) throws IOException {
        this.connectionSocket = connectionSocket;
        this.dataInputStream = new DataInputStream(connectionSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                int n = this.dataInputStream.read(buffer);
                String stringIn = new String(buffer, StandardCharsets.UTF_8);
                System.out.println("Server: " + stringIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
