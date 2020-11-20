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
                String stringIn = new String(buffer, StandardCharsets.UTF_8).trim();
                System.out.println("Server: " + stringIn);
                if (stringIn.length() < 3) continue;
                int code = Integer.parseInt(stringIn.substring(0,3));
                String message = "";
                if (stringIn.length() > 4) {
                    message = stringIn.substring(4);
                }
                if (message == "You haven't log in yet") {
                    Main.loginForm.setVisible(true);
                    Main.groupPanel.setVisible(false);
                    javax.swing.JOptionPane.showMessageDialog(Main.loginForm, message);
                }
                else if (code == 210) {
                    Main.loginForm.setVisible(false);
                    Main.groupPanel.setVisible(true);
                    Main.getGroups();
                }
                else if (code == 410) {
                    javax.swing.JOptionPane.showMessageDialog(Main.loginForm, message);
                }
                else if (code == 211) {
                    Main.loginForm.setVisible(true);
                    Main.groupPanel.setVisible(false);
                }
                else if (code == 411) {
                    javax.swing.JOptionPane.showMessageDialog(Main.groupPanel, message);
                }
                else if (code == 230) {
                    Main.getGroups();
                    Main.openGroup(Main.group);
                }
                else if (code == 430) {
                    javax.swing.JOptionPane.showMessageDialog(Main.groupPanel, message);
                }
                else if (code == 231) {
                    Main.openGroup(Main.group);
                }
                else if (code == 431) {
                    javax.swing.JOptionPane.showMessageDialog(Main.groupPanel, message);
                    Main.getGroups();
                }
                else if (code == 131 || code == 132 || code == 133 || code == 134) {
                    Main.toGroup(code, message);
                }
                else if (code == 241) {
                    Main.groupPanel.setGroups(message.split("/"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
