package network;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import database.DatabaseManager;
public class ChatServer {
    private static final int PORT = 12345;
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                pool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
    }
}
}
