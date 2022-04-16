package parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkCleaner {
    private final String domain;
    private Set<String> urlSet;

    public LinkCleaner(String domain) {
        this.domain = domain;
    }

    Set<String> clearLinks(@NotNull Elements links, String parentUrl) {
        urlSet = new HashSet<>();

        for (Element url : links) {
            addLinkToSet(url, parentUrl);
        }

        return urlSet;
    }

    private void addLinkToSet(@NotNull Element url, String parentUrl) {
        String link = url.absUrl("href");
        if (isNotChildLinks(link)) {
            return;
        }

        link = removeGetParamsAndAnchors(link);
        if (isMyselfLink(link, parentUrl) || isFileLink(link)) {
            return;
        }

        urlSet.add(link);
    }

    private String removeGetParamsAndAnchors(String link) {
        List<Character> chars = Arrays.asList('?', '#');
        for (char c : chars) {
            link = substringByChar(c, link);
        }

        return link;
    }

    private static @NotNull String substringByChar(char c, @NotNull String url) {
        int index = url.indexOf(c);
        if (index > 0) {
            return url.substring(0, index);
        }

        return url;
    }

    private boolean isMyselfLink(@NotNull String link, String parentUrl) {
        return link.equals(parentUrl);
    }

    private boolean isNotChildLinks(@NotNull String link) {
        return !link.contains(domain);
    }

    private boolean isFileLink(@NotNull String link) {
        return !link.matches("^.*\\.(html|php)$") && link.matches(".*\\.[\\da-z]{1,5}$");
    }
}
