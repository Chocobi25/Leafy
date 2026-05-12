package com.chocobi.leafy.place.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.chocobi.leafy.global.config.QueryDSLConfig;
import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.global.entity.RegionLevel;
import com.chocobi.leafy.global.entity.RegionRepository;
import com.chocobi.leafy.place.dto.request.AdminPlacePageRequest;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.entity.CustomPlaceEntity;
import com.chocobi.leafy.place.infra.entity.ExternalPlaceEntity;
import com.chocobi.leafy.place.infra.entity.PlaceEntity;
import com.chocobi.leafy.place.infra.repository.CategoryRepository;
import com.chocobi.leafy.place.infra.repository.CustomPlaceRepository;
import com.chocobi.leafy.place.infra.repository.ExternalPlaceRepository;
import com.chocobi.leafy.place.vo.PlaceType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({PlaceFindService.class, QueryDSLConfig.class})
@ActiveProfiles("test")
class PlaceFindServiceTest {
    @Autowired
    private PlaceFindService placeFindService;

    @Autowired
    private ExternalPlaceRepository externalPlaceRepository;

    @Autowired
    private CustomPlaceRepository customPlaceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void 최신순으로_페이징_조회() {
        CategoryEntity nature = saveCategory("자연", "NATURE");
        RegionEntity seoul = saveRegion("서울");

        saveExternalPlace("오래된 외부 장소", nature, seoul);
        saveCustomPlace("중간 커스텀 장소");
        saveExternalPlace("최신 외부 장소", nature, seoul);

        AdminPlacePageRequest request = new AdminPlacePageRequest(1, 2, null, null, null);
        Page<PlaceEntity> page = placeFindService.findPagePlaces(
                request,
                PageRequest.of(request.page() - 1, request.size())
        );

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(titles(page)).containsExactly("최신 외부 장소", "중간 커스텀 장소");
    }

    @Test
    void 장소_타입으로_필터링() {
        CategoryEntity nature = saveCategory("자연", "NATURE");
        RegionEntity seoul = saveRegion("서울");

        saveCustomPlace("커스텀 장소");
        saveExternalPlace("외부 장소", nature, seoul);

        AdminPlacePageRequest request = new AdminPlacePageRequest(1, 10, null, null, PlaceType.EXTERNAL);
        Page<PlaceEntity> page = placeFindService.findPagePlaces(
                request,
                PageRequest.of(request.page() - 1, request.size())
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasOnlyElementsOfType(ExternalPlaceEntity.class);
        assertThat(titles(page)).containsExactly("외부 장소");
    }

    @Test
    void 지역과_카테고리로_필터링() {
        CategoryEntity nature = saveCategory("자연", "NATURE");
        CategoryEntity food = saveCategory("음식", "FOOD");
        RegionEntity seoul = saveRegion("서울");
        RegionEntity busan = saveRegion("부산");

        saveExternalPlace("서울 자연 장소", nature, seoul);
        saveExternalPlace("서울 음식 장소", food, seoul);
        saveExternalPlace("부산 자연 장소", nature, busan);
        saveCustomPlace("커스텀 장소");

        AdminPlacePageRequest request = new AdminPlacePageRequest(
                1,
                10,
                seoul.getId(),
                nature.getId(),
                null
        );
        Page<PlaceEntity> page = placeFindService.findPagePlaces(
                request,
                PageRequest.of(request.page() - 1, request.size())
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(titles(page)).containsExactly("서울 자연 장소");
    }

    private CategoryEntity saveCategory(String name, String code) {
        return categoryRepository.saveAndFlush(CategoryEntity.builder()
                .name(name)
                .code(code)
                .iconUrl("https://example.com/" + code + ".png")
                .build());
    }

    private RegionEntity saveRegion(String name) {
        return regionRepository.saveAndFlush(new RegionEntity(name, null, RegionLevel.SIDO));
    }

    private void saveExternalPlace(String title, CategoryEntity category, RegionEntity region) {
        externalPlaceRepository.saveAndFlush(ExternalPlaceEntity.builder()
                .title(title)
                .description(title + " 설명")
                .category(category)
                .region(region)
                .address(title + " 주소")
                .latitude(37.0)
                .longitude(127.0)
                .copyright("테스트")
                .build());
    }

    private void saveCustomPlace(String title) {
        customPlaceRepository.saveAndFlush(CustomPlaceEntity.builder()
                .title(title)
                .address(title + " 주소")
                .latitude(37.0)
                .longitude(127.0)
                .copyright("테스트")
                .build());
    }

    private List<String> titles(Page<PlaceEntity> page) {
        return page.getContent().stream()
                .map(PlaceEntity::getTitle)
                .toList();
    }
}
