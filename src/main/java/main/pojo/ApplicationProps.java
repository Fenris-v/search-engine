package main.pojo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "parser")
public class ApplicationProps {
    private List<Map<String, String>> sites;

    private String referrer;

    private String userAgent;
}
