package main.repositories;

import main.entities.Lemma;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface LemmaRepository extends CrudRepository<Lemma, Long> {
    @Modifying
    @Query(value = "INSERT INTO lemma (lemma, frequency, site_id) VALUES (:lemmas.lemma, :lemmas.frequency, :lemmas.site.id)", nativeQuery = true)
    void saveLemmas(@Param("lemmas") Iterable<Lemma> lemmas);
}
