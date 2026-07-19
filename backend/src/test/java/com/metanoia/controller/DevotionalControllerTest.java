package com.metanoia.controller;

import com.metanoia.model.Devotional;
import com.metanoia.service.DevotionalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevotionalController.class)
class DevotionalControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DevotionalService devotionalService;

    @Test
    void getToday_returnsDevotional() throws Exception {
        var d = new Devotional();
        d.setId(UUID.randomUUID());
        d.setTitle("My Devotional");
        when(devotionalService.getToday()).thenReturn(d);

        mockMvc.perform(get("/api/devotionals/today"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("My Devotional"));
    }

    @Test
    void getAll_returnsList() throws Exception {
        var d = new Devotional();
        d.setId(UUID.randomUUID());
        d.setDate(LocalDate.of(2026, 7, 19));
        when(devotionalService.getAll()).thenReturn(List.of(d));

        mockMvc.perform(get("/api/devotionals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].date").value("2026-07-19"));
    }

    @Test
    void getByDate_returnsDevotional() throws Exception {
        var d = new Devotional();
        d.setId(UUID.randomUUID());
        d.setDate(LocalDate.of(2026, 7, 19));
        when(devotionalService.getByDate(LocalDate.of(2026, 7, 19))).thenReturn(d);

        mockMvc.perform(get("/api/devotionals")
                .param("date", "2026-07-19"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].date").value("2026-07-19"));
    }

    @Test
    void create_returnsDevotional() throws Exception {
        var d = new Devotional();
        d.setId(UUID.randomUUID());
        d.setDate(LocalDate.of(2026, 7, 20));
        when(devotionalService.create(LocalDate.of(2026, 7, 20))).thenReturn(d);

        mockMvc.perform(post("/api/devotionals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\": \"2026-07-20\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-07-20"));
    }

    @Test
    void update_returnsDevotional() throws Exception {
        var d = new Devotional();
        d.setId(UUID.randomUUID());
        d.setTitle("Updated Title");
        d.setContent("Updated Content");
        when(devotionalService.update(any(), eq("Updated Title"), eq("Updated Content")))
            .thenReturn(d);

        mockMvc.perform(put("/api/devotionals/" + d.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Updated Title\", \"content\": \"Updated Content\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void addVerse_returnsDevotional() throws Exception {
        var d = new Devotional();
        d.setId(UUID.randomUUID());
        when(devotionalService.addVerse(any(), eq(100L), eq(1), eq("Juan 3:16")))
            .thenReturn(d);

        mockMvc.perform(post("/api/devotionals/" + d.getId() + "/verses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"verseId\": 100, \"bibleVersionId\": 1, \"referenceText\": \"Juan 3:16\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void removeVerse_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/devotionals/" + UUID.randomUUID() + "/verses/100"))
            .andExpect(status().isNoContent());
    }
}
