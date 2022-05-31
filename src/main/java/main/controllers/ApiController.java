package main.controllers;

import models.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import parser.Parser;
import searching.Search;
import services.YamlReader;

import java.util.*;

@RestController
public class ApiController {
//    @Value("${sites}")
//    private List<?> sites;

    @GetMapping("/api/startIndexing")
    public Map<String, Object> index() {
        List<Map<String, String>> siteList = YamlReader.getSiteList();
        List<Thread> threads = new ArrayList<>();

        siteList.forEach(site -> threads.add(new Thread(new Parser(site.get("url")))));
        threads.forEach(Thread::start);

        Map<String, Object> response = new HashMap<>();
        response.put("result", true);

        return response;
    }

    @GetMapping("/api/stopIndexing")
    public Set<Result> stopIndex() {
        return new Search("купить телефон недорогой").execute();
    }

    @GetMapping("/api/indexPage")
    public Set<Result> indexPage() {
        return new Search("купить телефон недорогой").execute();
    }

    @GetMapping("/api/statistics")
    public Set<Result> statistics() {
        return new Search("купить телефон недорогой").execute();
    }

    @GetMapping("/api/results")
    public Set<Result> search() {
        return new Search("купить телефон недорогой").execute();
    }
}
