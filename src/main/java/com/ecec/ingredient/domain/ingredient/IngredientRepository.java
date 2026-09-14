package com.ecec.ingredient.domain.ingredient;

import com.ecec.ingredient.domain.category.IngredientCategoryId;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository {
    Ingredient save(Ingredient value);
    Optional<Ingredient> findById(IngredientId id);
    List<Ingredient> findAll();
    List<Ingredient> findByCategoryId(IngredientCategoryId categoryId);
}
