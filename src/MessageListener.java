import java.io.IOException;
import java.io.BufferedReader;
public class MessageListener implements Runnable {

    private BufferedReader reader;

    public MessageListener(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {

        String message;

        try {
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}