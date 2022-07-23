package main.repositories;

import main.entities.Site;
import main.pojo.Statistic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends CrudRepository<Site, Long> {
    @Query("SELECT new main.pojo.Statistic(s, COUNT(p.id), COUNT(l.id)) FROM Site s " +
            "LEFT JOIN Page p ON p.site = s " +
            "LEFT JOIN Lemma l ON l.site = s GROUP BY s")
    Iterable<Statistic> getStatistic();

    Site findByUrl(String url);
}
