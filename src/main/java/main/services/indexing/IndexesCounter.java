package main.services.indexing;

import main.entities.Field;
import main.entities.Page;
import main.services.morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexesCounter {
    private final Indexing indexing;
    private final Morphology morphology = new Morphology();
    private Document document;
    private final Map<String, Float> wordsWeight = new HashMap<>();

    private static final String addIndexSql = "INSERT INTO index (page_id, lemma_id, rank) VALUES (?0, ?1, ?2)";

    public IndexesCounter(Indexing indexing) {
        this.indexing = indexing;
    }

    void execute(@NotNull List<Page> pages) {
        pages.forEach(this::saveIndexes);
    }

    private void saveIndexes(@NotNull Page page) {
        if (page.getCode() != 200) {
            return;
        }

        document = Jsoup.parse(page.getContent());
        indexing.getFields().forEach(this::calculateWordsWeight);

        save(page);
    }

    private void calculateWordsWeight(@NotNull Field field) {
        Element element = document.selectFirst(field.getSelector());
        String str = element == null ? "" : element.text();
        morphology.countWords(str).forEach((word, count) -> calculateWordWeight(field, word, count));
    }

    private void calculateWordWeight(@NotNull Field field, String word, Integer count) {
        float weight = count * field.getWeight();
        if (wordsWeight.containsKey(word)) {
            weight += wordsWeight.get(word);
        }

        wordsWeight.put(word, weight);
    }

    private void save(Page page) {
        wordsWeight.forEach((word, weight) -> executeSave(page, word, weight));
    }

    private void executeSave(@NotNull Page page, String word, Float weight) {
        try {
            String sql = "SELECT * FROM lemma WHERE site_id = ?0 AND lemma = ?1";
//            Lemma lemma = indexing.getSession()
//                    .createNativeQuery(sql, Lemma.class)
//                    .setParameter(0, indexing.getSite().getId())
//                    .setParameter(1, word)
//                    .getSingleResult();
//
//            indexing.getSession()
//                    .createNativeQuery(addIndexSql)
//                    .setParameter(0, page.getId())
//                    .setParameter(1, lemma.getId())
//                    .setParameter(2, weight)
//                    .executeUpdate();
//
//            indexing.getSession().flush();
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        }
    }
}
