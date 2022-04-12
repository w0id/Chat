package ru.gb.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;
    private final List<ClientHandler> clients;

    public ChatServer() {
        authService = new InMemoryAuthService();
        clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9000)) {
            authService.start();
            while (true) {
                System.out.println("Ожидаем подключения клиента...");
                final Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this, authService);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сервера", e);
        }
    }


    public boolean isNickBusy(final String nick) {
        for (final ClientHandler client : clients) {
            if (client.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcast(final String message) {
        clients.forEach(client -> client.sendMessage(message));
    }

    public void subscribe(final ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(final ClientHandler client) {
        clients.remove(client);
    }
}
