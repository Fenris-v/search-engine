package parser;

import entities.Page;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class RecursiveParser extends RecursiveTask<Object> {
    private final String domain;
    private final String rootPath;
    private Response response;
    private final Session session;
    private final String path;
    private final CriteriaQuery<Page> query;

    public RecursiveParser(@NotNull String domain, @NotNull String rootPath, Session session) {
        this.domain = domain;
        this.rootPath = rootPath;
        this.session = session;

        path = rootPath.replaceAll(domain, "");
        query = getPageQuery();
    }

    @Override
    protected Page compute() {
        try {
            synchronized (session) {
                if (!pageNotExists()) {
                    return null;
                }
            }

            addPage();
            setQueue();
        } catch (NoResultException | IOException exception) {
            System.out.println(exception.getMessage());
        }

        return null;
    }

    private @NotNull CriteriaQuery<Page> getPageQuery() {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Page> query = criteriaBuilder.createQuery(Page.class);
        Root<Page> root = query.from(Page.class);
        query.select(root).where(criteriaBuilder.equal(root.get("path"), path));
        return query;
    }

    private boolean pageNotExists() {
        try {
            session.createQuery(query).getSingleResult();

            return false;
        } catch (NoResultException exception) {
            return true;
        }
    }

    private void addPage() throws IOException {
        Page newPage = getNewPage();

        synchronized (session) {
            if (pageNotExists()) {
                session.saveOrUpdate(newPage);
            }
        }
    }

    private @Nullable Page getNewPage() throws IOException {
        response = Jsoup.connect(rootPath)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(60000)
                .execute();

        if (response.statusCode() < 500) {
            return new Page(path, response.statusCode(), response.body());
        }

        return null;
    }

    private void setQueue() throws IOException {
        HashSet<String> links = (HashSet<String>) getUrlList();
        List<RecursiveParser> parserList = new ArrayList<>();
        links.forEach(link -> parserList.add((RecursiveParser) new RecursiveParser(domain, link, session).fork()));
        parserList.forEach(ForkJoinTask::join);
    }

    private Set<String> getUrlList() throws IOException {
        Elements links = response.parse().select("a");
        LinkCleaner linkCleaner = new LinkCleaner(domain);
        return linkCleaner.clearLinks(links);
    }
}
