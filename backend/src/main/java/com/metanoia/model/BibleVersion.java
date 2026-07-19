package com.metanoia.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bible_versions")
public class BibleVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 20)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String language;

    private String license;

    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
