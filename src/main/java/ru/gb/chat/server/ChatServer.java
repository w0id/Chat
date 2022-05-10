package ru.gb.chat.server;

import ru.gb.chat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final Map<String, ClientHandler> clients;
    private final MessageHistory messageHistory = new MessageHistory();
    private volatile char currentLetter = 'A';
    public final ExecutorService executorService = Executors.newFixedThreadPool(15);

    public ChatServer() { this.clients = new HashMap<>(); }

    public void run() {
        executorService.execute(() -> print('A', 'B'));
        executorService.execute(() -> print('B', 'C'));
        executorService.execute(() -> print('C', 'A'));

        try (ServerSocket serverSocket = new ServerSocket(9000);
        AuthService authService = new SQLAuthService()) {
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

    private synchronized void print(char prevLetter, char nextLetter) {
        try {
            for (int i = 0; i < 5; i++) {
                while (currentLetter != prevLetter) {
                    this.wait();
                }
                if (prevLetter == 'C') System.out.println(prevLetter);
                else System.out.print(prevLetter);
                currentLetter = nextLetter;
                this.notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public boolean isNickBusy(String nick) { return clients.containsKey(nick); }

    private void broadcastClients() {
        StringBuilder nicks = new StringBuilder();
        clients.values().forEach(value -> nicks.append(value.getNick()).append(" "));
        broarcast(Command.CLIENTS, nicks.toString().trim());
    }

    private void broarcast(Command command, String nicks) {
        clients.values().forEach(client -> client.sendMessage(command, nicks));
    }

    public void broadcast(final String message) { clients.values().forEach(client -> {
        client.sendMessage(message);
        messageHistory.write(client.getNick(), message);
    }); }

    public void subscribe(final ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClients();
    }

    public void unsubscribe(final ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClients();
    }

    public void sendMessageToClient(final ClientHandler from, final String to, final String message) {
        final ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("от " + from.getNick() + ": " + message);
            messageHistory.write(to, "от " + from.getNick() + ": " + message);
            from.sendMessage("Пользователю " + to + ": " + message);
            messageHistory.write(from.getNick(), "Пользователю " + to + ": " + message);
        } else {
            from.sendMessage(Command.ERROR, "Пользователя с ником " + to + " нет в чате!");
        }
    }
}
