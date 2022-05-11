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

    @Setter
    private float relevance;

    public Result(Map<Integer, Float> lemmas, float absRank, @NotNull Page page) {
        this.lemmas = lemmas;
        this.absRank = absRank;
        uri = page.getPath();
        Document document = Jsoup.parse(page.getContent());
        Element elementTitle = document.select("title").first();
        title = elementTitle == null ? "" : elementTitle.text();
        snippet = "";
    }

    @Override
    public int compareTo(@NotNull Result result) {
        return Float.compare(result.getAbsRank(), absRank);
    }
}
