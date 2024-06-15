package com.example;

import java.sql.*;

class PerformThread {
    public void run(PerformTable table) {
        try (Connection connection = new Database().connection();
             Statement statement = connection.createStatement()) {

            String dropTableSQL = "DROP TABLE IF EXISTS " + table.name;
            statement.execute(dropTableSQL);

            // Create table
            statement.execute(table.createTable);

            // Insert data
            statement.execute(table.insertQuery);

            // Query table size
            String getSizeTableSQL = "SELECT pg_size_pretty(pg_relation_size(?))";
            try (PreparedStatement sizeStatement = connection.prepareStatement(getSizeTableSQL)) {
                sizeStatement.setString(1, table.name);
                try (ResultSet sizeQuery = sizeStatement.executeQuery()) {
                    if (sizeQuery.next()) {
                        System.out.println("Table name " + table.name + " size: " + sizeQuery.getString(1));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

abstract class PerformTable implements Runnable {
    String name;
    String createTable;
    String insertQuery;
    public void run() {}
}
class OrderedColumns extends PerformTable {
    OrderedColumns() {
        this.name = "test_b";
        this.createTable = """
                CREATE TABLE test_b (
                t1 INT,
                t2 INT,
                t3 INT,
                t4 VARCHAR(100),
                t5 VARCHAR(100),
                t6 VARCHAR(100)
                )
        """;
        this.insertQuery = "INSERT INTO " + this.name + " SELECT 10, 20, 30, 'abcd', 'abcd', 'abcd' FROM generate_series(1, 10000000)";
    }

    public void run() {
        new PerformThread().run(this);
    }
}
class UnorderedColumns extends PerformTable {
    UnorderedColumns() {
        this.name = "test_a";
        this.createTable = """
                CREATE TABLE test_a (
                t1 VARCHAR(100),
                t2 INT,
                t3 VARCHAR(100),
                t4 INT,
                t5 VARCHAR(100),
                t6 INT
                )
        """;
        this.insertQuery = "INSERT INTO " + this.name + " SELECT 'abcd', 10, 'abcd', 20, 'abcd', 30 FROM generate_series(1, 10000000)";
    }

    public void run() {
        new PerformThread().run(this);
    }
}

public class Main {
    public static void main(String[] args) {
        Thread t1 = new Thread(new OrderedColumns());
        Thread t2 = new Thread(new UnorderedColumns());
        t1.start();
        t2.start();
    }
}
