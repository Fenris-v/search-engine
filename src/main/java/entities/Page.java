package entities;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "pages",
        indexes = {
                @Index(name = "path_index", columnList = "path", unique = true)
        })
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String path;

    @NotNull
    private int code;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    public Page(String path, int code, String content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
