package main.services.searching;

import main.entities.Lemma;
import main.services.morphology.Morphology;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

class Lemmas {
    private final static double JUNK_PERCENT = 0.9;
    private final Morphology morphology = new Morphology();

    private final String searchRequest;
    private final Session session;
    private final int junkLevel;

    public Lemmas(String searchRequest, Session session) {
        this.searchRequest = searchRequest;
        this.session = session;
        junkLevel = getJunkLevel();
    }

    @NotNull Set<Lemma> getLemmas() throws SQLException {
        Set<String> wordsInRequest = morphology.countWords(searchRequest).keySet();
        String sql = getLemmasSql(wordsInRequest);

        ResultSet result = session.executeQuery(sql);
        Set<Lemma> lemmas = new TreeSet<>();
        while (result.next()) {
            lemmas.add(makeLemma(result));
        }

        return lemmas;
    }

    private int getJunkLevel() {
        String sql = "SELECT COUNT(id) count FROM page";
        int pagesCount = (int) session.createNativeQuery(sql).getSingleResult();
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
        // todo
        return null;
//        return new Lemma(
//                result.getInt("id"),
//                result.getString("lemma"),
//                result.getInt("frequency"),
//                result.getInt("site_id")
//        );
    }
}
