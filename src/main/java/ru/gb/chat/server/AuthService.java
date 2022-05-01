package ru.gb.chat.server;

import java.io.Closeable;
import java.sql.SQLException;

public interface AuthService extends Closeable {
    
    String getNickByLoginAndPassword(String login, String password);

    void changeNick(String oldNick, String newNick) throws SQLException;

    String register(String login, String password);

    void close();

}
