package searching;

import entities.Lemma;
import morphology.Morphology;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;

class Lemmas {
    private final static double JUNK_PERCENT = 0.9;
    private final Morphology morphology = new Morphology();

    private final String searchRequest;
    private final Statement statement;
    private final int junkLevel;

    public Lemmas(String searchRequest, @NotNull Statement statement) throws SQLException {
        this.searchRequest = searchRequest;
        this.statement = statement;
        junkLevel = getJunkLevel(statement);
    }

    @NotNull Set<Lemma> getLemmas() throws SQLException {
        Set<String> wordsInRequest = morphology.countWords(searchRequest).keySet();
        String sql = getLemmasSql(wordsInRequest);

        ResultSet result = statement.executeQuery(sql);
        Set<Lemma> lemmas = new TreeSet<>();
        while (result.next()) {
            lemmas.add(makeLemma(result));
        }

        return lemmas;
    }

    private int getJunkLevel(@NotNull Statement statement) throws SQLException {
        String sql = "SELECT COUNT(id) count FROM pages";
        ResultSet result = statement.executeQuery(sql);
        result.next();
        int pagesCount = result.getInt("count");
        return (int) (pagesCount * JUNK_PERCENT);
    }

    private @NotNull String getLemmasSql(@NotNull Set<String> wordsInRequest) {
        StringBuilder sql = new StringBuilder();
        wordsInRequest.forEach(word -> sql.append(sql.isEmpty() ? "WHERE (" : "OR ")
                .append("lemma = '")
                .append(word)
                .append("' ")
        );

        sql.append(") AND frequency < ").append(junkLevel);
        sql.insert(0, "SELECT id, lemma, frequency FROM lemmas ");
        return sql.toString();
    }

    private @NotNull Lemma makeLemma(@NotNull ResultSet result) throws SQLException {
        return new Lemma(
                result.getInt("id"),
                result.getString("lemma"),
                result.getInt("frequency")
        );
    }
}
