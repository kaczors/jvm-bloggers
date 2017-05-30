package com.jvm_bloggers.core.rss.json;

import com.github.openjson.JSONObject;
import com.jvm_bloggers.core.rss.BlogPostsController;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import javaslang.control.Option;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.jvm_bloggers.core.rss.json.RssJsonKey.AUTHOR;
import static com.jvm_bloggers.core.rss.json.RssJsonKey.DATE;
import static com.jvm_bloggers.core.rss.json.RssJsonKey.DESCRIPTION;
import static com.jvm_bloggers.core.rss.json.RssJsonKey.ENTRIES;
import static com.jvm_bloggers.core.rss.json.RssJsonKey.GENERATOR;
import static com.jvm_bloggers.core.rss.json.RssJsonKey.LINK;
import static com.jvm_bloggers.core.rss.json.RssJsonKey.TITLE;
import static com.jvm_bloggers.utils.DateTimeUtilities.DATE_TIME_FORMATTER;
import static com.jvm_bloggers.utils.NowProvider.DEFAULT_ZONE;

@Component
@Slf4j
public class SyndFeedToJsonConverter {

    private final String baseUrl;

    @Autowired
    public SyndFeedToJsonConverter(@Value("${application.baseUrl}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private static JSONObject toJson(SyndFeed feed) {
        JSONObject json = new JSONObject();
        json.put(TITLE, feed.getTitle());
        json.put(LINK, feed.getLink());
        json.put(DESCRIPTION, feed.getDescription());
        json.put(ENTRIES, toJson(feed.getEntries()));
        return json;
    }

    private static List<JSONObject> toJson(List<SyndEntry> entries) {
        return entries.stream()
            .map(SyndFeedToJsonConverter::toJson)
            .collect(Collectors.toList());
    }

    private static JSONObject toJson(SyndEntry entry) {
        JSONObject json = new JSONObject();

        json.put(LINK, entry.getLink());
        json.put(TITLE, entry.getTitle());
        json.put(AUTHOR, entry.getAuthor());

        Option.of(entry.getDescription())
            .peek(d -> json.put(DESCRIPTION, d.getValue()));

        String date = DATE_TIME_FORMATTER.format(
            LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), DEFAULT_ZONE));

        json.put(DATE, date);
        return json;
    }

    public JSONObject convert(SyndFeed feed) {
        log.debug("Building JSON from the RSS feed...");

        JSONObject json = toJson(feed);
        json.put(GENERATOR, baseUrl);
        json.put(LINK, baseUrl + BlogPostsController.RSS_FEED_MAPPING);

        log.debug("JSON content generated successfully with '{}' entries",
            feed.getEntries().size());
        return json;
    }
}
