import models.Result;
import searching.Search;

import java.util.Set;

public class SearchTest {
    public static void main(String[] args) {
        Set<Result> results = new Search("купить телефон недорогой").execute();

        results.forEach(result -> {
            System.out.println(result.getUri());
            System.out.println(result.getTitle());
            System.out.println(result.getSnippet());
            System.out.println(result.getRelevance());
            System.out.println();
        });
    }
}
