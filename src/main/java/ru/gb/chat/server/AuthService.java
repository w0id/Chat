package ru.gb.chat.server;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public interface AuthService extends Closeable {

//    String register(Connection connection, String login, String password) throws SQLException;

    String getNickByLoginAndPassword(String login, String password);

    void changeNick(String oldNick, String newNick);

    String register(String login, String password);

    void close();

}
