import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class ConnectionThread extends Thread {
    private Connection connection;
    private Groups groups;

    public ConnectionThread (Connection connection, Groups groups) throws IOException {
        this.connection = connection;
        this.groups = groups;
    }

    private Group getGroupByName (String groupName) {
        Group targetGroup = null;
        for (Group group: this.groups.getGroups()) {
            if (group.getName().equals(groupName)) {
                targetGroup = group;
            }
        }
        return targetGroup;
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[1024];
            try {
                int n = connection.read(buffer);
                if (n == -1) {
                    this.connection.close();
                    break;
                }
                String readString = new String(buffer, StandardCharsets.UTF_8).trim();
                System.out.println("client: " + readString);

                String outString = "";

                if (!connection.isLogin()) {
                    if (readString.split(" ")[0].equals("login")) {
                        String id = readString.split(" ")[1];
                        Boolean success = this.connection.login(id);
                        if (success) {
                            outString = "210 OK";
                        }
                        else {
                            outString = "410 ERROR";
                        }
                    }
                    else {
                        outString = "411 YOU HAVEN'T LOGIN YET";
                    }
                }
                else {
                    if (readString.equals("groups")) {
                        outString = "241 " + groups.toString();
                    }
                    else if (readString.split(" ")[0].equals("join")) {
                        String groupName = readString.split(" ")[1];
                        Group targetGroup = getGroupByName(groupName);
                        if (targetGroup != null) {
                            targetGroup.joinGroup(this.connection);
                            outString = "231 OK";
                        }
                        else {
                            outString = "431 " + groupName + "not exists";
                        }
                    }
                    else if (readString.split(" ")[0].equals("users")) {
                        String groupName = readString.split(" ")[1];
                        Group targetGroup = getGroupByName(groupName);

                        if (targetGroup != null) {
                            outString = "243 " + targetGroup.getConnections().toString();
                        }
                        else outString = "440 " + groupName + " is not exists";
                    }
                    else if (readString.split(" ")[0].equals("create")) {
                        String groupName = readString.split(" ")[1];
                        Group group = new Group(groupName);
                        groups.add(group);
                        outString = "230 OK";
                    }
                    else if (readString.split(" ")[0].equals("gtext")) {
                        String groupName = readString.split(" ")[1];
                        String msg = readString.split(" ")[2];

                        Group targetGroup = getGroupByName(groupName);
                        if (targetGroup != null) {
                            targetGroup.sendMsg(this.connection, msg);
                            outString = "233 OK";
                        }
                        else {
                            outString = "433 " + groupName + " not found";
                        }
                    }
                    else {
                        outString = readString.toUpperCase();
                    }
                }


                connection.write(outString.getBytes());
                if (readString.equals("quit")) {
                    for (Group gr : this.groups.getGroups()) {
                        gr.outGroup(this.connection);
                    }
                    break;
                }
            }
            catch (SocketException e) {
                try {
                    this.connection.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                try {
                    connection.write(new String("Error: invalid command").getBytes());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
