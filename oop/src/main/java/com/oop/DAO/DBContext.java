package com.oop.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.oop.Config.DBLoader;

public class DBContext {
    public static Connection getConnection() throws SQLException {
        String host = DBLoader.get("db.host");
        String port = DBLoader.get("db.port");
        String dbName = DBLoader.get("db.name");
        String user = DBLoader.get("db.user");
        String pass = DBLoader.get("db.password");

        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?sslMode=REQUIRED", host, port, dbName);

        return DriverManager.getConnection(jdbcUrl, user, pass);
    }
}
