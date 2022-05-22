package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@AllArgsConstructor
public class Result implements Comparable<Result> {
    private Map<Integer, Float> lemmas;
    private float absRank;

    private String uri;
    private String title;
    private String snippet;

    public final static int SNIPPET_LENGTH = 250;

    @Setter
    private float relevance;

    public Result(Map<Integer, Float> lemmas, float absRank, String uri, String title, String snippet) {
        this.lemmas = lemmas;
        this.absRank = absRank;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public int compareTo(@NotNull Result result) {
        return Float.compare(result.getAbsRank(), absRank);
    }
}
