package main.repositories;

import main.entities.Page;
import main.entities.Site;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Iterator;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {
    //    @Query("SELECT Page FROM Page p WHERE p.site = :site GROUP BY p")
    org.springframework.data.domain.Page<Page> getPagesBySite(Pageable pageable, @Param("site") Site site);

    Iterator<Page> findBySite(@Param("site") Site site);

//    @Query(value = "SELECT * FROM page p WHERE site_id=:siteId LIMIT 5", nativeQuery = true)
//    Iterator<Page> getPages(long siteId);
}
