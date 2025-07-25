package com.chocobi.leafy.place.repository;

import com.chocobi.leafy.place.entity.AreaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AreaCodeRepository extends JpaRepository<AreaCode, Integer> {
}
