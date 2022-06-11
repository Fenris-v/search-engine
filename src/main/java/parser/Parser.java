package parser;

import entities.Page;
import enums.SiteStatus;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.YamlReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class Parser implements Runnable {
    @Getter
    private final String referrer;
    @Getter
    private final String userAgent;

    @Getter
    private final String domain;
    @Getter
    private final int siteId;
    private int batchSize = 0;

    @Getter
    private final Map<String, Page> pageMap;
    static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private Connection connection = null;

    public Parser(String domain, int id, Map<String, Page> pageMap) {
        try {
            connection = db.Connection.getInstance().getConnection();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        this.pageMap = pageMap;
        this.domain = domain;
        siteId = id;

        Map<String, String> data = YamlReader.getConfigs("parser");
        referrer = data.get("referrer");
        userAgent = data.get("user_agent");
    }

    @Override
    public void run() {
        new ForkJoinPool().invoke(new RecursiveParser(this, domain.concat("/")));

        if (pageMap.isEmpty()) {
            setSiteStatus(SiteStatus.FAILED);
            return;
        }

        savePages();
        db.Connection.closeConnection(connection);
    }

    private void savePages() {
        String sql = "INSERT INTO pages (code, content, path, site_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            addBatch(preparedStatement);
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            setSiteStatus(SiteStatus.INDEXED);
        }
    }

    private void addBatch(PreparedStatement statement) throws SQLException {
        for (Page page : pageMap.values()) {
            statement.setInt(1, page.getCode());
            statement.setString(2, page.getContent());
            statement.setString(3, page.getPath());
            statement.setInt(4, siteId);

            statement.addBatch();
            batchSize++;

            if (batchSize >= 1_000) {
                statement.executeBatch();
                batchSize = 0;
            }
        }
    }

    private void setSiteStatus(@NotNull SiteStatus status) {
        String sqlSiteStatus = "UPDATE sites SET status = '".concat(status.name())
                .concat("' WHERE id = ").concat(String.valueOf(siteId));

        try (Statement statement = connection.createStatement()) {
            statement.execute(sqlSiteStatus);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
