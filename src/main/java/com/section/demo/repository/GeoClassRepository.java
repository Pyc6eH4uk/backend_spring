package com.section.demo.repository;

import com.section.demo.entity.GeoClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoClassRepository extends JpaRepository<GeoClass, Long> {

    List<GeoClass> findAllByCode(String code);

}
