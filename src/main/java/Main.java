import db.Tables;
import indexing.Indexing;
import parser.Parser;

public class Main {
    private static final Parser parser = new Parser();

    public static void main(String[] args) {
        parser.parseSite();

        Tables.createTables();
        new Indexing().execute();
    }
}
