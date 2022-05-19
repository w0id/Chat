package ru.gb.chat.server;

public class ChatRunner {

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        try {
            chatServer.run();
        } finally {
            chatServer.getExecutorService().shutdown();
        }
    }
}
