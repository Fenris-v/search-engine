package main.controllers;

import main.exceptions.DomainNotInListException;
import main.models.Result;
import main.services.parser.PageParser;
import main.services.parser.SiteParser;
import main.services.searching.Search;
import main.services.statistic.StatisticService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class ApiController extends AbstractApiController {
    private final SiteParser siteParser;
    private final StatisticService statisticService;
    public static boolean IS_PARSE = false;
    private final PageParser pageParser;
    private final Search search;

    public ApiController(SiteParser siteParser, StatisticService statisticService,
                         PageParser pageParser, Search search) {
        this.siteParser = siteParser;
        this.statisticService = statisticService;
        this.pageParser = pageParser;
        this.search = search;
    }

    @GetMapping("/api/startIndexing")
    public Map<String, Object> index() {
        if (IS_PARSE) {
            return getSimpleResponse(false, "Индексация уже запущена");
        }

        IS_PARSE = true;

        new Thread(siteParser::run).start();
        return getSimpleResponse();
    }

    @GetMapping("/api/stopIndexing")
    public Map<String, Object> stopIndex() {
        IS_PARSE = false;
        return getSimpleResponse();
    }

    @PostMapping("/api/indexPage")
    public Map<String, Object> indexPage(String url) {
        try {
            pageParser.parse(url);
            return getSimpleResponse();
        } catch (DomainNotInListException | IOException e) {
            return getSimpleResponse(false, e.getMessage());
        }
    }

    @GetMapping("/api/statistics")
    public Map<String, ?> statistics() {
        return statisticService.get();
    }

    @GetMapping("/api/search")
    public Map<String, Object> search(
            @RequestParam(required = false, defaultValue = "") @NotNull String query,
            @RequestParam(required = false, defaultValue = "") String site,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        if (query.isEmpty()) {
            return getSimpleResponse(false, "Задан пустой поисковый запрос");
        }

        try {
            Set<Result> results = search.execute(query, site, offset, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("result", true);
            response.put("count", results.size());
            response.put("data", results);

            return response;
        } catch (DomainNotInListException e) {
            return getSimpleResponse(false, e.getMessage());
        }
    }
}
