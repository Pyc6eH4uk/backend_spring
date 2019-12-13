package com.section.demo.service;

import com.section.demo.entity.Section;
import com.section.demo.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SectionService {

    @Autowired
    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public void createSection(Section section) {
        sectionRepository.save(section);
    }
}
