package main.services.indexing;

import main.controllers.ApiController;
import main.entities.Field;
import main.entities.Page;
import main.entities.Word;
import main.services.morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IndexesCounter extends AbstractCounter {
    private final Morphology morphology = new Morphology();
    private Document document;
    private final Map<String, Float> wordsWeight = new HashMap<>();
    private final Map<String, Long> lemmas = new HashMap<>();

    public IndexesCounter(Indexing indexing) {
        super(indexing);
    }

    void execute(@NotNull List<Page> pages) {
        pages.forEach(this::saveIndexes);
    }

    private void saveIndexes(@NotNull Page page) {
        if (!ApiController.IS_PARSE || page.getCode() != 200) {
            return;
        }

        document = Jsoup.parse(page.getContent());
        indexing.getFields().forEach(this::calculateWordsWeight);

        save(page);
    }

    private void calculateWordsWeight(@NotNull Field field) {
        if (!ApiController.IS_PARSE) {
            return;
        }

        Element element = document.selectFirst(field.getSelector());
        String str = element == null ? "" : element.text();
        morphology.countWords(str).forEach((word, count) -> calculateWordWeight(field, word, count));
    }

    private void calculateWordWeight(@NotNull Field field, String word, Integer count) {
        if (!ApiController.IS_PARSE) {
            return;
        }

        float weight = count * field.getWeight();
        if (wordsWeight.containsKey(word)) {
            weight += wordsWeight.get(word);
        }

        wordsWeight.put(word, weight);
    }

    private void save(Page page) {
        if (wordsWeight.isEmpty()) {
            return;
        }

        getLemmas();

        buildQuery(page);
        executeQuery();
        stringBuilder.setLength(0);
    }

    private void getLemmas() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT id, lemma FROM lemma WHERE lemma.lemma IN (");

        wordsWeight.forEach((word, weight) -> builder.append("'").append(word).append("',"));

        builder.setLength(builder.length() - 1);
        builder.append(") AND lemma.site_id = ").append(siteId);

        List<Word> list = getSession().createNativeQuery(builder.toString(), Word.class).getResultList();
        list.forEach(word -> lemmas.put(word.getLemma(), word.getId()));
    }

    private void buildQuery(Page page) {
        stringBuilder.append("INSERT INTO index (page_id, lemma_id, rank) VALUES ");

        wordsWeight.forEach((word, weight) -> addValuesToQuery(page, word, weight));
        wordsWeight.clear();
        lemmas.clear();

        stringBuilder.setLength(stringBuilder.length() - 1);
    }

    private void addValuesToQuery(@NotNull Page page, String word, Float weight) {
        if (!lemmas.containsKey(word)) {
            return;
        }

        stringBuilder.append("(")
                .append(page.getId())
                .append(",")
                .append(lemmas.get(word))
                .append(",")
                .append(weight)
                .append("),");
    }
}
