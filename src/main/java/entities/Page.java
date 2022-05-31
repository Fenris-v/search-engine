package entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    private int id;

    private String path;

    private int code;

    private String content;

    private int siteId;

    public Page(String path, int code, String content, int siteId) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.siteId = siteId;
    }
}
