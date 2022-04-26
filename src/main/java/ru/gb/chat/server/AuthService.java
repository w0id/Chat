package ru.gb.chat.server;

import java.io.Closeable;

public interface AuthService extends Closeable {

    String getNickByLoginAndPassword(String login, String password);

    void changeNick(String oldNick, String newNick);

    String register(String login, String password);

    void close();

}
