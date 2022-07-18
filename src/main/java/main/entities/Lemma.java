package main.entities;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Index;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "lemmaIndex", columnList = "lemma")
})
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Override
    public int compareTo(@NotNull Lemma lemma) {
        return Integer.compare(frequency, lemma.getFrequency());
    }
}
