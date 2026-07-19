package com.metanoia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "osis_id", unique = true, nullable = false, length = 10)
    private String osisId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer testament;

    @Column(nullable = false)
    private Integer position;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOsisId() { return osisId; }
    public void setOsisId(String osisId) { this.osisId = osisId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getTestament() { return testament; }
    public void setTestament(Integer testament) { this.testament = testament; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
