package entities;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lemma implements Comparable<Lemma> {
    private int id;

    @NotNull
    private String lemma;

    @NotNull
    private int frequency;

    public Lemma(String lemma, int frequency) {
        this.lemma = lemma;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(@NotNull @org.jetbrains.annotations.NotNull Lemma lemma) {
        return Integer.compare(frequency, lemma.getFrequency());
    }
}
