package indexing;

import entities.Field;
import entities.Page;
import morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

class LemmasCounter {
    private final Indexing indexing;
    private final Morphology morphology = new Morphology();
    private final Map<String, Integer> words = new HashMap<>();
    private Document document;
    private PreparedStatement preparedStatement;

    public LemmasCounter(Indexing indexing) {
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

        executeSaveLemmas();
    }

    private void countWords(@NotNull Field field) {
        Element element = document.select(field.getSelector()).first();
        String str = element == null ? "" : element.text();
        morphology.countWords(str).forEach(this::wordSetMerge);
    }

    private void wordSetMerge(String word, int count) {
        if (words.containsKey(word)) {
            count += words.get(word);
        }

        words.put(word, count);
    }

    private void executeSaveLemmas() {
        try {
            String sql = "INSERT INTO lemmas (lemma, frequency) VALUES (?, ?) ON CONFLICT (lemma) DO UPDATE SET frequency = lemmas.frequency + 1";
            preparedStatement = indexing.getConnection().prepareStatement(sql);

            words.forEach((word, count) -> addLemmasBatch(word));

            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
