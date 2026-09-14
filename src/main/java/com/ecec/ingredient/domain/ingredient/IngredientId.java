package com.ecec.ingredient.domain.ingredient;



import java.io.Serializable;
import java.util.Objects;

public record IngredientId(Long value) implements Serializable {
    public IngredientId {
        Objects.requireNonNull(value, "ID must not be null");
    }

    public static IngredientId of(Long value) {
        return new IngredientId(value);
    }
}
