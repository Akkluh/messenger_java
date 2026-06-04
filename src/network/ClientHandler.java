package network;
import database.MessageRepository;
import database.UserInfo;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private String password;
    private static DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
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
        writer.println("Welcome to Chat!");
        writer.println("Sign in (s) or Log in (l) ?:");
        String SLoption = reader.readLine();
        
        // Выбор username
        writer.println("Enter your username:");
        while (true) {
            username = reader.readLine();
            
            if (username == null || username.isBlank()) {
                writer.println("ERROR: Username cannot be empty.");
                continue;
            }
            
            if (username.startsWith("/")) {
                writer.println("ERROR: Username cannot start with command sign");
                continue;
            }
            
            if (SLoption.equals("s")) {
                if (UserInfo.userExists(username)) {
                    writer.println("ERROR: Username already registered. Please, try another.");
                    continue;
                }
                if (onlineUsers.containsKey(username)) {
                    writer.println("ERROR: Username already taken online. Please, try another.");
                    continue;
                }
            }
            if (SLoption.equals("l")){
                if (onlineUsers.containsKey(username)) {
                    writer.println("ERROR: Username already taken online. Please, try another.");
                    continue;
                }
            }
            break;
        }
        
        // Выбор пароля и регистрация/логин
        while (true) {
            writer.println("Enter your password:");
            password = reader.readLine();
            
            if (SLoption.equals("s")) {
                UserInfo.signIn(username, password);
                writer.println("Registration successful! Welcome to the chat!");
                break;
            } else if (SLoption.equals("l")) {
                if (UserInfo.logIn(username, password)) {
                    writer.println("Login successful! Welcome back!");
                    break;
                } else {
                    writer.println("ERROR: Invalid username or password. Try again.");
                    continue;
                }
            } else {
                writer.println("ERROR: Invalid option. Disconnecting...");
                return;
            }
        }
            clients.add(this);
            onlineUsers.put(username, this);
            broadcast("[" + getCurrentTime() + "] " + "SERVER: " + username + " joined the chat", this);
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("/help")) {
                    writer.println("Commands:\n/exit - Disconnects the user from the server\n/users - Prints out list of users currently connected to the server\n/msg 'username' 'message' - Sends a direct message to a user that can only be viewed by the sender and the reciever.");
                    continue;
                }
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
                        writer.println("ERROR: No arguments given");
                        continue;
                    }
                    String[] parts = message.split(" ", 3);
                    if (parts.length < 3 && parts.length != 1) {
                        writer.println("ERROR: No target or message");
                        continue;
                    }
                    String targetUser = parts[1];
                    String privateMessage = parts[2];
                    sendPrivateMessage(targetUser, privateMessage);
                    continue;
                }

                String formatted = "[" + getCurrentTime() + "] " + username + ": " + message;
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
        broadcast("[" + getCurrentTime() + "] " + "SERVER: " + username + " left the chat", this);
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
        target.writer.println("[" + getCurrentTime() + "] " + "[PRIVATE] " + username + ": " + message);
        writer.println("[" + getCurrentTime() + "] " + "[PRIVATE to " + targetUser + "] " + message);
    }
    private String getCurrentTime() {
        return LocalDateTime.now().format(TimeFormat);
    }
}