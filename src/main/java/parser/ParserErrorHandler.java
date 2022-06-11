package parser;

import db.Connection;
import entities.Page;
import org.jetbrains.annotations.NotNull;
import org.jsoup.HttpStatusException;

import java.sql.SQLException;
import java.sql.Statement;

public class ParserErrorHandler {
    static void saveNotOkResponse(@NotNull HttpStatusException e, @NotNull Parser parser, String url) {
        java.sql.Connection connection = Connection.getInstance().getConnection();

        Page page = new Page(getPath(parser.getDomain(), url), e.getStatusCode(), null, parser.getSiteId());
        parser.getPageMap().put(url, page);

        if (connection == null) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            String sql = "UPDATE sites SET status_time = NOW() , last_error = '"
                    .concat(e.getMessage())
                    .concat("' WHERE url = '")
                    .concat(parser.getDomain())
                    .concat("'");

            statement.execute(sql);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static @NotNull String getPath(@NotNull String domain, @NotNull String url) {
        String path = url.replaceAll(domain, "");
        path = path.isEmpty() ? "/" : path;
        return path;
    }
}
