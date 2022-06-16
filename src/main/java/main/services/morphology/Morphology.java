package main.services.morphology;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Morphology {
    private final LuceneMorphology luceneMorphology;
    private Map<String, Integer> words;

    private final Pattern PATTERN = Pattern
            .compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);

    public Morphology() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSnippet(String text, String neededLemma) {
        Matcher matcher = PATTERN.matcher(text);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            String normalForm = tryGetNormalForm(word);
            if (normalForm == null) {
                continue;
            }

            if (normalForm.equals(neededLemma)) {
                return makeSnippet(text, word);
            }
        }

        return "";
    }

    public Map<String, Integer> countWords(String text) {
        Matcher matcher = PATTERN.matcher(text);

        words = new HashMap<>();
        while (matcher.find()) {
            try {
                String word = matcher.group().toLowerCase();
                word = luceneMorphology.getMorphInfo(word).get(0);

                countWord(word);
            } catch (Exception exception) {
//                System.out.println(exception.getMessage());
            }
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

    private @Nullable String tryGetNormalForm(String word) {
        try {
            return getNormalForm(word);
        } catch (WrongCharaterException e) {
            return null;
        }
    }

    private @Nullable String getNormalForm(String word) throws WrongCharaterException {
        String normalForm = luceneMorphology.getMorphInfo(word).get(0);

        if (normalForm.matches(".*(ПРЕДЛ|ЧАСТ|МЕЖД|СОЮЗ).*")) {
            return null;
        }

        return normalForm.substring(0, normalForm.indexOf('|'));
    }

    private @NotNull String makeSnippet(@NotNull String text, String word) {
        int wordIndex = text.indexOf(word);
        int sentenceEndIndex = text.indexOf('.', wordIndex) + 1;
        String snippet = text.substring(0, sentenceEndIndex);

        int sentenceStartIndex = snippet.lastIndexOf(". ") + 2;
        snippet = snippet.substring(sentenceStartIndex);

        return cutSnippet(snippet);
    }

    private @NotNull String cutSnippet(@NotNull String snippet) {
//        if (snippet.length() > Search.SNIPPET_LENGTH) {
//            snippet = snippet.substring(0, Search.SNIPPET_LENGTH - 3).concat("...");
//        }

        return snippet;
    }
}
