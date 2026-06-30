package com.chocobi.leafy.place.infra.entity;

import com.chocobi.leafy.global.entity.RegionEntity;
import com.chocobi.leafy.place.vo.ExternalPlaceStatus;
import com.chocobi.leafy.place.vo.ExternalPlaceSource;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "external_place",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source", "external_content_id"})
)
@DiscriminatorValue("EXTERNAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalPlaceEntity extends PlaceEntity{
    @Column
    private String externalContentId;

    @Enumerated(EnumType.STRING)
    private ExternalPlaceSource source;

    private Integer contentTypeId;

    private String largeCategoryCode;

    private String middleCategoryCode;

    private String smallCategoryCode;

    private String externalVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'ACTIVE'")
    private ExternalPlaceStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    private RegionEntity region;

    private String tel;

    @Column(length = 2000)
    private String url;

    @Builder
    public ExternalPlaceEntity(String title, String address, double latitude, double longitude,
                               String copyright, RegionEntity region, String description,
                               CategoryEntity category, String tel, String url,
                               String externalContentId, Integer contentTypeId,
                               String largeCategoryCode, String middleCategoryCode,
                               String smallCategoryCode, String externalVersion,
                               ExternalPlaceStatus status, ExternalPlaceSource source) {
        super(title, address, latitude, longitude, copyright);
        this.description = description;
        this.category = category;
        this.region = region;
        this.tel = tel;
        this.url = url;
        this.externalContentId = externalContentId;
        this.source = source;
        this.contentTypeId = contentTypeId;
        this.largeCategoryCode = largeCategoryCode;
        this.middleCategoryCode = middleCategoryCode;
        this.smallCategoryCode = smallCategoryCode;
        this.externalVersion = externalVersion;
        this.status = status == null ? ExternalPlaceStatus.ACTIVE : status;
    }

    public void update(ExternalPlaceEntity other) {
        super.update(other.getTitle(), other.getAddress(), other.getLatitude(), other.getLongitude(), other.getCopyright());
        this.description = other.description;
        this.category = other.category;
        this.region = other.region;
        this.tel = other.tel;
        this.url = other.url;
    }

    public boolean needsSync(String version, CategoryEntity syncCategory) {
        return status == ExternalPlaceStatus.INACTIVE
                || !java.util.Objects.equals(externalVersion, version)
                || !hasSameCategory(syncCategory);
    }

    private boolean hasSameCategory(CategoryEntity syncCategory) {
        if (category == null || syncCategory == null) {
            return category == syncCategory;
        }
        return java.util.Objects.equals(category.getCode(), syncCategory.getCode());
    }

    public void sync(ExternalPlaceEntity other) {
        super.update(other.getTitle(), other.getAddress(), other.getLatitude(), other.getLongitude(),
                other.getCopyright());
        if (other.description != null) {
            this.description = other.description;
        }
        this.category = other.category;
        this.tel = other.tel;
        if (other.url != null) {
            this.url = other.url;
        }
        this.contentTypeId = other.contentTypeId;
        this.largeCategoryCode = other.largeCategoryCode;
        this.middleCategoryCode = other.middleCategoryCode;
        this.smallCategoryCode = other.smallCategoryCode;
        this.externalVersion = other.externalVersion;
        this.status = ExternalPlaceStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ExternalPlaceStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == ExternalPlaceStatus.ACTIVE;
    }
}
