package com.section.demo.service;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.repository.SectionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
public class SectionService {

    @PersistenceContext
    private EntityManager entityManager;

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<Section> findAll() {
        return sectionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Section create(Section section) {
        return sectionRepository.save(section);
    }

    public Section update(Section section) {
        sectionRepository.getOne(section.getId());
        return sectionRepository.save(section);
    }

    public void delete(Long sectionId) {
        sectionRepository.delete(sectionRepository.getOne(sectionId));
    }

    public List<Section> findAllByGeoClassCode(String code) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Section> criteria = cb.createQuery(Section.class);
        Root<Section> root = criteria.from(Section.class);
        Join<Section, GeoClass> join = root.join("geologicalClasses");
        criteria.where(
                cb.equal(
                        join.get("code"),
                        code
                )
        );
        return entityManager.createQuery(criteria).getResultList();
    }

}
