package main.services.searching;

import main.db.Connection;
import main.models.Result;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class Search {
    public final static int SNIPPET_LENGTH = 250;

    private final Connection connection;

    public Search() {
        this.connection = new Connection();
    }

    public Set<Result> execute(String query) {
        Transaction transaction = connection.getSession().beginTransaction();
//        Set<Lemma> lemmas = new Lemmas(query, connection.getSession()).getLemmas();

        return null;
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
//
//        return null;
    }
}
