package com.metanoia.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DevotionalVerseId implements Serializable {
    private UUID devotionalId;
    private Long verseId;

    public DevotionalVerseId() {}

    public DevotionalVerseId(UUID devotionalId, Long verseId) {
        this.devotionalId = devotionalId;
        this.verseId = verseId;
    }

    public UUID getDevotionalId() { return devotionalId; }
    public void setDevotionalId(UUID devotionalId) { this.devotionalId = devotionalId; }
    public Long getVerseId() { return verseId; }
    public void setVerseId(Long verseId) { this.verseId = verseId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevotionalVerseId that = (DevotionalVerseId) o;
        return Objects.equals(devotionalId, that.devotionalId) &&
               Objects.equals(verseId, that.verseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devotionalId, verseId);
    }
}
