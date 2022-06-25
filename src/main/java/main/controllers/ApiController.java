package main.controllers;

import main.models.Result;
import main.services.StatisticService;
import main.services.parser.SiteParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class ApiController {
    private final SiteParser siteParser;

    private final StatisticService statisticService;

    public ApiController(SiteParser siteParser, StatisticService statisticService) {
        this.siteParser = siteParser;
        this.statisticService = statisticService;
    }

    @GetMapping("/api/startIndexing")
    public Map<String, Object> index() {
        siteParser.run();

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

//        ForkJoinPool pool1 = ForkJoinPool.commonPool();
//        ForkJoinPool.commonPool().shutdownNow();
//        System.out.println(pool1);
//        pool1.shutdownNow();

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
        return statisticService.get();
    }

    @GetMapping("/api/results")
    public Set<Result> search() {
        return null;
    }
}
