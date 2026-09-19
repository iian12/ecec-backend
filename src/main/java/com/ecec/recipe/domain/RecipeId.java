package com.ecec.recipe.domain;

import java.io.Serializable;

public record RecipeId(Long value) implements Serializable {

    public RecipeId {
        if (value == null) {
            throw new IllegalArgumentException("RecipeId value cannot be null");
        }
    }

    public static RecipeId of(Long value) {
        return new RecipeId(value);
    }
}
