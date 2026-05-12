package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.dto.request.AdminPlacePageRequest;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.infra.entity.QExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.QPlaceEntity;
import com.chocobi.leafy.place.infra.repository.PlaceRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import com.chocobi.leafy.place.vo.PlaceType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceFindService {
    private final PlaceRepository placeRepository;
    private final JPAQueryFactory queryFactory;

    public PlaceEntity findPlace(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.PLACE_NOT_FOUND));
    }

    public List<PlaceEntity> findAll() {
        return placeRepository.findAll();
    }

    public Page<PlaceEntity> findPagePlaces(AdminPlacePageRequest request, Pageable pageable) {
        QPlaceEntity place = QPlaceEntity.placeEntity;
        BooleanBuilder conditions = buildConditions(request);

        List<PlaceEntity> content = queryFactory
                .selectFrom(place)
                .where(conditions)
                .orderBy(place.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(place.count())
                .from(place)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildConditions(AdminPlacePageRequest request) {
        QPlaceEntity place = QPlaceEntity.placeEntity;
        QExternalPlaceEntity externalPlace = QExternalPlaceEntity.externalPlaceEntity;

        BooleanBuilder conditions = new BooleanBuilder();

        if (request.placeType() != null) {
            if (PlaceType.EXTERNAL.equals(request.placeType())) {
                conditions.and(place.instanceOf(ExternalPlaceEntity.class));
            } else {
                conditions.and(place.instanceOf(CustomPlaceEntity.class));
            }
        }

        if (request.regionId() != null) {
            conditions.and(JPAExpressions
                    .selectOne()
                    .from(externalPlace)
                    .where(
                            externalPlace.id.eq(place.id),
                            externalPlace.region.id.eq(request.regionId())
                    )
                    .exists());
        }

        if (request.categoryId() != null) {
            conditions.and(JPAExpressions
                    .selectOne()
                    .from(externalPlace)
                    .where(
                            externalPlace.id.eq(place.id),
                            externalPlace.category.id.eq(request.categoryId())
                    )
                    .exists());
        }

        return conditions;
    }
}
