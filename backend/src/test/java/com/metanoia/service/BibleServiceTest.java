package com.metanoia.service;

import com.metanoia.model.BibleVersion;
import com.metanoia.model.Book;
import com.metanoia.model.Verse;
import com.metanoia.repository.BibleVersionRepository;
import com.metanoia.repository.BookRepository;
import com.metanoia.repository.VerseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BibleServiceTest {

    @Mock
    BibleVersionRepository bibleVersionRepo;

    @Mock
    BookRepository bookRepo;

    @Mock
    VerseRepository verseRepo;

    @InjectMocks
    BibleService bibleService;

    @Test
    void getVersions_returnsAllVersions() {
        when(bibleVersionRepo.findAll()).thenReturn(List.of(
            version(1, "fbv", "Versión Biblia Libre"),
            version(2, "rv1909", "Reina-Valera 1909")
        ));

        var versions = bibleService.getVersions();

        assertEquals(2, versions.size());
        assertEquals("fbv", versions.get(0).getSlug());
    }

    @Test
    void getPassage_returnsVersesForChapter() {
        var version = version(1, "fbv", "test");
        var book = book(1, "Gen", "Génesis");
        when(bibleVersionRepo.findBySlug("fbv")).thenReturn(Optional.of(version));
        when(bookRepo.findByOsisId("Gen")).thenReturn(Optional.of(book));
        when(verseRepo.findByBibleVersionIdAndBookIdAndChapterOrderByVerse(1, 1, 1))
            .thenReturn(List.of(
                verse(1, 1, 1, "v1"),
                verse(2, 1, 1, "v2")
            ));

        var result = bibleService.getPassage("Gen", 1, null, "fbv");

        assertEquals(2, result.size());
        assertEquals("Génesis", result.get(0).getBookName());
    }

    @Test
    void getPassage_withVerseNum_returnsSingleVerse() {
        var version = version(1, "fbv", "test");
        var book = book(1, "Gen", "Génesis");
        when(bibleVersionRepo.findBySlug("fbv")).thenReturn(Optional.of(version));
        when(bookRepo.findByOsisId("Gen")).thenReturn(Optional.of(book));
        when(verseRepo.findByBibleVersionIdAndBookIdAndChapterOrderByVerse(1, 1, 1))
            .thenReturn(List.of(
                verse(1, 1, 1, "v1"),
                verse(2, 1, 2, "v2")
            ));

        var result = bibleService.getPassage("Gen", 1, 2, "fbv");

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().getVerse());
    }

    @Test
    void getPassage_unknownVersion_throws() {
        when(bibleVersionRepo.findBySlug("bad")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> bibleService.getPassage("Gen", 1, null, "bad"));
    }

    @Test
    void getPassage_unknownBook_throws() {
        when(bibleVersionRepo.findBySlug("fbv")).thenReturn(Optional.of(version(1, "fbv", "t")));
        when(bookRepo.findByOsisId("XYZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> bibleService.getPassage("XYZ", 1, null, "fbv"));
    }

    @Test
    void search_returnsResults() {
        var version = version(1, "fbv", "test");
        when(bibleVersionRepo.findBySlug("fbv")).thenReturn(Optional.of(version));
        when(verseRepo.searchByText(1, "amor")).thenReturn(List.of(
            verse(10, 1, 1, "Dios es amor")
        ));
        when(bookRepo.findById(1)).thenReturn(Optional.of(book(1, "1John", "1 Juan")));

        var result = bibleService.search("amor", "fbv");

        assertEquals(1, result.size());
        assertEquals("1 Juan", result.getFirst().getBookName());
    }

    @Test
    void search_unknownVersion_throws() {
        when(bibleVersionRepo.findBySlug("bad")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> bibleService.search("amor", "bad"));
    }

    private BibleVersion version(Integer id, String slug, String name) {
        var v = new BibleVersion();
        v.setId(id);
        v.setSlug(slug);
        v.setName(name);
        return v;
    }

    private Book book(Integer id, String osisId, String name) {
        var b = new Book();
        b.setId(id);
        b.setOsisId(osisId);
        b.setName(name);
        return b;
    }

    private Verse verse(long id, int bookId, int verseNum, String text) {
        var v = new Verse();
        v.setId(id);
        v.setBookId(bookId);
        v.setVerse(verseNum);
        v.setText(text);
        return v;
    }
}
