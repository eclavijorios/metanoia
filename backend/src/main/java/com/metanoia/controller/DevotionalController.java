package com.metanoia.controller;

import com.metanoia.model.Devotional;
import com.metanoia.model.DevotionalVerse;
import com.metanoia.service.DevotionalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/devotionals")
public class DevotionalController {

    private final DevotionalService devotionalService;

    public DevotionalController(DevotionalService devotionalService) {
        this.devotionalService = devotionalService;
    }

    @GetMapping("/today")
    public Devotional getToday() {
        return devotionalService.getToday();
    }

    @GetMapping
    public List<Devotional> getAll(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            Devotional d = devotionalService.getByDate(date);
            return d != null ? List.of(d) : List.of();
        }
        return devotionalService.getAll();
    }

    @PostMapping
    public Devotional create(@RequestBody(required = false) Map<String, String> body) {
        LocalDate date = body != null && body.containsKey("date")
            ? LocalDate.parse(body.get("date"))
            : null;
        return devotionalService.create(date);
    }

    @PutMapping("/{id}")
    public Devotional update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return devotionalService.update(id, body.get("title"), body.get("content"));
    }

    @PostMapping("/{id}/verses")
    public Devotional addVerse(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Long verseId = Long.valueOf(body.get("verseId").toString());
        Integer bibleVersionId = Integer.valueOf(body.get("bibleVersionId").toString());
        String referenceText = (String) body.get("referenceText");
        return devotionalService.addVerse(id, verseId, bibleVersionId, referenceText);
    }

    @DeleteMapping("/{id}/verses/{vid}")
    public ResponseEntity<Void> removeVerse(@PathVariable UUID id, @PathVariable Long vid) {
        devotionalService.removeVerse(id, vid);
        return ResponseEntity.noContent().build();
    }
}
