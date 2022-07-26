package main.services.searching;

import lombok.Getter;
import lombok.Setter;
import main.db.Connection;
import main.entities.Lemma;
import main.entities.Site;
import main.exceptions.DomainNotInListException;
import main.models.Result;
import main.pojo.ApplicationProps;
import main.repositories.SiteRepository;
import main.services.SiteList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class Search {
    public final static int SNIPPET_LENGTH = 250;

    @Getter
    @Setter
    private int wordsCount = 0;

    @Getter
    private final Connection connection;

    private final SiteRepository siteRepository;
    private final ApplicationProps applicationProps;

    public Search(SiteRepository siteRepository, @NotNull ApplicationProps applicationProps) {
        this.connection = new Connection();
        this.siteRepository = siteRepository;
        this.applicationProps = applicationProps;
    }

    public Set<Result> execute(String query, String site, int offset, int limit) throws DomainNotInListException {
        Site siteEntity = getSite(site);
        List<Lemma> lemmas = new Lemmas(query, siteEntity, this).getLemmas();

        if (lemmas.isEmpty()) {
            return null;
        }

        new Results(lemmas, siteEntity, this).getResults();

//        Transaction transaction = connection.getSession().beginTransaction();
//        String sql = buildQuery(query, siteEntity, offset, limit);

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

    private @Nullable Site getSite(@NotNull String site) throws DomainNotInListException {
        if (site.isEmpty()) {
            return null;
        }

        String domain = SiteList.getDomain(site, applicationProps);
        return siteRepository.findByUrl(domain);
    }

//    private String buildQuery(String query, Site siteEntity, int offset, int limit) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("SELECT ")
//    }
}
