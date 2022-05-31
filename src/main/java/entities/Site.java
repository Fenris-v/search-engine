package entities;

import enums.SiteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Site {
    private int id;

    private SiteStatus status;

    private LocalDateTime status_time;

    private String last_error;

    private String url;

    private String name;
}
