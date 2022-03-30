package parser;

import entities.Page;
import lombok.AllArgsConstructor;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

@AllArgsConstructor
public class RecursiveParser extends RecursiveTask<Map<String, Page>> {
    private final String rootPath;
    private String domain;
    private final Map<String, Page> pages = new HashMap<>();
    private final List<RecursiveParser> parserList = new ArrayList<>();

    @Override
    protected Map<String, Page> compute() {
        try {
            domain = domain.replaceAll("/*$", "").concat(rootPath);

            Response response = Jsoup.connect(domain)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36")
                    .referrer("https://www.google.com")
                    .timeout(10000)
                    .execute();

            Page rootPage = new Page("/", response.statusCode(), response.body());

            String path = rootPage.getPath().replaceAll(domain, "");
            pages.put(path, rootPage);

            Elements links = response.parse().select("a");
//                    .stream()
//                    .map(url -> url.absUrl("href"))
//                    .filter(url -> url.contains(domain));

            LinkCleaner linkCleaner = new LinkCleaner(domain);
            List<String> cleanedLinks = linkCleaner.clearLinks(links);

            System.out.println(cleanedLinks);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        return pages;
    }
}
