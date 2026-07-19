package com.metanoia.repository;

import com.metanoia.model.DevotionalVerse;
import com.metanoia.model.DevotionalVerseId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevotionalVerseRepository extends JpaRepository<DevotionalVerse, DevotionalVerseId> {
}
