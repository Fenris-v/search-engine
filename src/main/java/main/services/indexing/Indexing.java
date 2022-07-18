package main.services.indexing;

import lombok.Getter;
import lombok.Setter;
import main.db.Connection;
import main.entities.Field;
import main.entities.Page;
import main.entities.Site;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;

public class Indexing {
    private static final int LIMIT = 1000;

    @Getter
    private final Iterable<Field> fields;

    @Getter
    private List<Page> pages;

    @Getter
    private final Session session;

    @Getter
    @Setter
    private Site site;

    private int pageCount = 0;
    private LemmasCounter lemmasCounter;
    private IndexesCounter indexesCounter;

    private static final String pageCountSql = "SELECT * FROM page WHERE site_id = ? LIMIT ? OFFSET ?";

    public Indexing(Iterable<Field> fields) {
        this.fields = fields;
        session = Connection.getSession();
    }

    public void execute(@NotNull Site site) {
        setSite(site);

        lemmasCounter = new LemmasCounter(this);
        indexesCounter = new IndexesCounter(this);

        session.beginTransaction();
        countPages();
        index();
        session.flush();
        session.close();
    }

    private void countPages() {
        int totalCount = getTotalCount();
        pageCount = (int) Math.ceil((double) totalCount / LIMIT);
    }

    private int getTotalCount() {
        String sql = "SELECT COUNT(id) FROM page WHERE site_id = ?";

        return ((BigInteger) session.createNativeQuery(sql)
                .setParameter(1, site.getId())
                .getSingleResult())
                .intValue();
    }

    private void index() {
        for (int i = 0; i < pageCount; i++) {
            int offset = i * LIMIT;
            setPages(offset);

            lemmasCounter.execute();
            indexesCounter.execute();
        }
    }

    private void setPages(int offset) {
        pages = session.createNativeQuery(pageCountSql, Page.class)
                .setParameter(1, site.getId())
                .setParameter(2, LIMIT)
                .setParameter(3, offset)
                .list();
    }
}
