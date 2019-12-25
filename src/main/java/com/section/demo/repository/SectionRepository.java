package com.section.demo.repository;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    Section findByGeologicalClasses(GeoClass geoClass);
}
