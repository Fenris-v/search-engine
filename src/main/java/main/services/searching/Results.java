package main.services.searching;

import main.entities.Index;
import main.entities.Lemma;
import main.entities.Page;
import main.entities.Site;
import main.models.Result;
import main.services.HTMLCleaner;
import main.services.morphology.Morphology;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

class Results {
    private final List<Lemma> lemmas;
    private final String mostRareWord;
    private final Morphology morphology;
    private final Session session;
    private final Site site;
    private final Search search;

    private float maxAbsRank = 0;
    private float pageAbsRank = 0;

    private final Map<Long, List<Index>> indexMap = new HashMap<>();
    private final Map<Integer, Page> pages = new HashMap<>();
    private final TreeSet<Result> results = new TreeSet<>();
    private final Map<Long, Float> pageLemmasRank = new HashMap<>();

    Results(@NotNull List<Lemma> lemmas, Site site, @NotNull Search search) {
        this.search = search;
        this.lemmas = lemmas;
        this.session = search.getConnection().getSession();
        this.site = site;
        mostRareWord = lemmas.isEmpty() ? "" : lemmas.iterator().next().getLemma();
        morphology = new Morphology();
    }

    @NotNull TreeSet<Result> getResults() {
        if (lemmas.isEmpty()) {
            return new TreeSet<>();
        }

        List<BigInteger> indexIds = getIndexesIds();
        List<Index> indexes = getIndexes(indexIds);

        putIndexToMap(indexes);
        System.out.println(indexMap);
        return prepareResults();
    }

    private List<BigInteger> getIndexesIds() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT page_id FROM index i LEFT JOIN page ON page_id = page.id WHERE lemma_id IN (");
        lemmas.forEach(lemma -> {
            stringBuilder.append(lemma.getId()).append(",");
        });

        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(")");

        if (site != null) {
            stringBuilder.append(" AND site_id = ").append(site.getId());
        }

        stringBuilder.append(" GROUP BY i.page_id HAVING COUNT(lemma_id) = ").append(search.getWordsCount());

        return (List<BigInteger>) session.createNativeQuery(stringBuilder.toString()).getResultList();
    }

    private @NotNull List<Index> getIndexes(List<BigInteger> indexIds) {
        String sql = getIndexesSql(indexIds);
        return session.createNativeQuery(sql, Index.class).getResultList();
    }

    private @NotNull String getIndexesSql(@NotNull List<BigInteger> indexIds) {
        StringBuilder sql = new StringBuilder("SELECT * FROM index i LEFT JOIN page p ON p.id = page_id WHERE i.id IN (");
        indexIds.forEach(id -> sql.append(id).append(","));

        sql.setLength(sql.length() - 1);
        sql.append(") ORDER BY rank DESC");

        return sql.toString();
    }

    private @NotNull String getResultsSql(@NotNull List<Lemma> lemmas, @NotNull List<Index> indexes) {
        StringBuilder sql = new StringBuilder("SELECT * FROM index LEFT JOIN pages ON page_id = pages.id WHERE (");
        indexes.forEach(index -> {
            if (!Objects.equals(indexes.get(0), index)) {
                sql.append(" OR ");
            }

            sql.append("page_id = ").append(index);
        });

        sql.append(") AND (");

        lemmas.forEach(lemma -> {
            if (lemmas.iterator().next() != lemma) {
                sql.append(" OR ");
            }

            sql.append("lemma_id = ").append(lemma.getId());
        });

        return sql.append(")").toString();
    }

    private void putIndexToMap(List<Index> indexes) {
        indexes.forEach(index -> {
            long pageId = index.getPage().getId();

            if (!indexMap.containsKey(pageId)) {
                indexMap.put(pageId, new ArrayList<>());
            }

            indexMap.get(pageId).add(index);
        });
    }

    private @NotNull Index makeIndex(@NotNull ResultSet resultSet) throws SQLException {
        // todo
        return null;
//        return new Index(
//                resultSet.getInt("id"),
//                resultSet.getInt("page_id"),
//                resultSet.getInt("lemma_id"),
//                resultSet.getFloat("rank")
//        );
    }

    private @NotNull TreeSet<Result> prepareResults() {
        return null;
//        indexMap.forEach(this::addResult);
//
//        results.forEach(result -> result.setRelevance(result.getAbsRank() / maxAbsRank));
//        return results;
    }

    private void addResult(Integer pageId, @NotNull List<Index> indexes) {
        pageLemmasRank.clear();
        countRanks(indexes);

        Document document = Jsoup.parse(pages.get(pageId).getContent());
        Element element = document.selectFirst("body");
        if (element == null) {
            return;
        }

        Page page = pages.get(pageId);
        Element elementTitle = document.selectFirst("title");
        String pageTitle = elementTitle == null ? "NO TITLE" : elementTitle.text();
        results.add(new Result(
                pageLemmasRank,
                pageAbsRank,
                page.getPath(),
                pageTitle,
                getSnippet(element)
        ));
    }

    private String getSnippet(Element element) {
        HTMLCleaner.excludeJunkElements(element);
        return morphology.getSnippet(element.text(), mostRareWord);
    }

    private void countRanks(@NotNull List<Index> indexes) {
        for (Index index : indexes) {
            pageAbsRank += index.getRank();
            pageLemmasRank.put(index.getLemma().getId(), index.getRank());
        }

        updateMaxAbsRank();
    }

    private void updateMaxAbsRank() {
        if (pageAbsRank > maxAbsRank) {
            maxAbsRank = pageAbsRank;
        }
    }
}
