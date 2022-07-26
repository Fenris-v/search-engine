package main.services;

import main.exceptions.DomainNotInListException;
import main.pojo.ApplicationProps;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SiteList {
    public static String getDomain(String url, ApplicationProps applicationProps) throws DomainNotInListException {
        List<String> domainList = getDomainList(applicationProps);
        for (String domain : domainList) {
            if (url.indexOf(domain) != 0) {
                continue;
            }

            return domain;
        }

        throw new DomainNotInListException();
    }

    private static @NotNull List<String> getDomainList(@NotNull ApplicationProps applicationProps) {
        List<Map<String, String>> sites = applicationProps.getSites();
        List<String> domainList = new ArrayList<>();

        sites.forEach(site -> domainList.add(site.get("url")));
        return domainList;
    }
}
