package main.services.indexing;

import main.entities.Field;
import main.entities.Lemma;
import main.entities.Page;
import main.entities.Site;
import main.repositories.PageRepository;
import main.services.HTMLCleaner;
import main.services.morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LemmasCounter {
    private final Indexing indexing;
    private final Morphology morphology = new Morphology();
    private final Map<String, Integer> words = new HashMap<>();
    private final Site site;
    private Document document;
    private PreparedStatement preparedStatement;

    private final List<Lemma> lemmas = new ArrayList<>();

    private static final String addLemmaSql = "INSERT INTO lemmas (lemma, frequency) VALUES (?, ?) ON CONFLICT (lemma) DO UPDATE SET frequency = lemmas.frequency + 1";

    public LemmasCounter(Indexing indexing, Site site) {
        this.indexing = indexing;
        this.site = site;
    }

    void execute() {
//        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("application.yaml").build();
//        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
//        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
//
//        Session session = sessionFactory.openSession();
//
//        CriteriaBuilder builder = session.getCriteriaBuilder();
//        CriteriaQuery<Page> query = builder.createQuery(Page.class);
//        Root<Page> root = query.from(Page.class);
//        query.select(root).where(builder.equal(root.<Long>get("site_id"), site.getId()));
//        List<Page> pages = session.createQuery(query).getResultList();
//
//        System.out.println(pages);

        Pageable pageable = PageRequest.of(0, 1000);
        PageRepository pageRepository = indexing.getPageRepository();
        System.out.println(pageRepository.findBySite(site));
        System.out.println(pageRepository.getPagesBySite(pageable, site));
        pageRepository.findAll().spliterator();
        System.out.println(pageRepository.getPagesBySite(pageable, site));
        org.springframework.data.domain.Page<Page> pages = indexing.getPageRepository().getPagesBySite(pageable, site);
        int total = pages.getTotalPages();

        pages.forEach(page -> {
            System.out.println(page);
            System.out.println(page.getPath());
            System.out.println(page.getSite());
        });
        System.exit(6);
//        pages.forEach(this::saveLemmasForPage);
        System.out.println(lemmas);
        System.exit(1);
        indexing.getLemmaRepository().saveLemmas(lemmas);
        lemmas.clear();

        int page = 1;
//        while (total > page) {
//            pages = indexing.getPageRepository().getPagesBySite(site, SiteParser.pageable.withPage(page));
//            pages.forEach(this::saveLemmasForPage);
//            indexing.getLemmaRepository().saveLemmas(lemmas);
//            lemmas.clear();
//            page++;
//        }
//        indexing.getPageRepository().findAll();
//        indexing.getPages().forEach(this::saveLemmasForPage);
    }

    private void saveLemmasForPage(@NotNull Page page) {
        if (page.getCode() != 200) {
            return;
        }

        words.clear();
        document = Jsoup.parse(page.getContent());
        indexing.getFields().forEach(this::countWords);

        executeSaveLemmas();
    }

    private void countWords(@NotNull Field field) {
        Element element = document.selectFirst(field.getSelector());

        if (element == null) {
            return;
        }

        HTMLCleaner.excludeJunkElements(element);
        morphology.countWords(element.text()).forEach(this::wordSetMerge);
    }

    private void wordSetMerge(String word, int count) {
        if (words.containsKey(word)) {
            count += words.get(word);
        }

        words.put(word, count);
    }

    private void executeSaveLemmas() {
        words.forEach((word, count) -> {
            Lemma lemma = new Lemma();
            lemma.setLemma(word);
            lemma.setFrequency(count);
            lemma.setSite(site);

            lemmas.add(lemma);
        });
    }
}
