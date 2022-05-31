package entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lemma implements Comparable<Lemma> {
    private int id;

    private String lemma;

    private int frequency;

    private int siteId;

    public Lemma(String lemma, int frequency, int siteId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteId = siteId;
    }

    @Override
    public int compareTo(@NotNull Lemma lemma) {
        return Integer.compare(frequency, lemma.getFrequency());
    }
}
