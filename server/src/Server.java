import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();

    public static Connection getConnection(String id) {
        return connections.get(id);
    }

    public static Group getGroup(String id) {
        return groups.get(id);
    }

    public static void removeConnection(String id) {
        connections.remove(id);
    }

    public static void removeGroup(String id) {
        groups.remove(id);
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
}

