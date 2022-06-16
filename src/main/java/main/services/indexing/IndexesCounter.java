package main.services.indexing;

import main.entities.Field;
import main.entities.Page;
import main.services.morphology.Morphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class IndexesCounter {
    private final Indexing indexing;
    private final Morphology morphology = new Morphology();
    private Document document;
    private final Map<String, Float> wordsWeight = new HashMap<>();
    private final HashMap<String, Integer> lemmas = new HashMap<>();
    private PreparedStatement preparedStatement;

    private static final String addIndexSql = "INSERT INTO indexes (page_id, lemma_id, rank) VALUES (?, ?, ?)";

    public IndexesCounter(Indexing indexing) {
        this.indexing = indexing;
        setLemmas();
    }

    void execute() {
        indexing.getPages().forEach(this::saveIndexes);
    }

    private void setLemmas() {
        try (Statement statement = indexing.getConnection().createStatement()) {
            ResultSet result = statement.executeQuery("SELECT * FROM lemmas");
            while (result.next()) {
                lemmas.put(result.getString("lemma"), result.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveIndexes(@NotNull Page page) {
        if (page.getCode() != 200) {
            return;
        }

        wordsWeight.clear();
        document = Jsoup.parse(page.getContent());

        indexing.getFields().forEach(this::calculateWordsWeight);

        try {
            executeSavingIndexes(page);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    private void executeSavingIndexes(Page page) throws SQLException {
        preparedStatement = indexing.getConnection().prepareStatement(addIndexSql);
        wordsWeight.forEach((word, weight) -> addIndexToBatch(page, word, weight));
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    private void addIndexToBatch(@NotNull Page page, String word, @NotNull Float weight) {
        try {
            preparedStatement.setLong(1, page.getId());
            preparedStatement.setInt(2, lemmas.get(word));
            preparedStatement.setBigDecimal(3, new BigDecimal(weight.toString()));
            preparedStatement.addBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
