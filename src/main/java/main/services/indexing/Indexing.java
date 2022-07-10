package main.services.indexing;

import lombok.Getter;
import main.entities.Field;
import main.entities.Page;
import main.entities.Site;
import main.repositories.PageRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class Indexing {
    @Getter
    private final Iterable<Field> fields;

    private final Set<Page> pages = new HashSet<>();

    private static final String getAllPagesSql = "SELECT * FROM pages";

    @Getter
    private final PageRepository pageRepository;

    public Indexing(Iterable<Field> fields, PageRepository pageRepository) {
        this.fields = fields;
        this.pageRepository = pageRepository;
    }

    Set<Page> getPages() {
        return pages;
    }

    public void execute(Site site) {
        new LemmasCounter(this, site).execute();
//        new IndexesCounter(this).execute();
    }

    private void setPages(@NotNull Statement statement) throws SQLException {
        ResultSet result = statement.executeQuery(getAllPagesSql);
        while (result.next()) {
            // todo
//            pages.add(new Page(
//                            result.getInt("id"),
//                            result.getString("path"),
//                            result.getInt("code"),
//                            result.getString("content"),
//                            1
//                    )
//            );
        }
    }
}
