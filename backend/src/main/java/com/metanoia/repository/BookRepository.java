package com.metanoia.repository;

import com.metanoia.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Optional<Book> findByOsisId(String osisId);
}
