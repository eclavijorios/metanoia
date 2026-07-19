package com.metanoia.controller;

import com.metanoia.model.BibleVersion;
import com.metanoia.model.Verse;
import com.metanoia.service.BibleService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bibles")
public class BibleController {

    private final BibleService bibleService;

    public BibleController(BibleService bibleService) {
        this.bibleService = bibleService;
    }

    @GetMapping("/versions")
    public List<BibleVersion> getVersions() {
        return bibleService.getVersions();
    }

    @GetMapping("/passage")
    public List<Verse> getPassage(
            @RequestParam String book,
            @RequestParam int ch,
            @RequestParam(required = false) Integer v,
            @RequestParam(defaultValue = "fbv") String version) {
        return bibleService.getPassage(book, ch, v, version);
    }

    @GetMapping("/search")
    public List<Verse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "fbv") String version) {
        return bibleService.search(q, version);
    }
}
