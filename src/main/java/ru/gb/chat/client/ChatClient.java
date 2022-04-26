package ru.gb.chat.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import ru.gb.chat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class ChatClient {

    private DataInputStream in;
    private DataOutputStream out;
    private final ClientController controller;
    private boolean isDisconnecting;
    private boolean logout;
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
                if (logout) {
                    controller.messageArea.clear();
                    logout = false;
                    try {
                        openConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                if (Command.isCommand(msg)) {
                    final Command command = Command.getCommand(msg);
                    final String[] params = command.parse(msg);
                    if (command == Command.END) {
                        logout = true;
                        controller.toggleBoxesVisibility(false);
                        closeConnection();
                        break;
                    }
                    if (command == Command.ERROR) {
                        controller.setErrorText(params);
                    }
                    if (command == Command.REGISTEROK) {
                        controller.setNotificationText(params);
                    }
                    if (command == Command.CLIENTS) {
                        controller.updateClients(params);
                        continue;
                    }
                } else {
                    controller.addMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitAuth() {
        while (true) {
            try {
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(120000);
                        closeConnection();
                        Platform.runLater(() -> {
                            final Alert alert = new Alert(Alert.AlertType.ERROR,"Время ожидания истекло",
                                    new ButtonType("Повторить попытку", ButtonBar.ButtonData.OK_DONE),
                                    new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
                            alert.setTitle("Ошибка подключения");
                            final Optional<ButtonType> buttonType = alert.showAndWait();
                            final Boolean isExit = buttonType.map(btn -> btn.getButtonData().isCancelButton()).orElse(false);
                            final Boolean isRetry = buttonType.map(btn -> btn.getButtonData().isDefaultButton()).orElse(false);
                            if (isExit) {
                                System.exit(0);
                            } else if (isRetry) {
                                try {
                                    isDisconnecting = false;
                                    openConnection();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                final String msg = in.readUTF();
                if (Command.isCommand(msg)) {
                    final Command command = Command.getCommand(msg);
                    final String[] params = command.parse(msg);
                    if (command == Command.ERROR) {
                        controller.setErrorText(params);
                        controller.loginField.clear();
                        controller.passwordField.clear();
                    }
                    if (command == Command.REGISTEROK) {
                        thread.interrupt();
                        controller.setNotificationText(params);
                    }
                    if (command == Command.AUTHOK) {
                        final String[] split = msg.split("\\s+");
                        final String nick = split[1];
                        logout = false;
                        thread.interrupt();
                        System.out.println(thread.isInterrupted());
                        controller.addMessage("Успешная авторизация под ником " + nick);
                        controller.toggleBoxesVisibility(true);
                        break;
                    }
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

    public void sendMessage(final Command command, final String... params) {
        sendMessage(command.collectMessage(params));
    }
}
