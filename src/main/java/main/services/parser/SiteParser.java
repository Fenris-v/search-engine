package main.services.parser;

import lombok.Getter;
import main.entities.Field;
import main.entities.Site;
import main.enums.SiteStatus;
import main.pojo.ApplicationProps;
import main.repositories.FieldRepository;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SiteParser {
    @Getter
    private final ApplicationProps applicationProps;

    @Getter
    private final SiteRepository siteRepository;

    @Getter
    private final PageRepository pageRepository;

    @Getter
    private final LemmaRepository lemmaRepository;

    @Getter
    private final Iterable<Field> fields;

    private final Map<String, String> sitesMap = new HashMap<>();
    private final List<String> existsDomains = new ArrayList<>();
    private final List<Long> sitesForDelete = new ArrayList<>();
    private final List<Site> sitesForAdding = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();

    public SiteParser(
            ApplicationProps applicationProps,
            SiteRepository siteRepository,
            PageRepository pageRepository,
            LemmaRepository lemmaRepository,
            @NotNull FieldRepository fieldRepository
    ) {
        this.applicationProps = applicationProps;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;

        fields = fieldRepository.findAll();
    }

    public void run() {
        Iterable<Site> sites = getSites(applicationProps.getSites());

        sites.forEach(this::makeThread);
        threads.forEach(Thread::start);
    }

    private @NotNull Iterable<Site> getSites(@NotNull List<Map<String, String>> siteList) {
        siteList.forEach(site -> sitesMap.put(site.get("url"), site.get("name")));

        siteRepository.findAll().forEach(this::sortExistsDomains);
        siteRepository.deleteAllById(sitesForDelete);

        siteList.forEach(this::makeSite);
        siteRepository.saveAll(sitesForAdding);

        return siteRepository.findAll();
    }

    private void sortExistsDomains(@NotNull Site site) {
        if (sitesMap.containsKey(site.getUrl())) {
            existsDomains.add(site.getUrl());
            return;
        }

        sitesForDelete.add(site.getId());
    }

    private void makeSite(@NotNull Map<String, String> siteInfo) {
        if (existsDomains.contains(siteInfo.get("url"))) {
            return;
        }

        Site site = new Site();
        site.setName(siteInfo.get("name"));
        site.setUrl(siteInfo.get("url"));
        site.setStatus(SiteStatus.INDEXING.name());
        sitesForAdding.add(site);
    }

    private void makeThread(@NotNull Site site) {
        Parser parser = new Parser(site, new ConcurrentHashMap<>(), this);
        threads.add(new Thread(parser));
    }
}
