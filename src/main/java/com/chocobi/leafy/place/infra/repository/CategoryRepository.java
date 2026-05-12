package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
}
