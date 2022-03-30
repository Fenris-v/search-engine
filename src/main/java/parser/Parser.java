package parser;

import entities.Page;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class Parser {
    private final String domain = "http://www.playback.ru";

    public void parseSite() {
        //            Response response = Jsoup.connect(domain)
//                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36")
//                    .referrer("https://www.google.com")
//                    .timeout(10000)
//                    .execute();

//            Page rootPage = new Page("/", response.statusCode(), response.body());
        Map<String, Page> pages = new ForkJoinPool().invoke(new RecursiveParser("/", domain));
//            pages.put("/", rootPage);

//            List<String> links = response.parse().select("a").stream()
//                    .map(url -> url.absUrl("href"))
//                    .filter(url -> url.contains(domain))
//                    .toList();
//
//            links.forEach(System.out::println);
    }
}
