package morphology;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Morphology {
    private final LuceneMorphology luceneMorphology;
    private Map<String, Integer> words;

    public Morphology() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> countWords(String text) {
        Pattern pattern = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        words = new HashMap<>();
        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            word = luceneMorphology.getMorphInfo(word).get(0);

            countWord(word);
        }

        return words;
    }

    private void countWord(@NotNull String word) {
        if (word.matches(".*(ПРЕДЛ|ЧАСТ|МЕЖД|СОЮЗ).*")) {
            return;
        }

        word = word.substring(0, word.indexOf('|'));
        if (words.containsKey(word)) {
            words.put(word, words.get(word) + 1);
            return;
        }

        words.put(word, 1);
    }
}
