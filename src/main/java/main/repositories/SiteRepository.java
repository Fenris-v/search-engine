package main.repositories;

import main.entities.Site;
import main.pojo.Statistic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends CrudRepository<Site, Long> {
    @Query("SELECT new main.pojo.Statistic(s, COUNT(p)) FROM Site s LEFT JOIN Page.id p")
    Iterable<Statistic> getStatistic();
}
