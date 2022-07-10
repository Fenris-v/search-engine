package main.controllers;

import main.models.Result;
import main.services.StatisticService;
import main.services.parser.SiteParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class ApiController extends AbstractApiController {
    private final SiteParser siteParser;

    private final StatisticService statisticService;

    public static boolean IS_PARSE = false;

    public ApiController(SiteParser siteParser, StatisticService statisticService) {
        this.siteParser = siteParser;
        this.statisticService = statisticService;
    }

    @GetMapping("/api/startIndexing")
    public Map<String, Object> index() {
        IS_PARSE = true;
        siteParser.run();
        return getBoolResponse();
    }

    @GetMapping("/api/stopIndexing")
    public Map<String, Object> stopIndex() {
        IS_PARSE = false;
        return getBoolResponse();
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
