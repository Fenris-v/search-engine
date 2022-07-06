package main.pojo;

import lombok.Getter;
import main.entities.Site;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Getter
public class Statistic {
    private final long id;
    private final String status;
    private final LocalDateTime statusTime;
    private final String lastError;
    private final String url;
    private final String name;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final long pages;
    private final long lemmas;

    public Statistic(@NotNull Site site, long pages, long lemmas) {
        id = site.getId();
        status = site.getStatus();
        statusTime = site.getStatusTime();
        lastError = site.getLastError();
        url = site.getUrl();
        name = site.getName();
        createdAt = site.getCreatedAt();
        updatedAt = site.getUpdatedAt();
        this.pages = pages;
        this.lemmas = lemmas;
    }
}
