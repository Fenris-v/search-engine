package main.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "pageId", nullable = false)
    private Page page;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "lemmaId", nullable = false)
    private Lemma lemma;

    @Column(nullable = false)
    private float rank;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
