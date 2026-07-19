package com.metanoia.controller;

import com.metanoia.model.BibleVersion;
import com.metanoia.model.Verse;
import com.metanoia.service.BibleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BibleController.class)
class BibleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BibleService bibleService;

    @Test
    void getVersions_returnsList() throws Exception {
        var v = new BibleVersion();
        v.setId(1);
        v.setSlug("fbv");
        v.setName("Versión Biblia Libre");
        when(bibleService.getVersions()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/bibles/versions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].slug").value("fbv"));
    }

    @Test
    void getPassage_returnsVerses() throws Exception {
        var verse = new Verse();
        verse.setId(1L);
        verse.setVerse(1);
        verse.setText("En el principio...");
        when(bibleService.getPassage("Gen", 1, null, "fbv"))
            .thenReturn(List.of(verse));

        mockMvc.perform(get("/api/bibles/passage")
                .param("book", "Gen")
                .param("ch", "1")
                .param("version", "fbv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].verse").value(1));
    }

    @Test
    void getPassage_withSingleVerse() throws Exception {
        var verse = new Verse();
        verse.setId(1L);
        verse.setVerse(1);
        verse.setText("En el principio...");
        when(bibleService.getPassage("Gen", 1, 1, "fbv"))
            .thenReturn(List.of(verse));

        mockMvc.perform(get("/api/bibles/passage")
                .param("book", "Gen")
                .param("ch", "1")
                .param("v", "1")
                .param("version", "fbv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].verse").value(1));
    }

    @Test
    void getPassage_defaultsToFbv() throws Exception {
        when(bibleService.getPassage("Gen", 1, null, "fbv"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/bibles/passage")
                .param("book", "Gen")
                .param("ch", "1"))
            .andExpect(status().isOk());
    }

    @Test
    void search_returnsResults() throws Exception {
        var verse = new Verse();
        verse.setId(1L);
        verse.setText("Dios es amor");
        when(bibleService.search("amor", "fbv")).thenReturn(List.of(verse));

        mockMvc.perform(get("/api/bibles/search")
                .param("q", "amor")
                .param("version", "fbv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].text").value("Dios es amor"));
    }

    @Test
    void search_defaultsToFbv() throws Exception {
        when(bibleService.search("fe", "fbv")).thenReturn(List.of());

        mockMvc.perform(get("/api/bibles/search")
                .param("q", "fe"))
            .andExpect(status().isOk());
    }
}
