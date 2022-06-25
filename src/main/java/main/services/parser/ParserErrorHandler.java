package main.services.parser;

import main.entities.Page;
import org.jetbrains.annotations.NotNull;
import org.jsoup.HttpStatusException;

public class ParserErrorHandler {
    static void saveNotOkResponse(@NotNull HttpStatusException e, @NotNull Parser parser, String relativeUrl) {
        Page page = new Page();
        page.setPath(getPath(parser.getSite().getUrl(), relativeUrl));
        page.setCode(e.getStatusCode());
        page.setContent(null);
        page.setSite(parser.getSite());

        parser.getPageMap().put(relativeUrl, page);
        parser.saveParseError(e.getMessage());
    }

    private static @NotNull String getPath(@NotNull String domain, @NotNull String url) {
        String path = url.replaceAll(domain, "");
        path = path.isEmpty() ? "/" : path;
        return path;
    }
}
