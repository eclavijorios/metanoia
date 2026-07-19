package com.metanoia.config;

import com.metanoia.service.BibleImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final BibleImportService bibleImportService;

    public DataInitializer(BibleImportService bibleImportService) {
        this.bibleImportService = bibleImportService;
    }

    @Override
    public void run(String... args) {
        if (bibleImportService.needsImport()) {
            log.info("Base de datos vacía — importando datos bíblicos...");
            try {
                bibleImportService.importAll();
                log.info("Importación completada exitosamente.");
            } catch (Exception e) {
                log.error("Error durante la importación: {}", e.getMessage(), e);
            }
        } else {
            log.info("Datos bíblicos ya presentes — omitiendo importación.");
        }
    }
}
