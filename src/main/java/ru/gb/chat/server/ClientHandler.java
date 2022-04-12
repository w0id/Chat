package ru.gb.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final AuthService authService;
    private boolean isDisconnecting;

    public ClientHandler(Socket socket, ChatServer chatServer, AuthService authService) {
        try {
            this.socket = socket;
            this.server = chatServer;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            new Thread(() -> {
                try {
                    authenticate();
                    if (!isDisconnecting) {
                        readMessage();
                    }
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания подключения к клиенту", e);
        }
    }

    private void closeConnection() {
        sendMessage("/end");
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Отправляю сообщение: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        while (true) {
            try {
                final String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    server.broadcast("Пользователь " + nick + " покинул чат");
                    break;
                }
                System.out.println("Получено сообщение: " + msg);
                server.broadcast(nick+": "+msg);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    isDisconnecting = true;
                    break;
                }
                if (msg.startsWith("/auth")) {
                    final String[] s = msg.split("\\s+");
                    final String login = s[1];
                    final String password = s[2];
                    final String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("/alreadyLoggedIn");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " вошел в чат");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage("/authError");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isDisconnecting = true;
                break;
            }
        }
    }

    public String getNick() {
        return nick;
    }
}
