import java.io.*;
import java.net.Socket;
import java.util.Scanner;
public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
                MessageListener listener = new MessageListener(reader);
                new Thread(listener).start();
                while (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    writer.println(input);
                    if (input.equalsIgnoreCase("/exit")) {
                        break;
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
    }
    
    }
}
