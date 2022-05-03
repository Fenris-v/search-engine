package entities;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Index {
    private int id;

    @NotNull
    private int page_id;

    @NotNull
    private int lemma_id;

    @NotNull
    private float rank;

    public Index(int page_id, int lemma_id, float rank) {
        this.page_id = page_id;
        this.lemma_id = lemma_id;
        this.rank = rank;
    }
}
