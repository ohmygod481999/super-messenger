import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Main {
    public static final int SERVER_PORT = 6969;

    public static void main(String[] args) throws IOException {
        // Create Socket
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Socket socketConnection = null;

        System.out.println("Waiting for a client...");
        while (true) {
            socketConnection = serverSocket.accept();

            Connection connection = new Connection(socketConnection);
//            onlineConnections.add(connection);
            ConnectionThread connectionThread = new ConnectionThread(connection);
            connectionThread.start();
        }
    }
}
