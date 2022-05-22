package contracts;

import enums.ExcludingDocumentElements;
import org.jsoup.nodes.Element;

public interface ExcludeElements {
    default void excludeJunkElements(Element element) {
        for (ExcludingDocumentElements value : ExcludingDocumentElements.values()) {
            Element excludeElement = element.selectFirst(value.getValue());
            if (excludeElement != null) {
                element.after(excludeElement);
            }
        }
    }
}
