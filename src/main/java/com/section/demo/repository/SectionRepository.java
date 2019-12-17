package com.section.demo.repository;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    @Override
    List<Section> findAll();

    @Query("SELECT DISTINCT s FROM Section s JOIN GeoClass g ON g.code = '2312'")
    List<Section> getAllSectionsByCode(@Param("code") String code);

    Section findByGeoClasses(GeoClass geoClass);
}
