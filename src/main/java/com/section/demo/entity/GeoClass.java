package com.section.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
public class GeoClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "section_id")
    @JsonBackReference
    private Section sections;

    public GeoClass() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Section getSections() {
        return sections;
    }

    public void setSections(Section sections) {
        this.sections = sections;
    }
}
