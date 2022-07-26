package main.services.parser;

import lombok.Getter;
import main.entities.Field;
import main.entities.Page;
import main.entities.Site;
import main.entities.Word;
import main.exceptions.DomainNotInListException;
import main.pojo.ApplicationProps;
import main.repositories.FieldRepository;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import main.services.HTMLCleaner;
import main.services.SiteList;
import main.services.morphology.Morphology;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageParser {
    @Getter
    private final ApplicationProps applicationProps;

    private final Morphology morphology = new Morphology();
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final Iterable<Field> fields;
    private Site site;
    private String path;
    private Document document;
    private final main.db.Connection connection;
    private final Map<String, Float> wordsWeight = new HashMap<>();
    private final Map<String, Integer> words = new HashMap<>();

    public PageParser(@NotNull ApplicationProps applicationProps, SiteRepository siteRepository,
                      PageRepository pageRepository, @NotNull FieldRepository fieldRepository) {
        this.applicationProps = applicationProps;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;

        fields = fieldRepository.findAll();
        connection = new main.db.Connection();
    }

    public void parse(String url) throws DomainNotInListException, IOException {
        url = url.trim();

        String domain = SiteList.getDomain(url, applicationProps);
        path = url.replace(domain, "");
        site = siteRepository.findByUrl(domain);

        Connection.Response response = getResponse(url);
        Page page = savePage(response);

        index(page);
    }

    private @NotNull Connection.Response getResponse(String path) throws IOException {
        return Jsoup.connect(path)
                .userAgent(applicationProps.getUserAgent())
                .referrer(applicationProps.getReferrer())
                .timeout(5_000)
                .execute();
    }

    private @NotNull Page savePage(@NotNull Connection.Response response) {
        Page page = new Page();
        page.setPath(path);
        page.setCode(response.statusCode());
        page.setContent(response.body());
        page.setSite(site);

        return pageRepository.save(page);
    }

    private void index(@NotNull Page page) {
        if (page.getCode() != 200) {
            return;
        }

        fields.forEach(field -> saveWords(page, field));
        saveIndexes(page);
    }

    private void saveWords(@NotNull Page page, @NotNull Field field) {
        Element element = Jsoup.parse(page.getContent()).selectFirst(field.getSelector());
        if (element == null) {
            return;
        }

        HTMLCleaner.excludeJunkElements(element);
        morphology.countWords(element.text()).forEach(this::addWord);

        String lemmasSql = buildLemmasQuery(words);
        Transaction transaction = connection.getSession().beginTransaction();
        connection.getSession().createNativeQuery(lemmasSql).executeUpdate();
        connection.getSession().flush();
        transaction.commit();
    }

    private @NotNull String buildLemmasQuery(@NotNull Map<String, Integer> words) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO lemma (lemma, frequency, site_id) VALUES ");
        words.forEach((word, count) -> stringBuilder.append("('")
                .append(word)
                .append("',")
                .append(count)
                .append(",")
                .append(site.getId())
                .append("),"));

        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(" ON CONFLICT (lemma, site_id) DO UPDATE SET frequency = lemma.frequency + EXCLUDED.frequency");

        return stringBuilder.toString();
    }

    private void addWord(String word, Integer count) {
        words.put(word, words.getOrDefault(word, 0) + count);
    }

    private void saveIndexes(@NotNull Page page) {
        document = Jsoup.parse(page.getContent());
        fields.forEach(this::calculateWordsWeight);

        save(page);
    }

    private void calculateWordsWeight(@NotNull Field field) {
        Element element = document.selectFirst(field.getSelector());
        String str = element == null ? "" : element.text();
        morphology.countWords(str).forEach((word, count) -> calculateWordWeight(field, word, count));
    }

    private void calculateWordWeight(@NotNull Field field, String word, Integer count) {
        float weight = count * field.getWeight();
        if (wordsWeight.containsKey(word)) {
            weight += wordsWeight.get(word);
        }

        wordsWeight.put(word, weight);
    }

    private void save(Page page) {
        if (wordsWeight.isEmpty()) {
            return;
        }

        Map<String, Long> lemmas = getLemmas();

        String indexesSql = buildIndexesQuery(page, lemmas);
        connection.getSession().beginTransaction();
        connection.getSession().createNativeQuery(indexesSql).executeUpdate();
        connection.getSession().flush();
        connection.close();
    }

    private @NotNull Map<String, Long> getLemmas() {
        List<Word> list = connection.getSession().createNativeQuery(getLemmaSelectSql(), Word.class).getResultList();

        Map<String, Long> lemmas = new HashMap<>();
        list.forEach(word -> lemmas.put(word.getLemma(), word.getId()));

        return lemmas;
    }

    @NotNull
    private String getLemmaSelectSql() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT id, lemma FROM lemma WHERE lemma.lemma IN (");

        wordsWeight.forEach((word, weight) -> builder.append("'").append(word).append("',"));

        builder.setLength(builder.length() - 1);
        builder.append(") AND lemma.site_id = ").append(site.getId());
        return builder.toString();
    }

    private @NotNull String buildIndexesQuery(Page page, Map<String, Long> lemmas) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO index (page_id, lemma_id, rank) VALUES ");

        wordsWeight.forEach((word, weight) -> addIndexToSql(page, lemmas, stringBuilder, word, weight));

        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private void addIndexToSql(Page page, @NotNull Map<String, Long> lemmas, StringBuilder stringBuilder, String word, Float weight) {
        if (!lemmas.containsKey(word)) {
            return;
        }

        stringBuilder.append("(")
                .append(page.getId())
                .append(",")
                .append(lemmas.get(word))
                .append(",")
                .append(weight)
                .append("),");
    }
}
