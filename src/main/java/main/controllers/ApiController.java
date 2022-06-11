package main.controllers;

import models.Result;
import models.Site;
import models.Statistics;
import models.Total;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import parser.Parser;
import parser.ParserSite;
import searching.Search;
import services.YamlReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@RestController
public class ApiController {
//    @Value("${sites}")
//    private List<?> sites;

    //    private static final List<ForkJoinPool> pools = new ArrayList<>();
//    private static final List<Thread> threads = new ArrayList<>();

    @GetMapping("/api/startIndexing")
    public Map<String, Object> index() {
        List<Map<String, String>> siteList = YamlReader.getSiteList();

        ParserSite parserSite;
        try {
            parserSite = new ParserSite();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Map<String, Integer> domains = parserSite.getSites(siteList);

        List<Thread> threads = new ArrayList<>();

        domains.forEach((domain, id) -> threads.add(new Thread(new Parser(domain, id, new ConcurrentHashMap<>()))));
        threads.forEach(Thread::start);

        Map<String, Object> response = new HashMap<>();
        response.put("result", true);

        return response;
    }

    @GetMapping("/api/stopIndexing")
    public Map<String, Object> stopIndex() {
//        System.out.println(pools);
//        for (ForkJoinPool pool : pools) {
//            pool.shutdownNow();
//            pool.shutdown();
//            try {
//                pool.awaitTermination(1, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

        ForkJoinPool pool1 = ForkJoinPool.commonPool();
        ForkJoinPool.commonPool().shutdownNow();
        System.out.println(pool1);
        pool1.shutdownNow();

//        for (Thread thread : threads) {
//            thread.interrupt();
//        }

        Map<String, Object> response = new HashMap<>();
        response.put("result", true);

        return response;
    }

    @GetMapping("/api/indexPage")
    public List<?> indexPage() {
        return null;
    }

    @GetMapping("/api/statistics")
    public Map<String, ?> statistics() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = db.Connection.getInstance().getConnection()) {
            assert connection != null;
            try (Statement statement = connection.createStatement()) {
                List<Site> sites = new ArrayList<>();
                String sqlSites = "SELECT s.*, COUNT(l.id) lemmas, COUNT(p.id) pages FROM sites s LEFT JOIN pages p on s.id = p.site_id LEFT JOIN lemmas l on s.id = l.site_id GROUP BY s.id";
                ResultSet resultSet = statement.executeQuery(sqlSites);

                int totalPages = 0;
                int totalLemmas = 0;
                while (resultSet.next()) {
                    sites.add(new Site(
                            resultSet.getString("url"),
                            resultSet.getString("name"),
                            resultSet.getString("status"),
                            resultSet.getTimestamp("status_time").getTime(),
                            resultSet.getString("last_error"),
                            resultSet.getInt("pages"),
                            resultSet.getInt("lemmas")
                    ));

                    totalPages += resultSet.getInt("pages");
                    totalLemmas += resultSet.getInt("lemmas");
                }

                Total total = new Total(sites.size(), totalPages, totalLemmas, true);
                Statistics statistics = new Statistics(total, sites);
                response.put("result", true);
                response.put("statistics", statistics);
            }
        } catch (SQLException e) {
            response.put("result", false);
            throw new RuntimeException(e);
        }

        return response;
    }

    @GetMapping("/api/results")
    public Set<Result> search() {
        return new Search("купить телефон недорогой").execute();
    }
}