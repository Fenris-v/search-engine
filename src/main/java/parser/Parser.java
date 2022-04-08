package parser;

import db.Connection;
import org.hibernate.Session;

import java.util.concurrent.ForkJoinPool;

public class Parser {
    private final Connection connection = new Connection();
    private final String domain = "http://www.playback.ru";

    public void parseSite() {
        try (Session session = connection.getSession()) {
            new ForkJoinPool().invoke(new RecursiveParser(domain, domain.concat("/"), session));
        }
    }
}
