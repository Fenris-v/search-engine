package main.services.indexing;

import main.entities.Field;
import main.entities.Page;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Indexing {
    private final java.sql.Connection connection = null;
    private final Set<Field> fields = new HashSet<>();
    private final Set<Page> pages = new HashSet<>();

    private static final String getAllPagesSql = "SELECT * FROM pages";
    private static final String getAllFieldsSql = "SELECT * FROM fields";

    public Indexing() {
//        connection = Connection.getInstance().getConnection();

//        assert connection != null;
//        try (Statement statement = connection.createStatement()) {
//            setFields(statement);
//            setPages(statement);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    java.sql.Connection getConnection() {
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
        ResultSet result = statement.executeQuery(getAllFieldsSql);
        while (result.next()) {
//            fields.add(new Field(
//                    result.getString("name"),
//                    result.getString("selector"),
//                    result.getFloat("weight")
//            ));
        }
    }

    private void setPages(@NotNull Statement statement) throws SQLException {
        ResultSet result = statement.executeQuery(getAllPagesSql);
        while (result.next()) {
            // todo
//            pages.add(new Page(
//                            result.getInt("id"),
//                            result.getString("path"),
//                            result.getInt("code"),
//                            result.getString("content"),
//                            1
//                    )
//            );
        }
    }
}
