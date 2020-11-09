import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static final int SERVER_PORT = 6969;

    public static void main(String[] args) throws IOException {
        // Create Socket
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Socket connection = null;

        System.out.println("Waiting for a client...");
        connection = serverSocket.accept();

        System.out.println("Client " + connection.getInetAddress() + " connected");
        InputStream inputStream = connection.getInputStream();
        OutputStream outputStream = connection.getOutputStream();

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        byte[] buffer = new byte[1024];
        dataInputStream.read(buffer);
        String ipString = new String(buffer, StandardCharsets.UTF_8);

        System.out.println(ipString);
    }
}
