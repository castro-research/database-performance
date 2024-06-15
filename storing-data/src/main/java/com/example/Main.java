package com.example;

import java.sql.*;

class PerformThread {
    public void run(PerformTable table) {
        try (Connection connection = new Database().connection();
                Statement statement = connection.createStatement()) {

            String dropTableSQL = "DROP TABLE IF EXISTS " + table.getTableName();
            statement.execute(dropTableSQL);

            // Create table
            statement.execute(table.getCreateTableSql());

            // Insert data
            statement.execute(table.getInsertSql());

            // Query table size
            String getSizeTableSQL = "SELECT pg_size_pretty(pg_relation_size(?))";
            try (PreparedStatement sizeStatement = connection.prepareStatement(getSizeTableSQL)) {
                sizeStatement.setString(1, table.getTableName());
                try (ResultSet sizeQuery = sizeStatement.executeQuery()) {
                    if (sizeQuery.next()) {
                        System.out.println("Table name " + table.getTableName() + " size: " + sizeQuery.getString(1));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

abstract class PerformTable implements Runnable {
    protected String name;

    abstract String getCreateTableSql();

    abstract String getInsertSql();

    public void run() {
        new PerformThread().run(this);
    }

    public String getTableName() {
        return name;
    }
}

class OrderedColumns extends PerformTable {
    OrderedColumns() {
        this.name = "test_b";
    }

    @Override
    public String getCreateTableSql() {
        return """
                CREATE TABLE\s""" + this.name + """
                       (
                        t1 INT,
                        t2 INT,
                        t3 INT,
                        t4 VARCHAR(100),
                        t5 VARCHAR(100),
                        t6 VARCHAR(100)
                        )
                """;
    }

    @Override
    public String getInsertSql() {
        return "INSERT INTO " + this.name
                + " SELECT 10, 20, 30, 'abcd', 'abcd', 'abcd' FROM generate_series(1, 10000000)";
    }
}

class UnorderedColumns extends PerformTable {
    UnorderedColumns() {
        this.name = "test_a";
    }

    @Override
    public String getCreateTableSql() {
        return """
                CREATE TABLE\s""" + this.name + """
                       (
                        t1 VARCHAR(100),
                        t2 INT,
                        t3 VARCHAR(100),
                        t4 INT,
                        t5 VARCHAR(100),
                        t6 INT
                        )
                """;
    }

    @Override
    public String getInsertSql() {
        return "INSERT INTO " + this.name
                + " SELECT 'abcd', 10, 'abcd', 20, 'abcd', 30 FROM generate_series(1, 10000000)";
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
