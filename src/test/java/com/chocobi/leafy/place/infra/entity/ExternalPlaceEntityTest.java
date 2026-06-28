package com.chocobi.leafy.place.infra.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExternalPlaceEntityTest {
    @Test
    @DisplayName("외부 데이터 버전이 같아도 카테고리가 바뀌면 동기화한다")
    void needsSyncWhenCategoryChanges() {
        CategoryEntity nature = category("NATURE", "자연");
        CategoryEntity experience = category("EXPERIENCE", "체험");
        ExternalPlaceEntity place = ExternalPlaceEntity.builder()
                .title("장소")
                .address("서울")
                .latitude(37.5)
                .longitude(126.9)
                .category(nature)
                .externalVersion("same-version")
                .status(ExternalPlaceStatus.ACTIVE)
                .build();

        assertThat(place.needsSync("same-version", nature)).isFalse();
        assertThat(place.needsSync("same-version", experience)).isTrue();
    }

    private CategoryEntity category(String code, String name) {
        return CategoryEntity.builder()
                .code(code)
                .name(name)
                .build();
    }
}
