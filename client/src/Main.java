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
    public static String user, group, file, username;
    public static LoginForm loginForm;
    public static GroupPanel groupPanel;
    public static Map<String, GroupChat> groupChats = new HashMap<>();
    public static Map<String, UserChat> userChats = new HashMap<>();
    public static int posX = 0, posY = 0;

    public static void init() throws IOException {
        connection = new Socket(HOST_NAME, SERVER_PORT);

        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();

        Runtime.getRuntime().addShutdownHook(new ShutdownThread(connection, outputStream));

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
        System.out.println(stringOut);
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        try {
            dataOutputStream.write(stringOut.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void newLocation() {
        posX += 20;
        if(posX >= 1000) posX = 0;
        posY += 20;
        if(posY >= 700) posY = 0;
    }

    public static void login(String u) {
        username = u;
        write("Login [" + u + "]");
        groupPanel.setTitle("Logged in as: " + username);
    }
    
    //User functions
    public static void getUsers() {
        write("Users");
    }
    public static void openUser(String u) {
        if (userChats.get(u) == null) {
            userChats.put(u, new UserChat(u));
        }
        userChats.get(u).setVisible(true);
    }
    public static void leaveUser(String u) {
        userChats.remove(u);
    }
    public static void chatUser(String u, String m) {
        write("UText [" + u + "] " + m);
        userChats.get(u).updateChat(120, username, m);
    }
    public static void toUser(int c, String m) {
        int pos = m.indexOf("]");
        pos = m.indexOf("]");
        String u = m.substring(1, pos);
        m = m.substring(pos + 2);
        openUser(u);
        userChats.get(u).updateChat(c, u, m);
    }
    public static void filelistUser(String u) {
        user = u;
        write("UFile " + u);
    }
    public static void fileUser(String u, File file) {
        FileInputStream stream = null;
        try {
            write("UPut [" + u + "] [" + file.getName() + "] " + file.length());
            stream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            int n = 0;
            while ((n = stream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, n);
            }
            stream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public static void downloadUser(String u, String f) {
        file = f;
        write("UGet [" + u + "] [" + f + "]");
    }
    
    // Group functions
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
        groupChats.remove(g);
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
        openGroup(g);
        groupChats.get(g).updateChat(c, u, m);
    }
    public static void filelistGroup(String g) {
        group = g;
        write("GFile " + g);
    }
    public static void fileGroup(String g, File file) {
        FileInputStream stream = null;
        try {
            write("GPut [" + g + "] [" + file.getName() + "] " + file.length());
            stream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            int n = 0;
            while ((n = stream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, n);
            }
            stream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public static void downloadGroup(String g, String f) {
        file = f;
        write("GGet [" + g + "] [" + f + "]");
    }

    public static void logout() {
        write("Logout");
    }

    public static void main(String[] args) throws IOException {
        init();
        try {
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatIntelliJLaf());
        } catch (Exception ex) {
            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        loginForm = new LoginForm();
        groupPanel = new GroupPanel();
        loginForm.setVisible(true);
    }

}
