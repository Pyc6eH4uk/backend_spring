package com.section.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "section")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id")
    public List<GeoClass> geoClasses;

    public Section(){

    }

    public Section(String name) {
        this.name = name;
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

    public List<GeoClass> getGeoClasses() {
        return geoClasses;
    }

    public void setGeoClasses(List<GeoClass> geoClasses) {
        this.geoClasses = geoClasses;
    }

    @Override
    public String toString() {
        return String.format(
                "Section[id=%d, name='%s']%n",
                id, name);
    }
}


