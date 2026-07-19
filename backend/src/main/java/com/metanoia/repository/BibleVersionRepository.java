package com.metanoia.repository;

import com.metanoia.model.BibleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BibleVersionRepository extends JpaRepository<BibleVersion, Integer> {
    Optional<BibleVersion> findBySlug(String slug);
}
