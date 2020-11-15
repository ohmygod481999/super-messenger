import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static final int SERVER_PORT = 6969;
    public static final String HOST_NAME = "localhost";

    public static void main(String[] args) throws IOException {
        Socket connection = new Socket(HOST_NAME, SERVER_PORT);

        InputStream inputStream = connection.getInputStream();
        OutputStream outputStream = connection.getOutputStream();

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        ReadThread readThread = new ReadThread(connection);
        readThread.start();

        while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String stringOut = bufferedReader.readLine();
            stringOut = stringOut.trim();
            dataOutputStream.write(stringOut.getBytes());

            if (stringOut.equals("quit")) {
                break;
            }
        }

    }
}



