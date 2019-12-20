package com.section.demo.repository;

import com.section.demo.entity.GeoClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GeoClassRepository extends JpaRepository<GeoClass, Long> {

    List<GeoClass> findAllByCode(String code);

    @Query("SELECT COUNT(g.id) FROM GeoClass g")
    Integer countAllRows();

}
