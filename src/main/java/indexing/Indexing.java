package indexing;

import db.DbConnection;
import entities.Field;
import entities.Page;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Indexing {
    private final Connection connection;
    private final Set<Field> fields = new HashSet<>();
    private final Set<Page> pages = new HashSet<>();

    public Indexing() {
        connection = new DbConnection().getConnection();

        assert connection != null;
        try (Statement statement = connection.createStatement()) {
            setFields(statement);
            setPages(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Connection getConnection() {
        return connection;
    }

    Set<Field> getFields() {
        return fields;
    }

    Set<Page> getPages() {
        return pages;
    }

    public void execute() {
        new LemmasCounter(this).execute();
        new IndexesCounter(this).execute();

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFields(@NotNull Statement statement) throws SQLException {
        ResultSet result = statement.executeQuery("SELECT * FROM fields");
        while (result.next()) {
            fields.add(new Field(
                    result.getString("name"),
                    result.getString("selector"),
                    result.getFloat("weight")
            ));
        }
    }

    private void setPages(@NotNull Statement statement) throws SQLException {
        ResultSet result = statement.executeQuery("SELECT * FROM pages WHERE path = '/' LIMIT 1");
        while (result.next()) {
            pages.add(new Page(
                            result.getInt("id"),
                            result.getString("path"),
                            result.getInt("code"),
                            result.getString("content")
                    )
            );
        }
    }
}