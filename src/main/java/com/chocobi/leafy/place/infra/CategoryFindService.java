package com.chocobi.leafy.place.infra;

import com.chocobi.leafy.global.exception.CustomException;
import com.chocobi.leafy.place.infra.entity.CategoryEntity;
import com.chocobi.leafy.place.infra.repository.CategoryRepository;
import com.chocobi.leafy.place.vo.PlaceError;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryFindService {
    private final CategoryRepository categoryRepository;

    public CategoryEntity findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(PlaceError.CATEGORY_NOT_FOUND));
    }

    public List<CategoryEntity> findCategories(Collection<String> codes) {
        List<CategoryEntity> categories = categoryRepository.findAllByCodeIn(codes);
        if (categories.size() != codes.size()) {
            throw new CustomException(PlaceError.CATEGORY_NOT_FOUND);
        }
        return categories;
    }
}
