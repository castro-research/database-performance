package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class Database {
    private Connection connection = null;

    public static String Url() {
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");

        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }

    public Connection connection() {
        if (this.connection != null) return connection;
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(Database.Url(), dbUser, dbPassword);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return this.connection;
    }
}
