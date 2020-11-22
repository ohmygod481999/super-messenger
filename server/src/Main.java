import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int SERVER_PORT = 6969;

    public static void main(String[] args) throws IOException {
        // Create Socket
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Socket socketConnection = null;

        System.out.println("Waiting for a client...");
        while (true) {
            socketConnection = serverSocket.accept();

            new Connection(socketConnection);
        }
    }
}
