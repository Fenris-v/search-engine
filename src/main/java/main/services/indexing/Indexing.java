package main.services.indexing;

import lombok.Getter;
import main.entities.Field;
import main.entities.Page;
import main.entities.Site;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;

import java.util.HashSet;
import java.util.Set;

public class Indexing {
    @Getter
    private final Iterable<Field> fields;

    private final Set<Page> pages = new HashSet<>();

    // private static final String getAllPagesSql = "SELECT * FROM pages";

    @Getter
    private final PageRepository pageRepository;

    @Getter
    private final LemmaRepository lemmaRepository;

    public Indexing(Iterable<Field> fields, PageRepository pageRepository, LemmaRepository lemmaRepository) {
        this.fields = fields;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
    }

    Set<Page> getPages() {
        return pages;
    }

    public void execute(Site site) {
        new LemmasCounter(this, site).execute();
//        new IndexesCounter(this).execute();
    }
}
