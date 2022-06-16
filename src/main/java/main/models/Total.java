package main.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Total {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean isIndexing;
}
