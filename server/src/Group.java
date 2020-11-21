import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Group {
    private String name;
    private ConcurrentHashMap<String, Connection> connections;

    public Group(String name) {
        this.name = name;
        this.connections = new ConcurrentHashMap<>();
    }

    public void joinGroup(Connection connection) {
        this.connections.put(connection.getId(), connection);
    }

    public void outGroup(Connection connection) {
        this.connections.remove(connection.getId());
    }

    public void sendMsg(String msg) throws IOException {
        for (Connection connection : this.connections.values()) {
            connection.write(msg);
        }
    }

    public Connection getConnection(String id) {
        return connections.get(id);
    }

    public Collection<Connection> getConnections() {
        return this.connections.values();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
