package main.services.indexing;

import lombok.Getter;
import lombok.Setter;
import main.controllers.ApiController;
import main.db.Connection;
import main.entities.Field;
import main.entities.Page;
import main.entities.Site;
import org.hibernate.Transaction;
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
    @Setter
    private Site site;

    private int pageCount = 0;
    private LemmasCounter lemmasCounter;
    private IndexesCounter indexesCounter;

    private long start = System.currentTimeMillis();

    @Getter
    @Setter
    private Transaction transaction;

    @Getter
    private final Connection connection = new Connection();

    private static final String pageCountSql = "SELECT * FROM page WHERE site_id = ? LIMIT ? OFFSET ?";

    public Indexing(Iterable<Field> fields) {
        this.fields = fields;
    }

    public void execute(@NotNull Site site) {
        setSite(site);

        lemmasCounter = new LemmasCounter(this);
        indexesCounter = new IndexesCounter(this);

        transaction = connection.getSession().beginTransaction();
        countPages();
        index();
        connection.close();
    }

    private void countPages() {
        int totalCount = getTotalCount();
        pageCount = (int) Math.ceil((double) totalCount / LIMIT);
    }

    private int getTotalCount() {
        String sql = "SELECT COUNT(id) FROM page WHERE site_id = ?";

        return ((BigInteger) connection.getSession().createNativeQuery(sql).setParameter(1, site.getId()).getSingleResult()).intValue();
    }

    private void index() {
        for (int i = 0; i < pageCount; i++) {
            if (!ApiController.IS_PARSE) {
                return;
            }

            int offset = i * LIMIT;
            pages = getPages(offset);

            lemmasCounter.execute(pages);
            indexesCounter.execute(pages);

            printDuringOfPageIndexing(i);
        }
    }

    private List<Page> getPages(int offset) {
        return connection.getSession().createNativeQuery(pageCountSql, Page.class).setParameter(1, site.getId()).setParameter(2, LIMIT).setParameter(3, offset).list();
    }

    private void printDuringOfPageIndexing(int i) {
        System.out.println((i + 1) + " из " + pageCount + " на сайте " + site.getId());
        System.out.println("Время обработки страницы: " + ((double) System.currentTimeMillis() - start) / 1000 + "s");
        start = System.currentTimeMillis();
    }
}
