package main.services.searching;

import main.entities.Lemma;
import main.entities.Site;
import main.services.morphology.Morphology;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

class Lemmas {
    private final static double JUNK_PERCENT = 0.9;
    private final Morphology morphology = new Morphology();

    private final String query;
    private final Session session;
    private final int junkLevel;
    private final Site site;
    private final Search search;

    Lemmas(String query, Site site, @NotNull Search search) {
        this.search = search;
        this.query = query;
        this.session = search.getConnection().getSession();
        this.site = site;
        junkLevel = getJunkLevel();
    }

    List<Lemma> getLemmas() {
        Set<String> wordsInRequest = morphology.countWords(query).keySet();
        search.setWordsCount(wordsInRequest.size());
        String sql = getLemmasSql(wordsInRequest);

        return session.createNativeQuery(sql, Lemma.class).getResultList();
    }

    private int getJunkLevel() {
        String sql = "SELECT COUNT(id) count FROM page";
        if (site != null) {
            sql += " WHERE site_id = " + site.getId();
        }

        int pagesCount = ((BigInteger) session.createNativeQuery(sql).getSingleResult()).intValue();
        return (int) (pagesCount * JUNK_PERCENT);
    }

    private @NotNull String getLemmasSql(@NotNull Set<String> wordsInRequest) {
        StringBuilder sql = new StringBuilder();
        wordsInRequest.forEach(word -> sql.append(sql.isEmpty() ? "WHERE (" : "OR ")
                .append("lemma = '")
                .append(word)
                .append("' ")
        );

        sql.insert(0, "SELECT * FROM lemma ");

        sql.append(") AND frequency < ").append(junkLevel);
        if (site != null) {
            sql.append(" AND site_id = ").append(site.getId());
        }

        return sql.toString();
    }
}
