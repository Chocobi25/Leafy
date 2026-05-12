package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.repository.CategoryRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryEntity findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.CATEGORY_NOT_FOUND));
    }
}
