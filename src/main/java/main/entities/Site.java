package main.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Index;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "urlIndex", columnList = "url")
})
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String status;

    private LocalDateTime statusTime;

    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany
    @JoinColumn(name = "site_id")
    private List<Page> pages;

    @OneToMany
    @JoinColumn(name = "site_id")
    private List<Lemma> lemmas;
}
