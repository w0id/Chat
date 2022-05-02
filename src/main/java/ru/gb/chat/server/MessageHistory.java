package ru.gb.chat.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageHistory {

    public void write(String nick, String message) {
        try (final FileOutputStream out = new FileOutputStream(nick + ".txt", true)) {
            byte[] bytes = (message + "\n").getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
