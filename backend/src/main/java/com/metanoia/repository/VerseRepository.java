package com.metanoia.repository;

import com.metanoia.model.Verse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VerseRepository extends JpaRepository<Verse, Long> {

    List<Verse> findByBibleVersionIdAndBookIdAndChapterOrderByVerse(
        Integer bibleVersionId, Integer bookId, Integer chapter);

    @Query(value = """
        SELECT v.* FROM verses v
        WHERE v.bible_version_id = :versionId
        AND v.search_vector @@ plainto_tsquery('spanish', :query)
        ORDER BY ts_rank(v.search_vector, plainto_tsquery('spanish', :query)) DESC
        LIMIT 50
        """, nativeQuery = true)
    List<Verse> searchByText(@Param("versionId") Integer versionId, @Param("query") String query);
}
