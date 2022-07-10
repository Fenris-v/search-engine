package main.services.indexing;

import main.entities.Field;
import main.entities.Page;
import main.entities.Site;
import main.services.HTMLCleaner;
import main.services.morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

class LemmasCounter {
    private final Indexing indexing;
    private final Morphology morphology = new Morphology();
    private final Map<String, Integer> words = new HashMap<>();
    private final Site site;
    private Document document;
    private PreparedStatement preparedStatement;

    private static final String addLemmaSql = "INSERT INTO lemmas (lemma, frequency) VALUES (?, ?) ON CONFLICT (lemma) DO UPDATE SET frequency = lemmas.frequency + 1";

    public LemmasCounter(Indexing indexing, Site site) {
        this.indexing = indexing;
        this.site = site;
    }

    void execute() {
        Pageable pageable = PageRequest.of(0, 1000);
        org.springframework.data.domain.Page<Page> pages = indexing.getPageRepository().getPagesBySite(site, pageable);
        int total = pages.getTotalPages();

        pages.forEach(this::saveLemmasForPage);

        int page = 1;
        while (total < page) {
            pageable.withPage(page);
            pages = indexing.getPageRepository().getPagesBySite(site, pageable);
            pages.forEach(this::saveLemmasForPage);
            page++;
        }
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

//        try {
//            preparedStatement = indexing.getConnection().prepareStatement(addLemmaSql);
//
//            words.forEach((word, count) -> addLemmasBatch(word));
//
//            preparedStatement.executeBatch();
//            preparedStatement.close();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void addLemmasBatch(String word) {
        try {
            preparedStatement.setString(1, word);
            preparedStatement.setInt(2, 1);
            preparedStatement.addBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
