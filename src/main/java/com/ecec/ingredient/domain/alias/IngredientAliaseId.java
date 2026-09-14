package com.ecec.ingredient.domain.alias;

import com.ecec.ingredient.domain.ingredient.Ingredient;

import java.io.Serializable;
import java.util.Objects;

public record IngredientAliaseId(Long value) implements Serializable {
    public IngredientAliaseId {
        Objects.requireNonNull(value, "ID must not be null");
    }

    public static IngredientAliaseId of(Long value) {
        return new IngredientAliaseId(value);
    }
}
