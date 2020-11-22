import java.io.FileInputStream;
import java.io.OutputStream;

public abstract class Writer {

    public abstract void write(OutputStream output) throws Exception;

    public static class Text extends Writer {

        private String text;

        public Text(String text) {
            this.text = text;
        }

        @Override
        public void write(OutputStream output) throws Exception {
            output.write(text.getBytes());
            output.flush();
        }
    }

    public static class File extends Writer {

        private String code;
        private String name;

        public File(String code, String name) {
            this.name = name;
            this.code = code;
        }

        @Override
        public void write(OutputStream output) throws Exception {
            synchronized (Server.getFileLock(name)) {
                java.io.File file = new java.io.File(name);
                output.write((code + " " + file.length()).getBytes());
                FileInputStream stream = new FileInputStream(file);
                stream.transferTo(output);
                stream.close();
            }
        }

    }

}
