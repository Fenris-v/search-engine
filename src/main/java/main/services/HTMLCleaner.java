package main.services;

import main.enums.ExcludingDocumentElements;
import org.jsoup.nodes.Element;

public class HTMLCleaner {
    public static void excludeJunkElements(Element element) {
        for (ExcludingDocumentElements value : ExcludingDocumentElements.values()) {
            Element excludeElement = element.selectFirst(value.getValue());
            if (excludeElement != null) {
                element.after(excludeElement);
            }
        }
    }
}
