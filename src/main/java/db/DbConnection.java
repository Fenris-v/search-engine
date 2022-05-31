package db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.YamlReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DbConnection {
    private final String url;
    private final String user;
    private final String password;

//    @Value("${db.url}")
//    public String url;
//
//    @Value("${user}")
//    public String user;
//
//    @Value("${db.password}")
//    public String password;

    public DbConnection() {
        Map<String, String> data = YamlReader.getConfigs("db");
        url = data.get("url");
        user = data.get("user");
        password = data.get("password");
    }

    public @Nullable Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void closeConnection(@NotNull Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
