package com.metanoia.service;

import com.metanoia.model.BibleVersion;
import com.metanoia.model.Book;
import com.metanoia.model.Verse;
import com.metanoia.repository.BibleVersionRepository;
import com.metanoia.repository.BookRepository;
import com.metanoia.repository.VerseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.util.*;

@Service
public class BibleImportService {

    private final BibleVersionRepository bibleVersionRepo;
    private final BookRepository bookRepo;
    private final VerseRepository verseRepo;
    private final ObjectMapper mapper;

    private static final Map<String, String> OSIS_NAMES = new LinkedHashMap<>();
    static {
        OSIS_NAMES.put("Gen", "Génesis"); OSIS_NAMES.put("Exod", "Éxodo");
        OSIS_NAMES.put("Lev", "Levítico"); OSIS_NAMES.put("Num", "Números");
        OSIS_NAMES.put("Deut", "Deuteronomio"); OSIS_NAMES.put("Josh", "Josué");
        OSIS_NAMES.put("Judg", "Jueces"); OSIS_NAMES.put("Ruth", "Rut");
        OSIS_NAMES.put("1Sam", "1 Samuel"); OSIS_NAMES.put("2Sam", "2 Samuel");
        OSIS_NAMES.put("1Kgs", "1 Reyes"); OSIS_NAMES.put("2Kgs", "2 Reyes");
        OSIS_NAMES.put("1Chr", "1 Crónicas"); OSIS_NAMES.put("2Chr", "2 Crónicas");
        OSIS_NAMES.put("Ezra", "Esdras"); OSIS_NAMES.put("Neh", "Nehemías");
        OSIS_NAMES.put("Esth", "Ester"); OSIS_NAMES.put("Job", "Job");
        OSIS_NAMES.put("Ps", "Salmos"); OSIS_NAMES.put("Prov", "Proverbios");
        OSIS_NAMES.put("Eccl", "Eclesiastés"); OSIS_NAMES.put("Song", "Cantares");
        OSIS_NAMES.put("Isa", "Isaías"); OSIS_NAMES.put("Jer", "Jeremías");
        OSIS_NAMES.put("Lam", "Lamentaciones"); OSIS_NAMES.put("Ezek", "Ezequiel");
        OSIS_NAMES.put("Dan", "Daniel"); OSIS_NAMES.put("Hos", "Oseas");
        OSIS_NAMES.put("Joel", "Joel"); OSIS_NAMES.put("Amos", "Amós");
        OSIS_NAMES.put("Obad", "Abdías"); OSIS_NAMES.put("Jonah", "Jonás");
        OSIS_NAMES.put("Mic", "Miqueas"); OSIS_NAMES.put("Nah", "Nahúm");
        OSIS_NAMES.put("Hab", "Habacuc"); OSIS_NAMES.put("Zeph", "Sofonías");
        OSIS_NAMES.put("Hag", "Hageo"); OSIS_NAMES.put("Zech", "Zacarías");
        OSIS_NAMES.put("Mal", "Malaquías");
        OSIS_NAMES.put("Matt", "Mateo"); OSIS_NAMES.put("Mark", "Marcos");
        OSIS_NAMES.put("Luke", "Lucas"); OSIS_NAMES.put("John", "Juan");
        OSIS_NAMES.put("Acts", "Hechos"); OSIS_NAMES.put("Rom", "Romanos");
        OSIS_NAMES.put("1Cor", "1 Corintios"); OSIS_NAMES.put("2Cor", "2 Corintios");
        OSIS_NAMES.put("Gal", "Gálatas"); OSIS_NAMES.put("Eph", "Efesios");
        OSIS_NAMES.put("Phil", "Filipenses"); OSIS_NAMES.put("Col", "Colosenses");
        OSIS_NAMES.put("1Thess", "1 Tesalonicenses"); OSIS_NAMES.put("2Thess", "2 Tesalonicenses");
        OSIS_NAMES.put("1Tim", "1 Timoteo"); OSIS_NAMES.put("2Tim", "2 Timoteo");
        OSIS_NAMES.put("Titus", "Tito"); OSIS_NAMES.put("Phlm", "Filemón");
        OSIS_NAMES.put("Heb", "Hebreos"); OSIS_NAMES.put("Jas", "Santiago");
        OSIS_NAMES.put("1Pet", "1 Pedro"); OSIS_NAMES.put("2Pet", "2 Pedro");
        OSIS_NAMES.put("1John", "1 Juan"); OSIS_NAMES.put("2John", "2 Juan");
        OSIS_NAMES.put("3John", "3 Juan"); OSIS_NAMES.put("Jude", "Judas");
        OSIS_NAMES.put("Rev", "Apocalipsis");
    }

    public BibleImportService(BibleVersionRepository bibleVersionRepo, BookRepository bookRepo,
                              VerseRepository verseRepo, ObjectMapper mapper) {
        this.bibleVersionRepo = bibleVersionRepo;
        this.bookRepo = bookRepo;
        this.verseRepo = verseRepo;
        this.mapper = mapper;
    }

    public boolean needsImport() {
        return bibleVersionRepo.count() == 0;
    }

    @Transactional
    public void importAll() throws Exception {
        importVersion("fbv", "Versión Biblia Libre", "CC BY-SA 4.0", "data/fbv.json");
        importVersion("rv1909", "Reina-Valera 1909", "public-domain", "data/rv1909.json");
    }

    private void importVersion(String slug, String name, String license, String resourcePath) throws Exception {
        BibleVersion version = bibleVersionRepo.findBySlug(slug)
                .orElseGet(() -> {
                    BibleVersion v = new BibleVersion();
                    v.setSlug(slug);
                    v.setName(name);
                    v.setLanguage("es");
                    v.setLicense(license);
                    return bibleVersionRepo.save(v);
                });

        InputStream is = new ClassPathResource(resourcePath).getInputStream();
        JsonNode root = mapper.readTree(is);

        for (JsonNode bookNode : root) {
            String osisId = bookNode.get("id").asText();
            String bookName = OSIS_NAMES.getOrDefault(osisId, osisId);
            int testament = isNewTestament(osisId) ? 2 : 1;

            Book book = bookRepo.findByOsisId(osisId).orElseGet(() -> {
                Book b = new Book();
                b.setOsisId(osisId);
                b.setName(bookName);
                b.setTestament(testament);
                b.setPosition(getBookPosition(osisId));
                return bookRepo.save(b);
            });

            JsonNode chapters = bookNode.get("chapters");
            if (chapters == null) continue;

            int chapterNum = 1;
            for (JsonNode chapterNode : chapters) {
                JsonNode verses = chapterNode.get("verses");
                if (verses == null) continue;

                int verseNum = 1;
                for (JsonNode verseNode : verses) {
                    Verse v = new Verse();
                    v.setBibleVersionId(version.getId());
                    v.setBookId(book.getId());
                    v.setChapter(chapterNum);
                    v.setVerse(verseNum);
                    v.setText(verseNode.asText());
                    verseRepo.save(v);
                    verseNum++;
                }
                chapterNum++;
            }
        }
    }

    private boolean isNewTestament(String osisId) {
        return switch (osisId) {
            case "Matt", "Mark", "Luke", "John", "Acts", "Rom", "1Cor", "2Cor",
                 "Gal", "Eph", "Phil", "Col", "1Thess", "2Thess", "1Tim", "2Tim",
                 "Titus", "Phlm", "Heb", "Jas", "1Pet", "2Pet", "1John", "2John",
                 "3John", "Jude", "Rev" -> true;
            default -> false;
        };
    }

    private int getBookPosition(String osisId) {
        List<String> books = new ArrayList<>(OSIS_NAMES.keySet());
        return books.indexOf(osisId) + 1;
    }
}
