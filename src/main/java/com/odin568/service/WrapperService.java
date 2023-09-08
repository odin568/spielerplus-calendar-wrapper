package com.odin568.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


@Service
public class WrapperService
{
    private final Logger logger = LoggerFactory.getLogger(WrapperService.class);
    private final ConcurrentMap<String, ConcurrentMap<Integer, String>> cache = new ConcurrentHashMap<>();

    public Optional<String> GetIcs(String type, int id)
    {
        var typeCache = cache.get(type);
        if (typeCache == null)
            return Optional.empty();
        var cachedValue = typeCache.get(id);
        if (cachedValue == null)
            return Optional.empty();

        return Optional.of(cachedValue);
    }

    @Scheduled(fixedRate = 60 * 60, timeUnit = TimeUnit.SECONDS, initialDelay = 10)
    private void refreshCache()
    {
        logger.info("Starting updating cache");
        int id = 0;
        while(true)
        {
            boolean teamSuccess = requestAndCacheIcs("team", id);

            if (teamSuccess)
            {
                requestAndCacheIcs("personal", id);
                id++;
            }
            else
            {
                logger.info("Finished updating cache");
                break;
            }
        }
    }

    private boolean requestAndCacheIcs(String type, int id)
    {
        try {
            String url = "http://spielerplus-calendar/" + type + "/" + id + "/";
            logger.info("Requesting " + url);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 299)
                return false;

            String body = response.body();
            if (body == null || !body.startsWith("BEGIN:VCALENDAR"))
                throw new IllegalStateException("Body is no ics calendar: " + body);

            logger.info("Finished");

            cacheContent(type, id, body);

            return true;
        }
        catch (Exception ex) {
            logger.error("Error getting calendar for type " + type + " and id " + id, ex);
            return false;
        }
    }

    private void cacheContent(String type, int id, String content)
    {
        if (!cache.containsKey(type))
            cache.put(type, new ConcurrentHashMap<>());

        cache.get(type).put(id, content);
    }


}
