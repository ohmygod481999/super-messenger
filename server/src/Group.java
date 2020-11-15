import java.io.IOException;
import java.util.ArrayList;

public class Group {
    private String name;
    private ArrayList<Connection> connections;

    public Group(String name) {
        this.name = name;
        this.connections = new ArrayList<Connection>();
    }

    public void joinGroup (Connection connection) {
        this.connections.add(connection);
    }

    public void outGroup (Connection connection) {
        this.connections.remove(connection);
    }

    public void sendMsg (Connection from, String msg) throws IOException {
        for (Connection connection : this.connections) {
            String out = from.getId() + " " + msg;
            connection.write(out.getBytes());
        }
    }

    public ArrayList<Connection> getConnections () {
        return this.connections;
    }

    public String getName() {
        return name;
    }
}
