package indexing;

import morphology.Morphology;
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
    private float weight;
    private final HashMap<String, Integer> lemmas = new HashMap<>();

    public IndexesCounter(Indexing indexing) {
        this.indexing = indexing;
        setLemmas();
    }

    void execute() {
        indexing.getPages().forEach(page -> {
            if (page.getCode() != 200) {
                return;
            }

            wordsWeight.clear();
            document = Jsoup.parse(page.getContent());

            indexing.getFields().forEach(field -> {
                Element element = document.select(field.getSelector()).first();
                String str = element == null ? "" : element.text();
                morphology.countWords(str).forEach((word, count) -> {
                    weight = count * field.getWeight();
                    if (wordsWeight.containsKey(word)) {
                        weight += wordsWeight.get(word);
                    }

                    wordsWeight.put(word, weight);
                });
            });

            try {
                String sql = "INSERT INTO indexes (page_id, lemma_id, rank) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = indexing.getConnection().prepareStatement(sql);
                wordsWeight.forEach((word, weight) -> {
                    try {
                        preparedStatement.setInt(1, page.getId());
                        preparedStatement.setInt(2, lemmas.get(word));
                        preparedStatement.setBigDecimal(3, new BigDecimal(weight.toString()));

                        preparedStatement.addBatch();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                preparedStatement.executeBatch();
                preparedStatement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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
}
