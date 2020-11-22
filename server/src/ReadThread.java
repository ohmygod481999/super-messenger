import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;

public class ReadThread extends Thread {

    private Connection connection;
    private Matcher matcher;

    public ReadThread(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        try {
            while (!interrupted()) {
                int n = connection.getDataInputStream().read(buffer);
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
                    connection.addWriter(new Writer.Text("400 Invalid command"));
                }
            }
        } catch (Exception e) {
            try {
                this.connection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        System.out.println("Connection id: " + connection.getId() + " - read thread stopped");
    }

    // pattern: Groups
    private void onGroups() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("441 You haven't logged in yet"));
            return;
        }
        StringBuilder builder = new StringBuilder("241 ");
        for (Group group : Server.getGroups()) {
            builder.append("/").append(group.getName());
        }
        if (builder.indexOf("/") != -1) {
            builder.deleteCharAt(4);
        }
        connection.addWriter(new Writer.Text(builder.toString()));
    }

    // pattern: Users [(2)?]
    private void onUsers() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("440 You haven't logged in yet"));
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
                connection.addWriter(new Writer.Text("440 " + matcher.group(2) + " not exists"));
                return;
            }
            for (Connection connection : group.getConnections()) {
                builder.append("/").append(connection.getId());
            }
        }
        if (builder.indexOf("/") != -1) {
            builder.deleteCharAt(4);
        }
        connection.addWriter(new Writer.Text(builder.toString()));
    }

    // pattern: GGet [(1)] [(2)]
    private void onGGet() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("435 You haven't logged in yet"));
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.addWriter(new Writer.Text("435 " + matcher.group(1) + " not exists"));
            return;
        }
        if (group.getConnection(connection.getId()) == null) {
            connection.addWriter(new Writer.Text("435 You have not joined " + group.getName() + " yet"));
            return;
        }
        String name = matcher.group(1) + "_" + matcher.group(2);
        File file = new File(name);
        if (!file.exists()) {
            connection.addWriter(new Writer.Text("435 " + matcher.group(2) + " not exists"));
            return;
        }
        connection.addWriter(new Writer.File("235", name));
    }

    // pattern: GFile [(1)] [(2)] (3)
    private void onGFile() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("434 You haven't logged in yet"));
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.addWriter(new Writer.Text("434 " + matcher.group(1) + " not exists"));
            return;
        }
        if (group.getConnection(connection.getId()) == null) {
            connection.addWriter(new Writer.Text("434 You have not joined " + group.getName() + " yet"));
            return;
        }
        String name = group.getName() + "_" + matcher.group(2);
        synchronized (Server.getFileLock(name)) {
            try {
                File file = new File(name);
                if (!file.createNewFile()) {
                    System.out.println("Override " + name);
                }
                int size = Integer.parseInt(matcher.group(3));
                FileOutputStream stream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                while (size > 0) {
                    int n = connection.getDataInputStream().read(buffer);
                    stream.write(buffer, 0, n);
                    size -= n;
                }
                stream.close();
                connection.addWriter(new Writer.Text("234 Ok"));
                group.addWriter(new Writer.Text("134 [" + group.getName() + "] [" + connection.getId() + "] had uploaded " + matcher.group(2)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // pattern: GText [(1)] (2)
    private void onGText() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("433 You haven't logged in yet"));
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.addWriter(new Writer.Text("433 " + matcher.group(1) + " not exists"));
            return;
        }
        if (group.getConnection(connection.getId()) == null) {
            connection.addWriter(new Writer.Text("433 You have not joined " + group.getName() + " yet"));
            return;
        }
        group.addWriter(new Writer.Text("133 [" + group.getName() + "] [" + connection.getId() + "] " + matcher.group(2)));
        connection.addWriter(new Writer.Text("233 Ok"));
    }

    // pattern: Leave [(1)]
    private void onLeave() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("432 You haven't logged in yet"));
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.addWriter(new Writer.Text("432 " + matcher.group(1) + " not exists"));
            return;
        }
        if (group.getConnection(connection.getId()) == null) {
            connection.addWriter(new Writer.Text("432 You have not joined " + group.getName() + " yet"));
            return;
        }
        group.outGroup(connection);
        group.addWriter(new Writer.Text("132 [" + group.getName() + "] [" + connection.getId() + "] has left"));
        connection.addWriter(new Writer.Text("232 Ok"));
    }

    // pattern: Join [(1)]
    private void onJoin() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("431 You haven't logged in yet"));
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.addWriter(new Writer.Text("431 " + matcher.group(1) + " not exists"));
            return;
        }
        if (group.getConnection(connection.getId()) != null) {
            connection.addWriter(new Writer.Text("431 You have already joined"));
            return;
        }
        connection.addWriter(new Writer.Text("231 Ok"));
        group.joinGroup(connection);
        group.addWriter(new Writer.Text("131 [" + group.getName() + "] [" + connection.getId() + "] has joined"));
    }

    // pattern: Create [(1)]
    private void onCreate() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("430 You haven't logged in yet"));
            return;
        }
        if (Server.getGroup(matcher.group(1)) != null) {
            connection.addWriter(new Writer.Text("430 " + matcher.group(1) + " already exists"));
            return;
        }
        Group group = new Group(matcher.group(1));
        if (Server.addGroup(group)) {
            connection.addWriter(new Writer.Text("230 Ok"));
            group.joinGroup(connection);
            return;
        }
        connection.addWriter(new Writer.Text("430 Error"));
    }

    // pattern: UGet [(1)] [(2)]
    private void onUGet() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("422 You haven't logged in yet"));
            return;
        }
        Connection sender = Server.getConnection(matcher.group(1));
        if (sender == null) {
            connection.addWriter(new Writer.Text("422 " + matcher.group(1) + " not exists"));
            return;
        }
        String name = matcher.group(2);
        if (connection.getId().compareTo(sender.getId()) > 0) {
            name = connection.getId() + "_" + sender.getId() + "_" + name;
        } else {
            name = sender.getId() + "_" + connection.getId() + "_" + name;
        }
        File file = new File(name);
        if (!file.exists()) {
            connection.addWriter(new Writer.Text("422 " + matcher.group(2) + " not exists"));
            return;
        }
        connection.addWriter(new Writer.File("222", name));
    }

    // pattern: UFile [(1)] [(2)] (3)
    private void onUFile() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("421 You haven't logged in yet"));
            return;
        }
        Connection receiver = Server.getConnection(matcher.group(1));
        if (receiver == null) {
            connection.addWriter(new Writer.Text("421 " + matcher.group(1) + " not exists"));
            return;
        }
        String name = matcher.group(2);
        if (connection.getId().compareTo(receiver.getId()) > 0) {
            name = connection.getId() + "_" + receiver.getId() + "_" + name;
        } else {
            name = receiver.getId() + "_" + connection.getId() + "_" + name;
        }
        synchronized (Server.getFileLock(name)) {
            try {
                File file = new File(name);
                if (!file.createNewFile()) {
                    System.out.println("Override " + name);
                }
                int size = Integer.parseInt(matcher.group(3));
                FileOutputStream stream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                while (size > 0) {
                    int n = connection.getDataInputStream().read(buffer);
                    stream.write(buffer, 0, n);
                    size -= n;
                }
                stream.close();
                connection.addWriter(new Writer.Text("221 Ok"));
                receiver.addWriter(new Writer.Text("121 [" + connection.getId() + "] had uploaded " + matcher.group(2)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // pattern: UText [(1)] (2)
    private void onUText() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("420 You haven't logged in yet"));
            return;
        }
        Connection receiver = Server.getConnection(matcher.group(1));
        if (receiver == null) {
            connection.addWriter(new Writer.Text("420 " + matcher.group(1) + " not exists"));
            return;
        }
        receiver.addWriter(new Writer.Text("120 [" + connection.getId() + "] " + matcher.group(2)));
        connection.addWriter(new Writer.Text("220 Ok"));
    }

    // pattern: Logout
    private void onLogout() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("411 You haven't logged in yet"));
            return;
        }
        Server.removeConnection(connection.getId());
        connection.login(null);
    }

    // pattern: Login [(1)]
    private void onLogin() {
        if (connection.isLogin()) {
            connection.addWriter(new Writer.Text("410 You have already logged in"));
            return;
        }
        String id = matcher.group(1);
        if (id.isBlank()) {
            connection.addWriter(new Writer.Text("410 Username cannot be blank"));
            return;
        }
        if (Server.getConnection(id) != null) {
            connection.addWriter(new Writer.Text("410 " + id + " already exists"));
            return;
        }
        connection.login(id);
        if (Server.addConnection(connection)) {
            connection.addWriter(new Writer.Text("210 Ok"));
            return;
        }
        connection.login(null);
        connection.addWriter(new Writer.Text("410 Error"));
    }
}
