package parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// TODO: проверить работу пагинации
public class LinkCleaner {
    private final String domain;
    private Stream<String> stream;

    public LinkCleaner(String domain) {
        this.domain = domain;
    }

    List<String> clearLinks(@NotNull Elements links) {
        stream = links.stream().map(url -> url.absUrl("href"));
        removeGetParamsAndAnchors();
        removeToMyselfLink();
        removeNotChildLinks();
        removeFilesLinks();
        return stream.toList();
    }

    private void removeGetParamsAndAnchors() {
        List<Character> chars = Arrays.asList('?', '#');
        for (char c : chars) {
            stream = stream.map(url -> substringByChar(c, url));
        }
    }

    private @NotNull
    String substringByChar(char c, @NotNull String url) {
        int index = url.indexOf(c);
        if (index > 0) {
            return url.substring(0, index);
        }

        return url;
    }

    private void removeToMyselfLink() {
        stream = stream.filter(url -> !url.equals(domain));
    }

    private void removeNotChildLinks() {
        stream = stream.filter(url -> url.contains(domain));
    }

    private void removeFilesLinks() {
        stream = stream.filter(url -> {
            if (url.matches("^.*\\.(html|php)$")) {
                return true;
            }

            return !url.matches(".*\\.[0-9a-z]{1,5}$");
        });
    }
}
