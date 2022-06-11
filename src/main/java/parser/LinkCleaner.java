package parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkCleaner {
    private static String regex;

    static Set<String> clearLinks(@NotNull Elements links, String parentUrl, String domain, HashSet<String> urlSet) {
        regex = "^(".concat(domain).concat(").*");

        for (Element url : links) {
            addLinkToSet(url, parentUrl, urlSet);
        }

        return urlSet;
    }

    private static void addLinkToSet(@NotNull Element url, String parentUrl, HashSet<String> urlSet) {

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

    private static String removeGetParamsAndAnchors(String link) {
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

    private static boolean isMyselfLink(@NotNull String link, String parentUrl) {
        return link.equals(parentUrl);
    }

    private static boolean isNotChildLinks(@NotNull String link) {
        return !link.matches(regex);
    }

    private static boolean isFileLink(@NotNull String link) {
        return !link.matches("^.*\\.(html|php)$") && link.matches(".*\\.[\\da-z]{1,5}$");
    }
}
