/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author admin
 */
public class Main {

    public static final int SERVER_PORT = 6969;
    public static final String HOST_NAME = "localhost";
    public static Socket connection;
    public static InputStream inputStream;
    public static OutputStream outputStream;
    public static String username, group;
    public static LoginForm loginForm = new LoginForm();
    public static GroupPanel groupPanel = new GroupPanel();
    public static Map<String, GroupChat> groupChats = new HashMap<>();

    public static void init() throws IOException {
        connection = new Socket(HOST_NAME, SERVER_PORT);

        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();

        //Runtime.getRuntime().addShutdownHook(new ShutdownThread(connection, dataOutputStream));

        ReadThread readThread = new ReadThread(connection);
        readThread.start();
    }

    public static String read() {
        byte[] buffer = new byte[1024];
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        try {
            dataInputStream.read(buffer);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        String s = new String(buffer, StandardCharsets.UTF_8).trim();
        System.out.println(s);
        return s;
    }

    public static void write(String stringOut) {
        stringOut = stringOut.trim();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        try {
            dataOutputStream.write(stringOut.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void login(String u) {
        username = u;
        write("Login [" + u + "]");
    }

    public static void getGroups() {
        write("Groups");
    }

    public static void createGroup(String g) {
        group = g;
        write("Create [" + g + "]");
    }

    public static void joinGroup(String g) {
        group = g;
        write("Join [" + g + "]");
    }

    public static void openGroup(String g) {
        if (groupChats.get(g) == null) {
            groupChats.put(g, new GroupChat(g));
        }
        groupChats.get(g).setVisible(true);
    }

    public static void leaveGroup(String g) {
        write("Leave [" + g + "]");
    }

    public static void chatGroup(String g, String m) {
        write("GText [" + g + "] " + m);
    }

    public static void toGroup(int c, String m) {
        int pos = m.indexOf("]");
        String g = m.substring(1, pos);
        m = m.substring(pos + 2);
        pos = m.indexOf("]");
        String u = m.substring(1, pos);
        m = m.substring(pos + 2);
        if (groupChats.get(g) != null) {
            groupChats.get(g).updateChat(c, u, m);
        }
    }

    public static void logout() {
        write("Logout");
    }

    public static void main(String[] args) throws IOException {
        init();
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        loginForm.setVisible(true);
    }

}
