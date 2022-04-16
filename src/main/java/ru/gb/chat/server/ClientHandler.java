package ru.gb.chat.server;

import ru.gb.chat.Command;

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
        sendMessage(Command.END);
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
                if (Command.isCommand(msg)) {
                    final Command command = Command.getCommand(msg);
                    if (command == Command.END) {
                        server.broadcast("Пользователь " + nick + " покинул чат");
                        break;
                    }
                    if (command == Command.ERROR) {
                        sendMessage(Command.ERROR, "Неизвестная команда");
                    }
                    if (command == Command.PRIVATE_MESSAGE) {
                        final String[] params = command.parse(msg);
                        server.sendMessageToClient(this, params[0], params[1]);
                    }
                } else {
                    System.out.println("Получено сообщение: " + msg);
                    server.broadcast(nick + ": " + msg);
                }
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
                if (Command.isCommand(msg) && Command.getCommand(msg) == Command.END) {
                    isDisconnecting = true;
                    break;
                }
                if (Command.isCommand(msg)) {
                    final Command command = Command.getCommand(msg);
                    final String[] params = command.parse(msg);
                    if (command == Command.AUTH) {
                        final String login = params[0];
                        final String password = params[1];
                        final String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR, "Пользователь уже авторизован");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick);
                            this.nick = nick;
                            server.broadcast("Пользователь " + nick + " вошел в чат");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage(Command.ERROR,"Неверный логин или пароль");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isDisconnecting = true;
                break;
            }
        }
    }

    public void sendMessage(final Command command, final String... params) {
        sendMessage(command.collectMessage(params));
    }

    public String getNick() {
        return nick;
    }
}
