package db;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Tables {
    public static void createTables() {
        DbConnection dbConnection = new DbConnection();

        try (Connection connection = dbConnection.getConnection()) {
            assert connection != null;
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS indexes");
                statement.execute("DROP TABLE IF EXISTS lemmas");
                statement.execute("DROP TABLE IF EXISTS fields");
                statement.execute("DROP TABLE IF EXISTS pages");

                createSiteTable(statement);
                createPagesTable(statement);
                createFieldsTable(statement, connection);
                createLemmaTable(statement);
                createIndexTable(statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createPagesTable(@NotNull Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS pages("
                .concat("id SERIAL PRIMARY KEY, ")
                .concat("code INT NOT NULL, ")
                .concat("content TEXT NOT NULL, ")
                .concat("path VARCHAR(255) NOT NULL, ")
                .concat("created_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("updated_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("site_id int NOT NULL, ")
                .concat("CONSTRAINT fk_site FOREIGN KEY(site_id) REFERENCES sites(id))")
        );

        statement.execute("CREATE UNIQUE INDEX pages_path_uindex ON pages (path)");
    }

    private static void createFieldsTable(@NotNull Statement statement, @NotNull Connection connection)
            throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS fields("
                .concat("id SERIAL PRIMARY KEY, ")
                .concat("name VARCHAR(255) NOT NULL UNIQUE, ")
                .concat("selector VARCHAR(255) NOT NULL, ")
                .concat("weight FLOAT NOT NULL, ")
                .concat("created_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("updated_at TIMESTAMP NOT NULL DEFAULT NOW())")
        );

        String sql = "INSERT INTO fields (name, selector, weight) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "title");
            preparedStatement.setString(2, "title");
            preparedStatement.setFloat(3, 1);
            preparedStatement.addBatch();

            preparedStatement.setString(1, "body");
            preparedStatement.setString(2, "body");
            preparedStatement.setBigDecimal(3, new BigDecimal("0.8"));
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
        }
    }

    private static void createLemmaTable(@NotNull Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS lemmas("
                .concat("id SERIAL PRIMARY KEY, ")
                .concat("lemma VARCHAR(255) NOT NULL, ")
                .concat("frequency int NOT NULL, ")
                .concat("created_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("updated_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("site_id int NOT NULL, ")
                .concat("CONSTRAINT fk_site FOREIGN KEY(site_id) REFERENCES sites(id))")
        );

        statement.execute("CREATE UNIQUE INDEX lemmas_lemma_uindex ON lemmas (lemma)");
    }

    private static void createIndexTable(@NotNull Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS indexes("
                .concat("id SERIAL PRIMARY KEY, ")
                .concat("page_id int NOT NULL, ")
                .concat("lemma_id int NOT NULL, ")
                .concat("rank FLOAT NOT NULL, ")
                .concat("created_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("updated_at TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("CONSTRAINT indexes_pages_id_fk FOREIGN KEY (page_id) REFERENCES pages, ")
                .concat("CONSTRAINT indexes_lemmas_id_fk FOREIGN KEY (lemma_id) REFERENCES lemmas)")
        );
    }

    private static void createSiteTable(@NotNull Statement statement) throws SQLException {
        createPgStatusEnum(statement);

        statement.execute("CREATE TABLE IF NOT EXISTS sites("
                .concat("id SERIAL PRIMARY KEY, ")
                .concat("status index_status_enum NOT NULL, ")
                .concat("status_time TIMESTAMP NOT NULL DEFAULT NOW(), ")
                .concat("last_error TEXT, ")
                .concat("url VARCHAR(255) NOT NULL, ")
                .concat("name VARCHAR(255) NOT NULL)"));
    }

    private static void createPgStatusEnum(@NotNull Statement statement) throws SQLException {
        statement.execute("CREATE TYPE index_status_enum AS ENUM ('INDEXING', 'INDEXED', 'FAILED')");
    }
}
