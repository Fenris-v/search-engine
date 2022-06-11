package parser;

import entities.Page;
import exceptions.ServerNotRespondingException;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class RecursiveParser extends RecursiveTask<Map<String, Page>> {
    private final String parent;

    private final List<RecursiveParser> tasks = new ArrayList<>();
    private final Parser parser;

    public RecursiveParser(Parser parser, String parent) {
        this.parser = parser;
        this.parent = parent;
    }

    @Override
    protected Map<String, Page> compute() {
        String link = parser.getDomain().concat("/");

        try {
            if (parser.getPageMap().containsKey(parent)) {
                return null;
            }

            Connection.Response response = getResponse(parent);
            addPageToMap("/", response, link);

            Set<String> urls = getLinks(response, link);
            for (String url : urls) {
                if (url != null) {
                    tryAddPage(url);
                }
            }
        } catch (HttpStatusException e) {
            ParserErrorHandler.saveNotOkResponse(e, parser, link);
        } catch (IOException | ServerNotRespondingException | InterruptedException e) {
            Parser.logger.warn(e.getMessage());
        }

        return null;
    }

    private void tryAddPage(String link) throws IOException, ServerNotRespondingException, InterruptedException {
        try {
            addPageRecursive(link);

            tasks.forEach(ForkJoinTask::join);
        } catch (SocketTimeoutException e) {
            Parser.logger.warn(e.getMessage());
        }
    }

    private @NotNull Connection.Response getResponse(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent(parser.getUserAgent())
                .referrer(parser.getReferrer())
                .timeout(5_000)
                .execute();
    }

    private void addPageToMap(String path, @NotNull Connection.Response response, String url) {
        Page page = new Page(path, response.statusCode(), response.body(), parser.getSiteId());
        parser.getPageMap().put(url, page);
    }

    private Set<String> getLinks(@NotNull Connection.Response response, String parentLink) throws IOException {
        Elements links = response.parse().select("a");
        return LinkCleaner.clearLinks(links, parentLink, parser.getDomain(), new HashSet<>());
    }

    private void addPageRecursive(String url) throws IOException, InterruptedException {
        if (parser.getPageMap().containsKey(url)) {
            return;
        }

        Thread.sleep(500);

        try {
            Connection.Response response = getResponse(url);

            String path = url.replaceAll(parser.getDomain(), "");
            addPageToMap(path, response, url);

            Set<String> urls = getLinks(response, url);
            for (String link : urls) {
                tasks.add((RecursiveParser) new RecursiveParser(parser, link).fork());
            }
        } catch (HttpStatusException e) {
            ParserErrorHandler.saveNotOkResponse(e, parser, url);
        }
    }
}
