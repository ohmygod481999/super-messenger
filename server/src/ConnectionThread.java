import java.io.*;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

public class ConnectionThread extends Thread {

    private Connection connection;
    private Matcher matcher;

    public ConnectionThread (Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                int n = connection.read(buffer);
                if (n == -1) {
                    this.connection.close();
                    break;
                }
                String readString = new String(buffer, 0, n, StandardCharsets.UTF_8).trim();
                System.out.println("client: " + readString);
                if ((matcher = CommandPattern.LOGIN.matcher(readString)).matches()) {
                    onLogin();
                } else if ((matcher = CommandPattern.LOGOUT.matcher(readString)).matches()) {
                    onLogout();
                } else if ((matcher = CommandPattern.UTEXT.matcher(readString)).matches()) {
                    onUText();
                } else if ((matcher = CommandPattern.UFILE.matcher(readString)).matches()) {
                    onUFile();
                } else if ((matcher = CommandPattern.UGET.matcher(readString)).matches()) {
                    onUGet();
                } else if ((matcher = CommandPattern.CREATE.matcher(readString)).matches()) {
                    onCreate();
                } else if ((matcher = CommandPattern.JOIN.matcher(readString)).matches()) {
                    onJoin();
                } else if ((matcher = CommandPattern.LEAVE.matcher(readString)).matches()) {
                    onLeave();
                } else if ((matcher = CommandPattern.GTEXT.matcher(readString)).matches()) {
                    onGText();
                } else if ((matcher = CommandPattern.GFILE.matcher(readString)).matches()) {
                    onGFile();
                } else if ((matcher = CommandPattern.GGET.matcher(readString)).matches()) {
                    onGGet();
                } else if ((matcher = CommandPattern.USERS.matcher(readString)).matches()) {
                    onUsers();
                } else if ((matcher = CommandPattern.GROUPS.matcher(readString)).matches()) {
                    onGroups();
                } else {
                    connection.write("400 Invalid command");
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
                    connection.write("Error: invalid command");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    // pattern: Groups
    private void onGroups() throws IOException {
        if (!connection.isLogin()) {
            connection.write("441 You haven't logged in yet");
            return;
        }
        StringBuilder builder = new StringBuilder("241 ");
        for (Group group : Server.getGroups()) {
            builder.append("/").append(group.getName());
        }
        if (builder.indexOf("/") != -1) {
            builder.deleteCharAt(4);
        }
        connection.write(builder.toString());
    }

    // pattern: Users [(2)?]
    private void onUsers() throws IOException {
        if (!connection.isLogin()) {
            connection.write("440 You haven't logged in yet");
            return;
        }
        StringBuilder builder = new StringBuilder("240 ");
        if (matcher.groupCount() < 2) {
            for (Connection connection : Server.getConnections()) {
                builder.append("/").append(connection.getId());
            }
        } else {
            Group group = Server.getGroup(matcher.group(2));
            if (group == null) {
                connection.write("440 " + matcher.group(2) + " not exists");
                return;
            }
            for (Connection connection : group.getConnections()) {
                builder.append("/").append(connection.getId());
            }
        }
        if (builder.indexOf("/") != -1) {
            builder.deleteCharAt(4);
        }
        connection.write(builder.toString());
    }

    private void onGGet() throws IOException {
        // TODO
    }

    private void onGFile() throws IOException {
        // TODO
    }

    // pattern: GText [(1)] (2)
    private void onGText() throws IOException {
        if (!connection.isLogin()) {
            connection.write("433 You haven't logged in yet");
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.write("433 " + matcher.group(1) + " not exists");
            return;
        }
        if (group.getConnection(connection.getId()) == null) {
            connection.write("433 You have not joined " + group.getName() + " yet");
            return;
        }
        group.sendMsg("133 [" + group.getName() + "] [" + connection.getId() + "] " + matcher.group(2));
        connection.write("233 Ok");
    }

    // pattern: Leave [(1)]
    private void onLeave() throws IOException {
        if (!connection.isLogin()) {
            connection.write("432 You haven't logged in yet");
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.write("432 " + matcher.group(1) + " not exists");
            return;
        }
        if (group.getConnection(connection.getId()) == null) {
            connection.write("432 You have not joined " + group.getName() + " yet");
            return;
        }
        group.outGroup(connection);
        group.sendMsg("132 [" + group.getName() + "] [" + connection.getId() + "] has left");
        connection.write("232 Ok");
    }

    // pattern: Join [(1)]
    private void onJoin() throws IOException {
        if (!connection.isLogin()) {
            connection.write("431 You haven't logged in yet");
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.write("431 " + matcher.group(1) + " not exists");
            return;
        }
        if (group.getConnection(connection.getId()) != null) {
            connection.write("431 You have already joined");
            return;
        }
        connection.write("231 Ok");
        group.joinGroup(connection);
        group.sendMsg("131 [" + group.getName() + "] [" + connection.getId() + "] has joined");
    }

    // pattern: Create [(1)]
    private void onCreate() throws IOException {
        if (!connection.isLogin()) {
            connection.write("430 You haven't logged in yet");
            return;
        }
        if (Server.getGroup(matcher.group(1)) != null) {
            connection.write("430 " + matcher.group(1) + " already exists");
            return;
        }
        Group group = new Group(matcher.group(1));
        if (Server.addGroup(group)) {
            connection.write("230 Ok");
            group.joinGroup(connection);
            return;
        }
        connection.write("430 Error");
    }

    private void onUGet() throws IOException {
        // TODO
    }

    private void onUFile() throws IOException {
        // TODO
    }

    // pattern: UText [(1)] (2)
    private void onUText() throws IOException {
        if (!connection.isLogin()) {
            connection.write("420 You haven't logged in yet");
            return;
        }
        Connection receiver = Server.getConnection(matcher.group(1));
        if (receiver == null) {
            connection.write("420 " + matcher.group(1) + " not exists");
            return;
        }
        receiver.write("120 [" + connection.getId() + "] " + matcher.group(2));
        connection.write("220 Ok");
    }

    // pattern: Logout
    private void onLogout() throws IOException {
        if (!connection.isLogin()) {
            connection.write("411 You haven't logged in yet");
            return;
        }
        Server.removeConnection(connection.getId());
        connection.login(null);
    }

    // pattern: Login [(1)]
    private void onLogin() throws IOException {
        if (connection.isLogin()) {
            connection.write("410 You have already logged in");
            return;
        }
        String id = matcher.group(1);
        if (id.isBlank()) {
            connection.write("410 Username cannot be blank");
            return;
        }
        if (Server.getConnection(id) != null) {
            connection.write("410 " + id + " already exists");
            return;
        }
        connection.login(id);
        if (Server.addConnection(connection)) {
            connection.write("210 Ok");
            return;
        }
        connection.login(null);
        connection.write("410 Error");
    }
}
