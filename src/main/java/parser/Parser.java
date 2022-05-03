package parser;

import db.DbConnection;
import entities.Page;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class Parser {
    private String domain = "http://www.playback.ru";
    private int batchSize = 0;

    public volatile static Map<String, Page> pageMap = new ConcurrentHashMap<>();
    static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private final DbConnection dbConnection = new DbConnection();

    public void parseSite() {
        try {
            throw new SQLException();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        new ForkJoinPool().invoke(new RecursiveParser(domain, domain.concat("/")));
        savePages();
    }

    private void savePages() {
        String sql = "INSERT INTO pages (code, content, path) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection()) {
            assert connection != null;
            dropTableIfExists(connection);
            createTable(connection);

            PreparedStatement statement = connection.prepareStatement(sql);
            addBatch(statement);

            statement.executeBatch();
            statement.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private void addBatch(PreparedStatement statement) throws SQLException {
        for (Page page : pageMap.values()) {
            statement.setInt(1, page.getCode());
            statement.setString(2, page.getContent());
            statement.setString(3, page.getPath());

            statement.addBatch();
            batchSize++;

            if (batchSize >= 1_000) {
                statement.executeBatch();
                batchSize = 0;
            }
        }
    }

    private static void dropTableIfExists(@NotNull Connection connection) throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS pages");
    }

    private static void createTable(@NotNull Connection connection) throws SQLException {
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS pages("
                .concat("id SERIAL PRIMARY KEY, ")
                .concat("code INT NOT NULL, ")
                .concat("content TEXT NOT NULL, ")
                .concat("path VARCHAR(255) NOT NULL, ")
                .concat("created_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("updated_at TIMESTAMP NOT NULL DEFAULT NOW())")
        );

        connection.createStatement().execute("CREATE UNIQUE INDEX pages_path_uindex ON pages (path)");
    }
}
