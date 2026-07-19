package com.metanoia.repository;

import com.metanoia.model.Devotional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DevotionalRepository extends JpaRepository<Devotional, UUID> {
    Optional<Devotional> findByDate(LocalDate date);

    @Query("SELECT d FROM Devotional d ORDER BY d.date DESC")
    List<Devotional> findAllOrderByDateDesc();
}
