package com.chocobi.leafy.place.infra.repository;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalPlaceRepository extends JpaRepository<ExternalPlaceEntity, Long> {
    List<ExternalPlaceEntity> findAllByRegion(RegionEntity region);

    List<ExternalPlaceEntity> findAllByStatus(ExternalPlaceStatus status);

    List<ExternalPlaceEntity> findAllByRegionAndStatus(RegionEntity region, ExternalPlaceStatus status);

    Optional<ExternalPlaceEntity> findByIdAndStatus(Long id, ExternalPlaceStatus status);

    @EntityGraph(attributePaths = "category")
    List<ExternalPlaceEntity> findAllBySourceAndExternalContentIdIn(
            ExternalPlaceSource source,
            Collection<String> externalContentIds
    );

    @Query("select p.externalContentId from ExternalPlaceEntity p "
            + "where p.source = :source and p.externalContentId is not null and p.status = :status")
    List<String> findExternalContentIds(ExternalPlaceSource source, ExternalPlaceStatus status);
}
