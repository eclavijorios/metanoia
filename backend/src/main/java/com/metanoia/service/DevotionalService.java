package com.metanoia.service;

import com.metanoia.model.Devotional;
import com.metanoia.model.DevotionalVerse;
import com.metanoia.repository.DevotionalRepository;
import com.metanoia.repository.DevotionalVerseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DevotionalService {

    private final DevotionalRepository devotionalRepo;
    private final DevotionalVerseRepository verseRepo;

    public DevotionalService(DevotionalRepository devotionalRepo, DevotionalVerseRepository verseRepo) {
        this.devotionalRepo = devotionalRepo;
        this.verseRepo = verseRepo;
    }

    public Devotional getToday() {
        LocalDate today = LocalDate.now();
        return devotionalRepo.findByDate(today)
            .orElseGet(() -> {
                Devotional d = new Devotional();
                d.setDate(today);
                d.setTitle("");
                d.setContent("");
                return devotionalRepo.save(d);
            });
    }

    public Devotional getByDate(LocalDate date) {
        return devotionalRepo.findByDate(date).orElse(null);
    }

    public List<Devotional> getAll() {
        return devotionalRepo.findAllOrderByDateDesc();
    }

    @Transactional
    public Devotional create(LocalDate date) {
        Devotional d = new Devotional();
        d.setDate(date != null ? date : LocalDate.now());
        d.setTitle("");
        d.setContent("");
        return devotionalRepo.save(d);
    }

    @Transactional
    public Devotional update(UUID id, String title, String content) {
        Devotional d = devotionalRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Devotional not found: " + id));
        if (title != null) d.setTitle(title);
        if (content != null) d.setContent(content);
        return devotionalRepo.save(d);
    }

    @Transactional
    public Devotional addVerse(UUID devotionalId, Long verseId, Integer bibleVersionId, String referenceText) {
        Devotional d = devotionalRepo.findById(devotionalId)
            .orElseThrow(() -> new IllegalArgumentException("Devotional not found: " + devotionalId));
        DevotionalVerse dv = new DevotionalVerse(d, verseId, bibleVersionId, referenceText);
        d.getVerses().add(dv);
        return devotionalRepo.save(d);
    }

    @Transactional
    public void removeVerse(UUID devotionalId, Long verseId) {
        Devotional d = devotionalRepo.findById(devotionalId)
            .orElseThrow(() -> new IllegalArgumentException("Devotional not found: " + devotionalId));
        d.getVerses().removeIf(v -> v.getVerseId().equals(verseId));
        devotionalRepo.save(d);
    }
}
