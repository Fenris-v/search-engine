package searching;

import db.DbConnection;
import entities.Lemma;
import models.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class Search {
    private final String searchRequest;
    private final Connection connection;

    public Search(String searchRequest) {
        this.searchRequest = searchRequest;
        connection = new DbConnection().getConnection();
    }

    public Set<Result> execute() {
        try (Statement statement = connection.createStatement()) {
            Set<Lemma> lemmas = new Lemmas(searchRequest, statement).getLemmas();
            return new Results(statement).getResults(lemmas);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbConnection.closeConnection(connection);
        }
    }
}
