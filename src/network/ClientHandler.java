package network;

import database.MessageRepository;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
            Integer CorrectUsernameFlag = 0;
            while (CorrectUsernameFlag == 0){
                username = reader.readLine();
                if (username.startsWith("/")){
                    writer.println("YOu are an idiot | ERROR: Username cannot start with command sign");
                }
                else
                    {
                    CorrectUsernameFlag = 1;
                }
            }
            clients.add(this);
            onlineUsers.put(username, this);
            broadcast("SERVER: " + username + " joined the chat", this);
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("/exit")) {
                    writer.println("Disconnecting...");
                    break;
                }
                if (message.equalsIgnoreCase("/users")) {
                    writer.println(getOnlineUsers());
                    continue;
                }
                if (message.startsWith("/msg")) {
                    if (message.equals("/msg")) {
                        writer.println("ERROR: no arguments given, type ? for help");
                        continue;
                    }
                    String[] parts = message.split(" ", 3);
                    if (parts[1].equals("?") ) {
                        writer.println("Usage: /msg username message");
                        continue;
                    }
                    if (parts.length < 3) {
                        writer.println("ERROR: no target or message, type ? for help");
                        continue;
                    }
                    String targetUser = parts[1];
                    String privateMessage = parts[2];
                    sendPrivateMessage(targetUser, privateMessage);
                    continue;
                }

                String formatted = username + ": " + message;
                MessageRepository.saveMessage(username, message);
                broadcast(formatted, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
    } finally {
        disconnect();
    }
}
    private void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.writer.println(message);
            }
        }
    }
    private void disconnect() {
        clients.remove(this);
        onlineUsers.remove(username);
        broadcast("SERVER: " + username + " left the chat", this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getOnlineUsers() {
        StringBuilder sb = new StringBuilder("Online users:\n");
        for (ClientHandler client : clients) {
            sb.append("- ").append(client.username).append("\n");
        }
        return sb.toString();
    }
    private static Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    private void sendPrivateMessage(String targetUser, String message) {
        ClientHandler target = onlineUsers.get(targetUser);
        if (target == null) {
            writer.println("User " + targetUser + " not found.");
            return;
        }
        target.writer.println("[PRIVATE] " + username + ": " + message);
        writer.println("[PRIVATE to " + targetUser + "] " + message);
    }
}