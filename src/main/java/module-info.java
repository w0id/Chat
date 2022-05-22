module ru.gb.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.logging.log4j;

    exports ru.gb.chat.client;
    opens ru.gb.chat.client to javafx.fxml;
}