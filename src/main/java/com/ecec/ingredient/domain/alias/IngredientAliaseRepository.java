package com.ecec.ingredient.domain.alias;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.ingredient.IngredientId;

import java.util.List;
import java.util.Optional;

public interface IngredientAliaseRepository {
    IngredientAliase save(IngredientAliase value);
    Optional<IngredientAliase> findById(IngredientAliaseId id);
    List<IngredientAliase> findAll();
    List<IngredientAliase> findByIngredientId(IngredientId ingredientId);
}
