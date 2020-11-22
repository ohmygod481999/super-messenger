import java.util.concurrent.ConcurrentLinkedQueue;

public class WriteThread extends Thread {

    private Connection connection;
    private ConcurrentLinkedQueue<Writer> writers;

    public WriteThread(Connection connection) {
        this.connection = connection;
        writers = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        super.run();
        try {
            while (!interrupted()) {
                synchronized (this) {
                    if (writers.isEmpty()) {
                        wait();
                    }
                }
                while (!writers.isEmpty()) {
                    writers.poll().write(connection.getDataOutputStream());
                }
            }
        } catch (Exception e) {
            try {
                connection.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        System.out.println("Connection id: " + connection.getId() + " - write thread stopped");

    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    public void addWriter(Writer writer) {
        synchronized (this) {
            writers.add(writer);
        }
    }
}

