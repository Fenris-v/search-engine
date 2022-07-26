package main.services.statistic;

import main.controllers.ApiController;
import main.db.Connection;
import main.entities.Site;
import main.repositories.SiteRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticService {
    private final Iterable<Site> sites;
    private final Connection connection;
    private final Map<Long, Map<String, Object>> sitesMap = new HashMap<>();
    private final Map<String, Object> total = new HashMap<>();
    private int totalPages = 0;
    private int totalLemmas = 0;

    public StatisticService(@NotNull SiteRepository siteRepository) {
        this.connection = new Connection();
        sites = siteRepository.findAll();
    }

    public Map<String, Object> get() {
        createSitesMap();
        countPages();
        countLemmas();
        setTotal();

        return getResponse();
    }

    private void createSitesMap() {
        sites.forEach(siteEntity -> {
            Map<String, Object> site = new HashMap<>();
            site.put("url", siteEntity.getUrl());
            site.put("name", siteEntity.getName());
            site.put("status", siteEntity.getStatus());
            site.put("statusTime", siteEntity.getStatusTime());
            site.put("error", siteEntity.getLastError());
            site.put("pages", 0);
            site.put("lemmas", 0);

            sitesMap.put(siteEntity.getId(), site);
        });
    }

    private void countPages() {
        List<?> list = connection.getSession().createNativeQuery("SELECT site_id, COUNT(id) count FROM page GROUP BY site_id").getResultList();
        for (Object record : list) {
            List<Object> obj = Arrays.stream((Object[]) record).toList();
            int pages = Integer.parseInt(obj.get(1).toString());
            totalPages += pages;
            sitesMap.get(Long.parseLong(obj.get(0).toString())).put("pages", Integer.parseInt(obj.get(1).toString()));
        }
    }

    private void countLemmas() {
        List<?> list = connection.getSession()
                .createNativeQuery("SELECT site_id, COUNT(id) count FROM lemma GROUP BY site_id")
                .getResultList();

        for (Object record : list) {
            List<Object> obj = Arrays.stream((Object[]) record).toList();
            int lemmas = Integer.parseInt(obj.get(1).toString());
            totalLemmas += lemmas;
            sitesMap.get(Long.parseLong(obj.get(0).toString())).put("lemmas", Integer.parseInt(obj.get(1).toString()));
        }
    }

    private void setTotal() {
        total.put("sites", sitesMap.size());
        total.put("pages", totalPages);
        total.put("lemmas", totalLemmas);
        total.put("isIndexing", ApiController.IS_PARSE);
    }

    private @NotNull Map<String, Object> getResponse() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("detailed", sitesMap.values());

        Map<String, Object> response = new HashMap<>();
        response.put("result", true);
        response.put("statistics", statistics);

        return response;
    }
}
