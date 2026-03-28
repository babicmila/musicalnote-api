package com.musicalnote.musicalnote.repository;

import com.musicalnote.musicalnote.entity.Instrument;
import com.musicalnote.musicalnote.enums.Category;
import com.musicalnote.musicalnote.enums.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    List<Instrument> findByAvailableTrue();
    List<Instrument> findByCategory(Category category);
    List<Instrument> findByCondition(Condition condition);
    List<Instrument> findByAvailableTrueAndCategory(Category category);
    List<Instrument> findByAvailableTrueAndCondition(Condition condition);
    List<Instrument> findByAvailableTrueAndCategoryAndCondition(Category category, Condition condition);
}
