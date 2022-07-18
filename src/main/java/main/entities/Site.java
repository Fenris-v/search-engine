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

    @Column(name = "status_time")
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany
    @JoinColumn(name = "site_id")
    private List<Page> pages;

    @OneToMany
    @JoinColumn(name = "site_id")
    private List<Lemma> lemmas;
}
