package com.metanoia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "verses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bible_version_id", "book_id", "chapter", "verse"}))
public class Verse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bible_version_id", nullable = false)
    private Integer bibleVersionId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Integer chapter;

    @Column(nullable = false)
    private Integer verse;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "search_vector", columnDefinition = "TSVECTOR", insertable = false, updatable = false)
    private String searchVector;

    @Transient
    private String bookName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getBibleVersionId() { return bibleVersionId; }
    public void setBibleVersionId(Integer bibleVersionId) { this.bibleVersionId = bibleVersionId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public Integer getChapter() { return chapter; }
    public void setChapter(Integer chapter) { this.chapter = chapter; }
    public Integer getVerse() { return verse; }
    public void setVerse(Integer verse) { this.verse = verse; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSearchVector() { return searchVector; }
    public void setSearchVector(String searchVector) { this.searchVector = searchVector; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
}
