package com.ecec.ingredient.domain.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;

import java.io.Serializable;
import java.util.Objects;

public record IngredientCategoryId(Long value) implements Serializable {
    public IngredientCategoryId {
        Objects.requireNonNull(value, "ID must not be null");
    }

    public static IngredientCategoryId of(Long value) {
        return new IngredientCategoryId(value);
    }
}
