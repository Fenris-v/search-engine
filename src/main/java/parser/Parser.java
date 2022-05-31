package parser;

import db.DbConnection;
import entities.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.YamlReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class Parser implements Runnable {
    private final String referrer;
    private final String userAgent;

    private final String domain;
    private int batchSize = 0;

    public volatile static Map<String, Page> pageMap = new ConcurrentHashMap<>();
    static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private final DbConnection dbConnection = new DbConnection();

    public Parser(String domain) {
        this.domain = domain;

        Map<String, String> data = YamlReader.getConfigs("parser");
        referrer = data.get("referrer");
        userAgent = data.get("user_agent");
    }

    @Override
    public void run() {
        parseSite();
    }

    public void parseSite() {
        try {
            throw new SQLException();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        new ForkJoinPool().invoke(new RecursiveParser(domain, domain.concat("/"), referrer, userAgent));
        savePages();
    }

    private void savePages() {
        // todo: add site_id
        String sql = "INSERT INTO pages (code, content, path, site_id) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection()) {
            assert connection != null;

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
}
