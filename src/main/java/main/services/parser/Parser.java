package main.services.parser;

import lombok.Getter;
import main.entities.Page;
import main.entities.Site;
import main.enums.SiteStatus;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import main.services.indexing.Indexing;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class Parser implements Runnable {
    @Getter
    private final Site site;

    @Getter
    private final Map<String, Page> pageMap;
    static final Logger logger = LoggerFactory.getLogger(Parser.class);

    @Getter
    private final String referrer;

    @Getter
    private final String userAgent;

    @Getter
    private final PageRepository pageRepository;

    @Getter
    private final LemmaRepository lemmaRepository;

    private final SiteParser siteParser;

    public Parser(Site site, Map<String, Page> pageMap, @NotNull SiteParser siteParser) {
        this.pageMap = pageMap;
        this.site = site;
        this.siteParser = siteParser;
        referrer = siteParser.getApplicationProps().getUserAgent();
        userAgent = siteParser.getApplicationProps().getUserAgent();
        pageRepository = siteParser.getPageRepository();
        lemmaRepository = siteParser.getLemmaRepository();
    }

    public void saveParseError(String message) {
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(message);

        siteParser.getSiteRepository().save(site);
    }

    @Override
    public void run() {
//        new ForkJoinPool().invoke(new RecursiveParser(this, site.getUrl().concat("/")));

//        if (pageMap.size() <= 1) {
//            setSiteStatus(SiteStatus.FAILED);
//            return;
//        }

        savePages();
        new Indexing(siteParser.getFields()).execute(site);
    }

    private void savePages() {
        pageRepository.saveAll(pageMap.values());
        setSiteStatus(SiteStatus.INDEXED);
    }

    private void setSiteStatus(@NotNull SiteStatus status) {
        site.setStatus(status.name());
        siteParser.getSiteRepository().save(site);
    }
}
