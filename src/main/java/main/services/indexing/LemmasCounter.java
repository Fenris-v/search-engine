package main.services.indexing;

import main.entities.Field;
import main.entities.Page;
import main.services.HTMLCleaner;
import main.services.morphology.Morphology;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LemmasCounter {
    private final Indexing indexing;
    private final long siteId;
    private Document document;
    private final Morphology morphology = new Morphology();
    private final Map<String, Integer> words = new HashMap<>();
    private final StringBuilder stringBuilder = new StringBuilder();

    public LemmasCounter(@NotNull Indexing indexing) {
        this.indexing = indexing;
        this.siteId = indexing.getSite().getId();
    }

    void execute(@NotNull List<Page> pages) {
        pages.forEach(this::saveLemmasForPage);
    }

    private Session getSession() {
        return indexing.getConnection().getSession();
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
        morphology.countWords(element.text()).forEach(this::addWord);
    }

    private void addWord(String word, int count) {
        words.put(word, words.getOrDefault(word, 0) + count);

        if (words.size() >= 1_000_000) {
            saveWords();
        }
    }

    private void save() {
        if (!words.isEmpty()) {
            saveWords();
        }
    }

    private void saveWords() {
        buildQuery();
        words.clear();

        executeQuery();
        stringBuilder.setLength(0);
    }

    private void buildQuery() {
        stringBuilder.append("INSERT INTO lemma (lemma, frequency, site_id) VALUES ");
        words.forEach(this::addValuesToQuery);

        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(" ON CONFLICT (lemma, site_id) DO UPDATE SET frequency = lemma.frequency + EXCLUDED.frequency");
    }

    private void addValuesToQuery(String word, int count) {
        stringBuilder.append("('")
                .append(word)
                .append("',")
                .append(count)
                .append(",")
                .append(siteId)
                .append("),");
    }

    private void executeQuery() {
        getSession().createNativeQuery(stringBuilder.toString()).executeUpdate();

        getSession().flush();
        getSession().clear();
        indexing.getTransaction().commit();
        indexing.setTransaction(getSession().beginTransaction());
    }
}
