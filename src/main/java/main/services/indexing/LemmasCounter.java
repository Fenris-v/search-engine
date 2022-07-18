package main.services.indexing;

import main.entities.Field;
import main.entities.Page;
import main.services.HTMLCleaner;
import main.services.morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

class LemmasCounter {
    private final Indexing indexing;
    private Document document;
    private final Morphology morphology = new Morphology();
    private final Map<String, Integer> words = new HashMap<>();
    private static final String addLemmaSql = "INSERT INTO lemma (lemma, frequency, site_id) VALUES (?0, ?1, ?2) ON CONFLICT (site_id, lemma) DO UPDATE SET frequency = ?3";

    public LemmasCounter(@NotNull Indexing indexing) {
        this.indexing = indexing;
    }

    void execute() {
        indexing.getPages().forEach(this::saveLemmasForPage);
    }

    private void saveLemmasForPage(@NotNull Page page) {
        if (page.getCode() != 200) {
            return;
        }

        words.clear();
        document = Jsoup.parse(page.getContent());
        indexing.getFields().forEach(this::countWords);

        save();
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

    private void save() {
        words.forEach(this::executeSave);
        indexing.getSession().flush();
    }

    private void executeSave(String word, Integer count) {
        indexing.getSession()
                .createNativeQuery(addLemmaSql)
                .setParameter(0, word)
                .setParameter(1, count)
                .setParameter(2, indexing.getSite().getId())
                .setParameter(3, count)
                .executeUpdate();
    }
}
