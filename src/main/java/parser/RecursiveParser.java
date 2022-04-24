package parser;

import entities.Page;
import exceptions.parser.ServerNotRespondingException;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class RecursiveParser extends RecursiveTask<Map<String, Page>> {
    private final String parent;
    private final String domain;

    private final List<RecursiveParser> tasks = new ArrayList<>();

    public RecursiveParser(String domain, String parent) {
        this.domain = domain;
        this.parent = parent;
    }

    @Override
    protected Map<String, Page> compute() {
        try {
            if (Parser.pageMap.containsKey(parent)) {
                return null;
            }

            Connection.Response response = getResponse(parent);
            addPageToMap("/", response, domain.concat("/"));

            Set<String> urls = getLinks(response, domain.concat("/"));
            for (String url : urls) {
                if (url != null) {
                    tryAddPage(url);
                }
            }
        } catch (IOException | ServerNotRespondingException | InterruptedException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private void tryAddPage(String link) throws IOException, ServerNotRespondingException, InterruptedException {
        try {
            addPageRecursive(link);

            tasks.forEach(ForkJoinTask::join);
        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    private @NotNull Connection.Response getResponse(String path) throws IOException, ServerNotRespondingException {
        Connection.Response response = Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(5_000)
                .execute();

        if (response.statusCode() >= 500) {
            throw new ServerNotRespondingException();
        }

        return response;
    }

    private void addPageToMap(String path, @NotNull Connection.Response response, String url) {
        Page page = new Page(path, response.statusCode(), response.body());
        Parser.pageMap.put(url, page);
    }

    private Set<String> getLinks(@NotNull Connection.Response response, String parentLink) throws IOException {
        Elements links = response.parse().select("a");
        return LinkCleaner.clearLinks(links, parentLink, domain, new HashSet<>());
    }

    private void addPageRecursive(String url) throws IOException, ServerNotRespondingException, InterruptedException {
        if (Parser.pageMap.containsKey(url)) {
            return;
        }

        Thread.sleep(500);
        Connection.Response response = getResponse(url);

        String path = url.replaceAll(domain, "");
        addPageToMap(path, response, url);

        Set<String> urls = getLinks(response, url);
        for (String link : urls) {
            tasks.add((RecursiveParser) new RecursiveParser(domain, link).fork());
        }
    }
}
