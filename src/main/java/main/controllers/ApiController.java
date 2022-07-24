package main.controllers;

import main.exceptions.DomainNotInListException;
import main.services.StatisticService;
import main.services.parser.PageParser;
import main.services.parser.SiteParser;
import main.services.searching.Search;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

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
            return getResponse(false, "Индексация уже запущена");
        }

        IS_PARSE = true;

        new Thread(siteParser::run).start();
        return getResponse();
    }

    @GetMapping("/api/stopIndexing")
    public Map<String, Object> stopIndex() {
        IS_PARSE = false;
        return getResponse();
    }

    @PostMapping("/api/indexPage")
    public Map<String, Object> indexPage(String url) {
        try {
            pageParser.parse(url);
            return getResponse();
        } catch (DomainNotInListException | IOException e) {
            return getResponse(false, e.getMessage());
        }
    }

    @GetMapping("/api/statistics")
    public Map<String, ?> statistics() {
        return statisticService.get();
    }

    @GetMapping("/api/search")
    public Map<String, Object> search(@NotNull String query) {
//        if (query.isEmpty()) {
//            return getResponse(false, "Задан пустой поисковый запрос");
//        }
//
//        Set<Result> results = search.execute(query);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("result", true);
//        response.put("count", results.size());
//        response.put("data", results);
//
//        return response;
        return null;
    }
}
