import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final int SERVER_PORT = 6969;

    public static void main(String[] args) throws IOException {
        // Create Socket
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        Socket socketConnection = null;

        // Remove old files
        Files.createDirectories(Paths.get("Files"));
        deleteFolder(new File("Files"));

        while (true) {
            socketConnection = serverSocket.accept();

            new Connection(socketConnection);
        }
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                }
                file.delete();
            }
        }
    }
}
