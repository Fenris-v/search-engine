package parser;

import db.DbConnection;
import entities.Page;
import exceptions.parser.ServerNotRespondingException;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parser {
    private String domain = "http://www.playback.ru";
    private int batchSize = 0;

    private final DbConnection dbConnection = new DbConnection();
    private final Map<String, Page> pageMap = new HashMap<>();
    private final LinkCleaner linkCleaner = new LinkCleaner(domain);

    public void parseSite() {
        try {
            execute();
        } catch (IOException | ServerNotRespondingException | SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void execute() throws IOException, ServerNotRespondingException, SQLException, InterruptedException {
        Response response = getResponse(domain.concat("/"));
        addPageToMap("/", response, domain.concat("/"));

        Set<String> urls = getLinks(response, domain.concat("/"));
        for (String url : urls) {
            tryAddPage(url);
        }

        savePages();
    }

    private void tryAddPage(String link) throws IOException, ServerNotRespondingException, InterruptedException {
        try {
            addPageRecursive(link);
        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    private @NotNull Response getResponse(String path) throws IOException, ServerNotRespondingException {
        Response response = Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(5_000)
                .execute();

        if (response.statusCode() >= 500) {
            throw new ServerNotRespondingException();
        }

        return response;
    }

    private void addPageToMap(String path, @NotNull Response response, String url) {
        Page page = new Page(path, response.statusCode(), response.body());
        pageMap.put(url, page);
    }

    private Set<String> getLinks(@NotNull Response response, String parentLink) throws IOException {
        Elements links = response.parse().select("a");
        return linkCleaner.clearLinks(links, parentLink);
    }

    private void addPageRecursive(String url) throws IOException, ServerNotRespondingException, InterruptedException {
        if (pageMap.containsKey(url)) {
            return;
        }

        Thread.sleep(500);
        Response response = getResponse(url);

        String path = url.replaceAll(domain, "");
        addPageToMap(path, response, url);

        Set<String> urls = getLinks(response, url);
        for (String link : urls) {
            tryAddPage(link);
        }
    }

    private void savePages() {
        String sql = "INSERT INTO pages (code, content, path) VALUES (?, ?, ?)";

        try (Connection connection = this.dbConnection.getConnection()) {
            assert connection != null;
            dropTableIfExists(connection);
            createTable(connection);

            PreparedStatement statement = connection.prepareStatement(sql);
            addBatch(statement);

            statement.executeBatch();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
