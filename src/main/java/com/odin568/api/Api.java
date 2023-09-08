package com.odin568.api;

import com.odin568.service.WrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
public class Api {

    @Autowired
    private WrapperService wrapperService;

    @GetMapping(value = {"/api/team/{id}/", "/api/team/{id}"}, produces = "text/calendar")
    public ResponseEntity<Resource> team(@PathVariable("id") int id)
    {
        return handleRequest("team", id);
    }

    @GetMapping(value = {"/api/personal/{id}/", "/api/personal/{id}"}, produces = "text/calendar")
    public ResponseEntity<Resource> personal(@PathVariable("id") int id)
    {
        return handleRequest("personal", id);
    }

    private ResponseEntity<Resource> handleRequest(String type, int id)
    {
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".ics");
        header.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        header.add(HttpHeaders.PRAGMA, "no-cache");
        header.add(HttpHeaders.EXPIRES, "0");

        Optional<String> ics = wrapperService.GetIcs(type, id);
        if (ics.isEmpty())
            return ResponseEntity.badRequest().build();

        byte[] icsAsBytes = ics.get().getBytes(StandardCharsets.UTF_8);

        ByteArrayResource resource = new ByteArrayResource(icsAsBytes);

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(icsAsBytes.length)
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(resource);
    }


}
