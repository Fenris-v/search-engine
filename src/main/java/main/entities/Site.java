package main.entities;

import lombok.Getter;
import main.enums.SiteStatus;

import javax.persistence.Index;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "urlIndex", columnList = "url")
})
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private SiteStatus status;

    @Column(nullable = false)
    private LocalDateTime statusTime = LocalDateTime.now();

    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
