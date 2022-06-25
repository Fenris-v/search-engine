package main.services;

import main.pojo.Statistic;
import main.repositories.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatisticService {
    private final SiteRepository siteRepository;

    public StatisticService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Map<String, Object> get() {
        Iterable<Statistic> statistic = siteRepository.getStatistic();
        System.out.println(statistic);

        return null;
    }
}
