package com.metanoia.service;

import com.metanoia.model.BibleVersion;
import com.metanoia.model.Book;
import com.metanoia.model.Verse;
import com.metanoia.repository.BibleVersionRepository;
import com.metanoia.repository.BookRepository;
import com.metanoia.repository.VerseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BibleService {

    private final BibleVersionRepository bibleVersionRepo;
    private final BookRepository bookRepo;
    private final VerseRepository verseRepo;

    public BibleService(BibleVersionRepository bibleVersionRepo, BookRepository bookRepo, VerseRepository verseRepo) {
        this.bibleVersionRepo = bibleVersionRepo;
        this.bookRepo = bookRepo;
        this.verseRepo = verseRepo;
    }

    public List<BibleVersion> getVersions() {
        return bibleVersionRepo.findAll();
    }

    public List<Verse> getPassage(String bookOsisId, int chapter, Integer verseNum, String versionSlug) {
        BibleVersion version = bibleVersionRepo.findBySlug(versionSlug)
            .orElseThrow(() -> new IllegalArgumentException("Unknown version: " + versionSlug));
        Book book = bookRepo.findByOsisId(bookOsisId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown book: " + bookOsisId));

        List<Verse> verses = verseRepo.findByBibleVersionIdAndBookIdAndChapterOrderByVerse(
            version.getId(), book.getId(), chapter);

        verses.forEach(v -> v.setBookName(book.getName()));

        if (verseNum != null) {
            return verses.stream()
                .filter(v -> v.getVerse().equals(verseNum))
                .toList();
        }
        return verses;
    }

    public List<Verse> search(String query, String versionSlug) {
        BibleVersion version = bibleVersionRepo.findBySlug(versionSlug)
            .orElseThrow(() -> new IllegalArgumentException("Unknown version: " + versionSlug));

        List<Verse> results = verseRepo.searchByText(version.getId(), query);
        results.forEach(v -> {
            Book book = bookRepo.findById(v.getBookId()).orElse(null);
            if (book != null) v.setBookName(book.getName());
        });
        return results;
    }
}
