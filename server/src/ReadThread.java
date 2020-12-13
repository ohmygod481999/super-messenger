import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                } else if ((matcher = CommandPattern.UPUT.matcher(readString)).matches()) {
                    onUPut();
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
                } else if ((matcher = CommandPattern.GPUT.matcher(readString)).matches()) {
                    onGPut();
                } else if ((matcher = CommandPattern.GGET.matcher(readString)).matches()) {
                    onGGet();
                } else if ((matcher = CommandPattern.USERS.matcher(readString)).matches()) {
                    onUsers();
                } else if ((matcher = CommandPattern.GROUPS.matcher(readString)).matches()) {
                    onGroups();
                } else if ((matcher = CommandPattern.UFILE.matcher(readString)).matches()) {
                    onUFile();
                } else if ((matcher = CommandPattern.GFILE.matcher(readString)).matches()) {
                    onGFile();
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

    // pattern: GFile (1)
    private void onGFile() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("443 You haven't logged in yet"));
            return;
        }
        Group group = Server.getGroup(matcher.group(1));
        if (group == null) {
            connection.addWriter(new Writer.Text("443 " + matcher.group(1) + " not exists"));
            return;
        }
        File folder = new File(getDirectoryName(group));
        StringBuilder builder = new StringBuilder("243 ");
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    builder.append("/").append(file.getName());
                }
            }
            if (builder.indexOf("/") != -1) {
                builder.deleteCharAt(4);
            }
        }
        connection.addWriter(new Writer.Text(builder.toString().trim()));
    }

    // pattern: UFile (1)
    private void onUFile() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("442 You haven't logged in yet"));
            return;
        }
        Connection other = Server.getConnection(matcher.group(1));
        if (other == null) {
            connection.addWriter(new Writer.Text("442 " + matcher.group(1) + " not exists"));
            return;
        }
        File folder = new File(getDirectoryName(other));
        StringBuilder builder = new StringBuilder("242 ");
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    builder.append("/").append(file.getName());
                }
            }
            if (builder.indexOf("/") != -1) {
                builder.deleteCharAt(4);
            }
        }
        connection.addWriter(new Writer.Text(builder.toString().trim()));
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
        if (matcher.group(2) == null) {
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
        String directory = getDirectoryName(group);
        String name = matcher.group(2);
        File file = new File(directory, name);
        if (!file.exists()) {
            connection.addWriter(new Writer.Text("435 " + matcher.group(2) + " not exists"));
            return;
        }
        connection.addWriter(new Writer.File("235", directory + "/" + name));
    }

    // pattern: GPut [(1)] [(2)] (3)
    private void onGPut() {
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
        String directory = getDirectoryName(group);
        if (!createDirectory(directory)) {
            connection.addWriter(new Writer.Text("434 Error"));
            return;
        }
        String name = createFile(directory, matcher.group(2));
        if (name == null) {
            connection.addWriter(new Writer.Text("434 Error"));
            return;
        }
        synchronized (Server.getFileLock(directory + name)) {
            try {
                File file = new File(directory, name);
                int size = Integer.parseInt(matcher.group(3));
                readBufferAndWriteToFile(file, size);
                connection.addWriter(new Writer.Text("234 Ok"));
                group.addWriter(new Writer.Text("134 [" + group.getName() + "] [" + connection.getId() + "] had uploaded " + name));
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
        String directory = getDirectoryName(sender);
        String name = matcher.group(2);
        File file = new File(directory, name);
        if (!file.exists()) {
            connection.addWriter(new Writer.Text("422 " + matcher.group(2) + " not exists"));
            return;
        }
        connection.addWriter(new Writer.File("222", directory + "/" + name));
    }

    // pattern: UPut [(1)] [(2)] (3)
    private void onUPut() {
        if (!connection.isLogin()) {
            connection.addWriter(new Writer.Text("421 You haven't logged in yet"));
            return;
        }
        Connection receiver = Server.getConnection(matcher.group(1));
        if (receiver == null) {
            connection.addWriter(new Writer.Text("421 " + matcher.group(1) + " not exists"));
            return;
        }
        String directory = getDirectoryName(receiver);
        if (!createDirectory(directory)) {
            connection.addWriter(new Writer.Text("421 Error"));
            return;
        }
        String name = createFile(directory, matcher.group(2));
        if (name == null) {
            connection.addWriter(new Writer.Text("421 Error"));
            return;
        }
        synchronized (Server.getFileLock(directory + name)) {
            try {
                File file = new File(directory, name);
                int size = Integer.parseInt(matcher.group(3));
                readBufferAndWriteToFile(file, size);
                connection.addWriter(new Writer.Text("221 Ok"));
                receiver.addWriter(new Writer.Text("121 [" + connection.getId() + "] had uploaded " + name));
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

    private String getDirectoryName(Connection connection) {
        if (connection.getId().compareTo(this.connection.getId()) > 0) {
            return "Files/" + connection.getId() + "_" + this.connection.getId();
        } else {
            return "Files/" + this.connection.getId() + "_" + connection.getId();
        }
    }

    private void readBufferAndWriteToFile(File output, int size) throws Exception {
        FileOutputStream stream = new FileOutputStream(output);
        byte[] buffer = new byte[1024];
        while (size > 0) {
            int n = connection.getDataInputStream().read(buffer);
            stream.write(buffer, 0, n);
            size -= n;
        }
        stream.close();
    }

    private String getDirectoryName(Group group) {
        return "Files/" + group.getName();
    }

    private static synchronized boolean createDirectory(String name) {
        Path path = Paths.get(name);
        if (Files.exists(path)) {
            return true;
        }
        try {
            Files.createDirectories(path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static synchronized String createFile(String folder, String name) {
        try {
            File file = new File(folder, name);
            if (!file.exists()) {
                file.createNewFile();
                return name;
            } else {
                String extension = "";
                int index = name.lastIndexOf(".");
                if (index != -1) {
                    extension = name.substring(index);
                    name = name.substring(0, index);
                }
                index = 1;
                while (true) {
                    file = new File(folder, name + "(" + index + ")" + extension);
                    if (!file.exists()) {
                        file.createNewFile();
                        return name + "(" + index + ")" + extension;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
