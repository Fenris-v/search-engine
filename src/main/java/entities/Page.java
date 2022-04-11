package entities;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Page {
    private int id;

    @NotNull
    private String path;

    @NotNull
    private int code;

    @NotNull
    private String content;

    public Page(String path, int code, String content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
