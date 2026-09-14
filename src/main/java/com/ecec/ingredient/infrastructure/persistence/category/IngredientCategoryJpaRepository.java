package com.ecec.ingredient.infrastructure.persistence.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IngredientCategoryJpaRepository extends JpaRepository<IngredientCategoryEntity, Long> {
    // List만으로 DB 결과 순서가 보장되지 않으므로 조회 쿼리에 정렬을 명시한다.
    List<IngredientCategoryEntity> findAllByOrderBySortOrderAscIdAsc();
    List<IngredientCategoryEntity> findByParentCategoryIdOrderBySortOrderAscIdAsc(Long parentCategoryId);
}
