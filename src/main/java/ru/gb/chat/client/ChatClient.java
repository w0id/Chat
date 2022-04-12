package ru.gb.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private DataInputStream in;
    private DataOutputStream out;
    private final ClientController controller;
    private boolean isDisconnecting;
    private Socket socket;

    public ChatClient(final ClientController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 9000);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                if (!isDisconnecting) {
                    readMessage();
                }
            } finally {
                controller.messageArea.clear();
                try {
                    openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
    }

    private void readMessage() {
        while (true) {
            try {
                final String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    controller.toggleBoxesVisibility(false);
                    closeConnection();
                    break;
                }
                controller.addMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitAuth() {
        while (true) {
            try {
                final String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    isDisconnecting = true;
                    break;
                }
                if (msg.startsWith("/authError")) {
                    controller.setErrorText("Неверный логин или пароль");
                    controller.loginField.clear();
                    controller.passwordField.clear();
                }
                if (msg.startsWith("/alreadyLoggedIn")) {
                    controller.setErrorText("Пользователь уже авторизован");
                    controller.loginField.clear();
                    controller.passwordField.clear();
                }
                if (msg.startsWith("/authok")) {
                    final String[] split = msg.split("\\s+");
                    final String nick = split[1];
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    controller.toggleBoxesVisibility(true);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isDisconnecting = true;
                break;
            }
        }
    }

    public void sendMessage(final String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
