package main.services;

import main.pojo.Statistic;
import main.repositories.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticService {
    private final SiteRepository siteRepository;

    public StatisticService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Map<String, Object> get() {
        Iterable<Statistic> statistics = siteRepository.getStatistic();

        Map<String, Object> response = new HashMap<>();
        response.put("result", true);
        response.put("statistics", statistics);

        return response;
    }
}
