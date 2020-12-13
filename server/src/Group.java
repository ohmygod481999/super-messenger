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
        if (connections.remove(connection.getId()) != null) {
            addWriter(new Writer.Text("132 [" + name + "] [" + connection.getId() + "] has left"));
        }
    }

    public void addWriter(Writer writer) {
        for (Connection connection : connections.values()) {
            connection.addWriter(writer);
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
