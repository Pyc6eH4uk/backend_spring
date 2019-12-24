package com.section.demo.entity;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "section")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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


