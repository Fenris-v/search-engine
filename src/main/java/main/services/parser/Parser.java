package main.services.parser;

import lombok.Getter;
import main.entities.Page;
import main.entities.Site;
import main.enums.SiteStatus;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

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

    private final SiteRepository siteRepository;

    @Getter
    private final PageRepository pageRepository;

    public Parser(Site site, Map<String, Page> pageMap, @NotNull SiteParser siteParser) {
        this.pageMap = pageMap;
        this.site = site;
        referrer = siteParser.getApplicationProps().getUserAgent();
        userAgent = siteParser.getApplicationProps().getUserAgent();
        siteRepository = siteParser.getSiteRepository();
        pageRepository = siteParser.getPageRepository();
    }

    public void saveParseError(String message) {
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(message);

        siteRepository.save(site);
    }

    @Override
    public void run() {
        new ForkJoinPool().invoke(new RecursiveParser(this, site.getUrl().concat("/")));

        if (pageMap.size() <= 1) {
            setSiteStatus(SiteStatus.FAILED);
            return;
        }

        savePages();
    }

    private void savePages() {
        pageRepository.saveAll(pageMap.values());
        setSiteStatus(SiteStatus.INDEXED);
    }

    private void setSiteStatus(@NotNull SiteStatus status) {
        site.setStatus(status.name());
        siteRepository.save(site);
    }
}
