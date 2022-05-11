package searching;

import entities.Index;
import entities.Lemma;
import entities.Page;
import models.Result;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

class Results {
    private final Statement statement;

    private float maxAbsRank = 0;
    private float pageAbsRank = 0;
    private final Map<Integer, List<Index>> indexMap = new HashMap<>();
    private final Map<Integer, Page> pages = new HashMap<>();
    private final TreeSet<Result> results = new TreeSet<>();
    private final Map<Integer, Float> pageLemmasRank = new HashMap<>();

    public Results(Statement statement) {
        this.statement = statement;
    }

    @NotNull TreeSet<Result> getResults(@NotNull Set<Lemma> lemmas) throws SQLException {
        List<Integer> indexes = getIndexes(lemmas);

        String sql = getResultsSql(lemmas, indexes);
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            int pageId = resultSet.getInt("page_id");
            addPageToList(resultSet, pageId);
            putIndexToMap(resultSet, pageId);
        }

        return prepareResults();
    }

    private @NotNull List<Integer> getIndexes(@NotNull Set<Lemma> lemmas) {
        List<Integer> indexes = new ArrayList<>();
        for (Lemma lemma : lemmas) {
            getIndexesByLemma(indexes, lemma);

            if (indexes.isEmpty()) {
                break;
            }
        }

        return indexes;
    }

    private void getIndexesByLemma(List<Integer> indexes, Lemma lemma) {
        try {
            String sql = getIndexesSql(indexes, lemma);
            ResultSet resultSet = statement.executeQuery(sql);
            indexes.clear();

            while (resultSet.next()) {
                indexes.add(resultSet.getInt("page_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull String getIndexesSql(@NotNull List<Integer> indexes, @NotNull Lemma lemma) {
        StringBuilder sql = new StringBuilder("SELECT * FROM indexes WHERE lemma_id = " + lemma.getId());

        if (!indexes.isEmpty()) {
            sql.append(" AND (");
            indexes.forEach(index -> {
                if (!Objects.equals(indexes.get(0), index)) {
                    sql.append(" OR ");
                }

                sql.append(" page_id = ").append(index);
            });
            sql.append(")");
        }

        return sql.toString();
    }

    private @NotNull String getResultsSql(@NotNull Set<Lemma> lemmas, @NotNull List<Integer> indexes) {
        StringBuilder sql = new StringBuilder("SELECT * FROM indexes LEFT JOIN pages ON page_id = pages.id WHERE (");
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

    private void addPageToList(ResultSet resultSet, int pageId) throws SQLException {
        if (pages.containsKey(pageId)) {
            return;
        }

        pages.put(pageId, makePage(resultSet, pageId));
    }

    private @NotNull Page makePage(@NotNull ResultSet resultSet, int pageId) throws SQLException {
        return new Page(
                pageId,
                resultSet.getString("path"),
                resultSet.getInt("code"),
                resultSet.getString("content")
        );
    }

    private void putIndexToMap(@NotNull ResultSet resultSet, int pageId) throws SQLException {
        if (!indexMap.containsKey(pageId)) {
            indexMap.put(pageId, new ArrayList<>(List.of(makeIndex(resultSet))));
            return;
        }

        indexMap.get(pageId).add(makeIndex(resultSet));
    }

    private @NotNull Index makeIndex(@NotNull ResultSet resultSet) throws SQLException {
        return new Index(
                resultSet.getInt("id"),
                resultSet.getInt("page_id"),
                resultSet.getInt("lemma_id"),
                resultSet.getFloat("rank")
        );
    }

    private @NotNull TreeSet<Result> prepareResults() {
        indexMap.forEach(this::addResult);

        results.forEach(result -> result.setRelevance(result.getAbsRank() / maxAbsRank));
        return results;
    }

    private void addResult(Integer pageId, @NotNull List<Index> indexes) {
        pageLemmasRank.clear();
        countRanks(indexes);

        results.add(new Result(pageLemmasRank, pageAbsRank, pages.get(pageId)));
    }

    private void countRanks(@NotNull List<Index> indexes) {
        for (Index index : indexes) {
            pageAbsRank += index.getRank();
            pageLemmasRank.put(index.getLemmaId(), index.getRank());
        }

        updateMaxAbsRank();
    }

    private void updateMaxAbsRank() {
        if (pageAbsRank > maxAbsRank) {
            maxAbsRank = pageAbsRank;
        }
    }
}
