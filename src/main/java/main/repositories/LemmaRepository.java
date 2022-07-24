package main.repositories;

import main.entities.Lemma;
import org.springframework.data.repository.CrudRepository;

public interface LemmaRepository extends CrudRepository<Lemma, Long> {
}
