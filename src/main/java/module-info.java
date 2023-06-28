module com.tcp.book {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql.rowset;


    opens com.tcp.book to javafx.fxml;
    exports com.tcp.book;
}