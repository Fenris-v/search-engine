package main.repositories;

import main.entities.Page;
import main.entities.Site;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {
    //    @Query("SELECT Page FROM Page p WHERE p.site = :site GROUP BY p")
    org.springframework.data.domain.Page<Page> getPagesBySite(@Param("site") Site site, Pageable pageable);
}
