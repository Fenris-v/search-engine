package main.services.indexing;

import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

abstract class AbstractCounter {
    protected final Indexing indexing;
    protected final StringBuilder stringBuilder = new StringBuilder();
    protected final long siteId;

    protected AbstractCounter(@NotNull Indexing indexing) {
        this.indexing = indexing;
        this.siteId = indexing.getSite().getId();
    }

    protected void executeQuery() {
        getSession().createNativeQuery(stringBuilder.toString()).executeUpdate();

        getSession().flush();
        getSession().clear();
        indexing.getTransaction().commit();
        indexing.setTransaction(getSession().beginTransaction());
    }

    protected Session getSession() {
        return indexing.getConnection().getSession();
    }
}
