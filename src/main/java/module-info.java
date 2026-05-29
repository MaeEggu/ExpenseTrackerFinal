module expense.tracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires org.postgresql.jdbc;

    exports application;
    exports model;
    exports service;
    exports utility;
    exports database;
    exports dao;
    exports repository;

    opens controller to javafx.fxml;
    opens model to javafx.base;
}
