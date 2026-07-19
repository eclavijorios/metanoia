package com.metanoia.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "devotional_verses")
public class DevotionalVerse {
    @EmbeddedId
    private DevotionalVerseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("devotionalId")
    @JoinColumn(name = "devotional_id")
    private Devotional devotional;

    @Column(name = "bible_version_id", nullable = false)
    private Integer bibleVersionId;

    @Column(name = "reference_text", nullable = false, length = 50)
    private String referenceText;

    public DevotionalVerse() {}

    public DevotionalVerse(Devotional devotional, Long verseId, Integer bibleVersionId, String referenceText) {
        this.id = new DevotionalVerseId(devotional.getId(), verseId);
        this.devotional = devotional;
        this.bibleVersionId = bibleVersionId;
        this.referenceText = referenceText;
    }

    public DevotionalVerseId getId() { return id; }
    public void setId(DevotionalVerseId id) { this.id = id; }
    public Devotional getDevotional() { return devotional; }
    public void setDevotional(Devotional devotional) { this.devotional = devotional; }
    public Long getVerseId() { return id.getVerseId(); }
    public void setVerseId(Long verseId) { this.id.setVerseId(verseId); }
    public Integer getBibleVersionId() { return bibleVersionId; }
    public void setBibleVersionId(Integer bibleVersionId) { this.bibleVersionId = bibleVersionId; }
    public String getReferenceText() { return referenceText; }
    public void setReferenceText(String referenceText) { this.referenceText = referenceText; }
}
