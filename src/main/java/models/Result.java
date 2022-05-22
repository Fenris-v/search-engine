package models;

import entities.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

    public Result(Map<Integer, Float> lemmas, float absRank, @NotNull Page page, String snippet) {
        this.lemmas = lemmas;
        this.absRank = absRank;
        uri = page.getPath();
        Document document = Jsoup.parse(page.getContent());
        Element elementTitle = document.selectFirst("title");
        title = elementTitle == null ? "NO TITLE" : elementTitle.text();
        this.snippet = snippet;
    }

    @Override
    public int compareTo(@NotNull Result result) {
        return Float.compare(result.getAbsRank(), absRank);
    }
}
