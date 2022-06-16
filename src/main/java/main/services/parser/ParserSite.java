package main.services.parser;

import main.entities.Site;
import main.enums.SiteStatus;
import main.repositories.SiteRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParserSite {
    private final PreparedStatement preparedStatement = null;
    private final Statement statement = null;

    @Autowired
    private SiteRepository siteRepository;

    public ParserSite() throws SQLException {
        System.out.println(siteRepository);
//        java.sql.Connection connection = Connection.getInstance().getConnection();
//
//        if (connection == null) {
//            throw new SQLException();
//        }
//
//        String insertDomain = "INSERT INTO sites (name, url, status) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
//        preparedStatement = connection.prepareStatement(insertDomain);
//
//        statement = connection.createStatement();
    }

    public Map<String, Integer> getSites(@NotNull List<Map<String, String>> siteList) {
        Iterable<Site> sites = siteRepository.findAll();
        System.out.println(sites);
        siteList.forEach(site -> addSites(preparedStatement, site));

        try {
            return syncDomains();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addSites(@NotNull PreparedStatement preparedStatement, @NotNull Map<String, String> site) {
        try {
            preparedStatement.setString(1, site.get("name"));
            preparedStatement.setString(2, site.get("url"));
            preparedStatement.setObject(3, SiteStatus.INDEXING, Types.OTHER);
            preparedStatement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private @NotNull Map<String, Integer> syncDomains() throws SQLException {
        Map<String, Integer> domains = new HashMap<>();

        preparedStatement.executeBatch();
        preparedStatement.close();

        ResultSet resultSet = statement.executeQuery("SELECT id, url FROM sites");
        while (resultSet.next()) {
            domains.put(resultSet.getString("url"), resultSet.getInt("id"));
        }

        statement.close();

        return domains;
    }
}
