package com.metanoia.service;

import com.metanoia.model.Devotional;
import com.metanoia.model.DevotionalVerse;
import com.metanoia.repository.DevotionalRepository;
import com.metanoia.repository.DevotionalVerseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevotionalServiceTest {

    @Mock
    DevotionalRepository devotionalRepo;

    @Mock
    DevotionalVerseRepository verseRepo;

    @InjectMocks
    DevotionalService devotionalService;

    @Captor
    ArgumentCaptor<Devotional> devotionalCaptor;

    @Test
    void getToday_returnsExisting() {
        var today = LocalDate.now();
        var existing = new Devotional();
        existing.setId(UUID.randomUUID());
        existing.setDate(today);
        when(devotionalRepo.findByDate(today)).thenReturn(Optional.of(existing));

        var result = devotionalService.getToday();

        assertEquals(existing.getId(), result.getId());
        verify(devotionalRepo, never()).save(any());
    }

    @Test
    void getToday_createsWhenMissing() {
        var today = LocalDate.now();
        when(devotionalRepo.findByDate(today)).thenReturn(Optional.empty());
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = devotionalService.getToday();

        assertEquals(today, result.getDate());
        assertEquals("", result.getTitle());
        assertEquals("", result.getContent());
        verify(devotionalRepo).save(any());
    }

    @Test
    void getByDate_returnsDevotional() {
        var date = LocalDate.of(2026, 7, 19);
        var d = new Devotional();
        d.setDate(date);
        when(devotionalRepo.findByDate(date)).thenReturn(Optional.of(d));

        var result = devotionalService.getByDate(date);

        assertNotNull(result);
        assertEquals(date, result.getDate());
    }

    @Test
    void getByDate_returnsNullWhenMissing() {
        when(devotionalRepo.findByDate(any())).thenReturn(Optional.empty());

        assertNull(devotionalService.getByDate(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void getAll_returnsAllOrderedByDateDesc() {
        var d1 = new Devotional(); d1.setDate(LocalDate.of(2026, 7, 19));
        var d2 = new Devotional(); d2.setDate(LocalDate.of(2026, 7, 18));
        when(devotionalRepo.findAllOrderByDateDesc()).thenReturn(List.of(d1, d2));

        var result = devotionalService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void create_usesProvidedDate() {
        var date = LocalDate.of(2026, 7, 20);
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = devotionalService.create(date);

        assertEquals(date, result.getDate());
    }

    @Test
    void create_usesTodayWhenDateNull() {
        var today = LocalDate.now();
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = devotionalService.create(null);

        assertEquals(today, result.getDate());
    }

    @Test
    void update_changesTitleAndContent() {
        var id = UUID.randomUUID();
        var d = new Devotional();
        d.setId(id);
        when(devotionalRepo.findById(id)).thenReturn(Optional.of(d));
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = devotionalService.update(id, "New Title", "New Content");

        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
    }

    @Test
    void update_skipsNullFields() {
        var id = UUID.randomUUID();
        var d = new Devotional();
        d.setId(id);
        d.setTitle("Original");
        d.setContent("Original");
        when(devotionalRepo.findById(id)).thenReturn(Optional.of(d));
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = devotionalService.update(id, null, null);

        assertEquals("Original", result.getTitle());
        assertEquals("Original", result.getContent());
    }

    @Test
    void update_unknownId_throws() {
        var id = UUID.randomUUID();
        when(devotionalRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> devotionalService.update(id, "t", "c"));
    }

    @Test
    void addVerse_addsToDevotional() {
        var id = UUID.randomUUID();
        var d = new Devotional();
        d.setId(id);
        when(devotionalRepo.findById(id)).thenReturn(Optional.of(d));
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = devotionalService.addVerse(id, 100L, 1, "Juan 3:16");

        assertEquals(1, result.getVerses().size());
        assertEquals("Juan 3:16", result.getVerses().getFirst().getReferenceText());
    }

    @Test
    void addVerse_unknownDevotional_throws() {
        var id = UUID.randomUUID();
        when(devotionalRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> devotionalService.addVerse(id, 1L, 1, "ref"));
    }

    @Test
    void removeVerse_removesMatchingVerse() {
        var id = UUID.randomUUID();
        var d = new Devotional();
        d.setId(id);
        var dv = new DevotionalVerse(d, 100L, 1, "Gen 1:1");
        d.getVerses().add(dv);
        when(devotionalRepo.findById(id)).thenReturn(Optional.of(d));
        when(devotionalRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        devotionalService.removeVerse(id, 100L);

        assertTrue(d.getVerses().isEmpty());
    }

    @Test
    void removeVerse_unknownDevotional_throws() {
        var id = UUID.randomUUID();
        when(devotionalRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> devotionalService.removeVerse(id, 100L));
    }
}
