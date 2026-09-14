package com.ecec.ingredient.domain.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientCategoryRepository {
    IngredientCategory save(IngredientCategory value);
    Optional<IngredientCategory> findById(IngredientCategoryId id);
    List<IngredientCategory> findAll();
    // parentCategoryId가 null이면 최상위 카테고리를 조회한다.
    List<IngredientCategory> findByParentCategoryId(IngredientCategoryId parentCategoryId);
}
