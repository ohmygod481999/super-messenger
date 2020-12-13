import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
                int n = dataInputStream.read(buffer);
                String stringIn = new String(buffer, StandardCharsets.UTF_8).trim();
                System.out.println("Server: " + stringIn);
                if (stringIn.length() < 3) continue;
                int code = Integer.parseInt(stringIn.substring(0, 3));
                String message = "";
                if (stringIn.length() > 4) {
                    message = stringIn.substring(4);
                }
                System.out.println(message);
                if (message.equals("You haven't log in yet")) {
                    Main.loginForm.setVisible(true);
                    Main.groupPanel.setVisible(false);
                    javax.swing.JOptionPane.showMessageDialog(Main.loginForm, message);
                } else if (message.equals("You have already joined")) {
                    Main.openGroup(Main.group);
                } else if (code == 210 || message.equals("You have already logged in")) {
                    Main.loginForm.setVisible(false);
                    Main.groupPanel.setVisible(true);
                    Main.getUsers();
                    Main.getGroups();
                } else if (code == 120 || code == 121) {
                    Main.toUser(code, message);
                } else if (code == 221) {
                    Main.filelistUser(Main.user);
                } else if (code == 230) {
                    Main.getGroups();
                    Main.openGroup(Main.group);
                } else if (code == 231) {
                    Main.openGroup(Main.group);
                } else if (code == 131 || code == 132 || code == 133 || code == 134) {
                    Main.toGroup(code, message);
                } else if (code == 234) {
                    Main.filelistGroup(Main.group);
                } else if (code == 235 || code == 222) {
                    File file = new File(Main.file);
                    if (!file.createNewFile()) {
                        System.out.println("Override " + Main.file);
                    }
                    int size = Integer.parseInt(message);
                    FileOutputStream stream = new FileOutputStream(file);
                    while (size > 0) {
                        n = dataInputStream.read(buffer);
                        stream.write(buffer, 0, n);
                        size -= n;
                    }
                    stream.close();
                    javax.swing.JOptionPane.showMessageDialog(null, "Download complete!");
                } else if (code / 100 == 4) {
                    javax.swing.JOptionPane.showMessageDialog(null, message);
                } else if (code == 241) {
                    Main.groupPanel.setGroups(message.split("/"));
                } else if (code == 240) {
                    Main.setUsers(message.split("/"));
                } else if (code == 242) {
                    Main.setFilesUser(Main.user, message.split("/"));
                } else if (code == 243) {
                    Main.setFilesGroup(Main.group, message.split("/"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
