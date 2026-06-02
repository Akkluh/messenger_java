package network;

import database.MessageRepository;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private static Set<ClientHandler> clients = new HashSet<>();
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            writer.println("Enter your username:");
            username = reader.readLine();
            clients.add(this);
            broadcast("SERVER: " + username + " joined the chat");
            String message;
            while ((message = reader.readLine()) != null) {
                String formatted = username + ": " + message;
                MessageRepository.saveMessage(username, message);
                broadcast(formatted);
            }
        } catch (IOException e) {
            e.printStackTrace();
    } finally {
        disconnect();
    }
}
    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.writer.println(message);
        }
    }
    private void disconnect() {
        clients.remove(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}