package ru.gb.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {

    AUTH("/auth") {
        @Override
        public String[] parse(final String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },
    REGISTER("/register") {
        @Override
        public String[] parse(final String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER);
            return new String[]{split[1], split[2]};
        }
    },
    AUTHOK("/authok") {
        @Override
        public String[] parse(final String commandText) {
            return new String[]{commandText.split(COMMAND_DELIMITER)[1]};
        }
    },
    PRIVATE_MESSAGE("/w") {
        @Override
        public String[] parse(final String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER, 3);
            return new String[]{split[1], split[2]};
        }
    },
    NICK("/nick") {
        @Override
        public String[] parse(final String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER);
            return new String[]{split[1]};
        }
    },
    END("/end") {
        @Override
        public String[] parse(final String commandText) {
            return new String[0];
        }
    },
    ERROR("/error") {
        @Override
        public String[] parse(final String commandText) {
            final String errorMsg = commandText.split(COMMAND_DELIMITER, 2)[1];
            return new String[]{errorMsg};
        }
    },
    REGISTEROK("/notification") {
        @Override
        public String[] parse(final String commandText) {
            final String notificationMsg = commandText.split(COMMAND_DELIMITER, 2)[1];
            return new String[]{notificationMsg};
        }
    },
    CLIENTS("/clients") {
        @Override
        public String[] parse(final String commandText) {
            final String[] split = commandText.split(COMMAND_DELIMITER);
            return Arrays.stream(split).skip(1).toArray(String[]::new);
        }
    };

    private static final Map<String, Command> map = Stream.of(Command.values())
            .collect(Collectors.toMap(Command::getCommand, Function.identity()));

    private final String command;

    static final String COMMAND_DELIMITER = "\\s+";

    Command(String command) {
        this.command = command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommand(String message) {
        message = message.trim();
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "' is not a command");
        }
        final int index = message.indexOf(" ");
        final String cmd = index > 0 ? message.substring(0, index) : message;

        final Command command = map.get(cmd);
        if (command == null) {
            return ERROR;
        }
        return command;
    }

    public abstract String[] parse(String commandText);

    public String collectMessage(String... params) {
        final String command = this.getCommand();
        return command + (params==null ? "" : " " + String.join(" ", params));
    }
}
