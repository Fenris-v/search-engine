import db.Tables;
import indexing.Indexing;
import parser.Parser;

public class Main {
    private static final Parser parser = new Parser();

    public static void main(String[] args) {
        // parser.parseSite();

        Tables.createTables();

        new Indexing().execute();
//        Connection connection = new DbConnection().getConnection();
//        assert connection != null;
//
//        try (Statement statement = connection.createStatement()) {
//            Map<String, Field> fields = new HashMap<>();
//            ResultSet result = statement.executeQuery("SELECT * FROM fields");
//
//            while (result.next()) {
//                Field field = new Field(
//                        result.getString("name"),
//                        result.getString("selector"),
//                        result.getFloat("weight")
//                );
//
//                fields.put(field.getName(), field);
//            }
//
//            ResultSet resultPages = statement.executeQuery("SELECT * FROM pages");
//            Morphology morphology = new Morphology();
//            while (resultPages.next()) {
//                if (resultPages.getInt("code") != 200) {
//                    continue;
//                }
//
//                int pageId = resultPages.getInt("id");
//
//                Document document = Jsoup.parse(resultPages.getString("content"));
//
//                Element titleElement = document.select(fields.get("title").getSelector()).first();
//                String title = titleElement == null ? "" : titleElement.text();
//
//                Element bodyElement = document.select(fields.get("body").getSelector()).first();
//                String body = bodyElement == null ? "" : bodyElement.text();
//
//                Map<String, Integer> titleWords = morphology.countWords(title);
//                Map<String, Integer> bodyWords = morphology.countWords(body);
//
//                Map<String, Integer> words = new HashMap<>(bodyWords);
//                titleWords.forEach((word, count) -> {
//                    int wordCount = 0;
//                    if (words.containsKey(word)) {
//                        wordCount = words.get(word);
//                    }
//
//                    words.put(word, wordCount + 1);
//                });
//
//                String sql = "INSERT INTO lemmas (lemma, frequency) VALUES (?, ?) ON CONFLICT (lemma) DO UPDATE SET frequency = lemmas.frequency + 1";
//                PreparedStatement preparedStatement = connection.prepareStatement(sql);
//                words.forEach((word, count) -> {
//                    try {
//                        preparedStatement.setString(1, word);
//                        preparedStatement.setInt(2, 1);
//
//                        preparedStatement.addBatch();
//                    } catch (SQLException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//
//                preparedStatement.executeBatch();
//                preparedStatement.close();
//
//                Map<String, Float> wordsWeight = new HashMap<>();
//                titleWords.forEach((word, count) -> {
//                    wordsWeight.put(word, count * fields.get("title").getWeight());
//                });
//
//                bodyWords.forEach((word, count) -> {
//                    if (wordsWeight.containsKey(word)) {
//                        wordsWeight.put(word, wordsWeight.get(word) + (count * fields.get("body").getWeight()));
//                        return;
//                    }
//
//                    wordsWeight.put(word, count * fields.get("body").getWeight());
//                });
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }
}
