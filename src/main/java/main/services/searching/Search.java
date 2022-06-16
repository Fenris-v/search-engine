package main.services.searching;

import main.models.Result;

import java.util.Set;

public class Search {
    public final static int SNIPPET_LENGTH = 250;

    private final String searchRequest;
//    private final java.sql.Connection connection;

    public Search(String searchRequest) {
        this.searchRequest = searchRequest;
//        connection = Connection.getInstance().getConnection();
    }

    public Set<Result> execute() {
//        try (Statement statement = connection.createStatement()) {
//            Set<Lemma> lemmas = new Lemmas(searchRequest, statement).getLemmas();
//            if (lemmas.isEmpty()) {
//                return null;
//            }
//
//            return new Results(statement, lemmas).getResults();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } finally {
//            Connection.closeConnection(connection);
//        }
        return null;
    }
}
