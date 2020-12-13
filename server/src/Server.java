import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Server {

    public static final Pattern PATTERN_LOGIN = Pattern.compile("^Login \\[(.*)]$");
    public static final Pattern PATTERN_LOGOUT = Pattern.compile("^Logout$");
    public static final Pattern PATTERN_UTEXT = Pattern.compile("^UText \\[(.*)] (.*)$");
    public static final Pattern PATTERN_UFILE = Pattern.compile("^UFile (.*)$");
    public static final Pattern PATTERN_UGET = Pattern.compile("^UGet \\[(.*)] \\[(.*)]$");
    public static final Pattern PATTERN_CREATE = Pattern.compile("^Create \\[(.*)]$");
    public static final Pattern PATTERN_JOIN = Pattern.compile("^Join \\[(.*)]$");
    public static final Pattern PATTERN_LEAVE = Pattern.compile("^Leave \\[(.*)]$");
    public static final Pattern PATTERN_GTEXT = Pattern.compile("^GText \\[(.*)] (.*)$");
    public static final Pattern PATTERN_GFILE = Pattern.compile("^GFile (.*)$");
    public static final Pattern PATTERN_GGET = Pattern.compile("^GGet \\[(.*)] \\[(.*)]$");
    public static final Pattern PATTERN_USERS = Pattern.compile("^Users( \\[(.*)])?$");
    public static final Pattern PATTERN_GROUPS = Pattern.compile("^Groups$");
    public static final Pattern PATTERN_UPUT = Pattern.compile("^UPut \\[(.*)] \\[(.*)] (\\d*)$");
    public static final Pattern PATTERN_GPUT = Pattern.compile("^GPut \\[(.*)] \\[(.*)] (\\d*)$");

    private static final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> fileLocks = new ConcurrentHashMap<>();

    public static Connection getConnection(String id) {
        return connections.get(id);
    }

    public static Group getGroup(String id) {
        return groups.get(id);
    }

    public static void removeConnection(String id) {
        Connection connection = connections.remove(id);
        if (connection != null) {
            for (Group group : groups.values()) {
                group.outGroup(connection);
            }
        }
    }

    public static Collection<Group> getGroups() {
        return groups.values();
    }

    public static Collection<Connection> getConnections() {
        return connections.values();
    }

    public static synchronized boolean addConnection(Connection connection) {
        if (connections.containsKey(connection.getId())) {
            return false;
        }
        connections.put(connection.getId(), connection);
        return true;
    }

    public static synchronized boolean addGroup(Group group) {
        if (groups.containsKey(group.getName())) {
            return false;
        }
        groups.put(group.getName(), group);
        return true;
    }

    public static synchronized Object getFileLock(String name) {
        fileLocks.putIfAbsent(name, new Object());
        return fileLocks.get(name);
    }

}

