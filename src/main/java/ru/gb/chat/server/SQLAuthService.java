package ru.gb.chat.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLAuthService implements AuthService {

    private Connection connection;
    private final List<UserData> users;
    private static class UserData {
        private final String login;
        private final String password;
        private final String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

    public SQLAuthService() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(new UserData("login" + i, "pass" + i, "nick" + i));
        }
    }

    @Override
    public String register(final String login, final String password){
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement nicksStatement = connection.prepareStatement("INSERT INTO nicks (nick) VALUES (?) ");
                PreparedStatement usersStatement = connection.prepareStatement("INSERT INTO users (login, password, nick) " +
                        "VALUES (?, ?, (SELECT id FROM nicks WHERE nicks.nick = ?))")) {
            nicksStatement.setString(1,login);
            nicksStatement.executeUpdate();
            usersStatement.setString(1,login);
            usersStatement.setString(2,password);
            usersStatement.setString(3,login);
            usersStatement.executeUpdate();
            connection.commit();
            return getNickByLoginAndPassword(login, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        return selectNickByLogin(connection, login, password);
    }

    public String selectNickByLogin(final Connection connection, String login, String password) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT n.nick FROM nicks n INNER JOIN users u on u.nick = n.id WHERE u.login = ? AND u.password = ?")) {
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            return rs.getString("nick");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void changeNick(String oldNick, String newNick) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE nicks SET nick = ? WHERE nick = ?")) {
            statement.setString(1, newNick);
            statement.setString(2, oldNick);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
