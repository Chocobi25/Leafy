package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findAllByCodeIn(Collection<String> codes);
}
