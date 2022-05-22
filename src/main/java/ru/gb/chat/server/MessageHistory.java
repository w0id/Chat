package ru.gb.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageHistory {
    private static final Logger log = LogManager.getLogger(ChatServer.class);

    public void write(String nick, String message) {
        try (final FileOutputStream out = new FileOutputStream(nick + ".txt", true)) {
            byte[] bytes = (message + "\n").getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
        } catch (IOException e) {
            log.error("error occured: {}", e);
            throw new RuntimeException(e);
        }
    }
}
