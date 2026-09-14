package com.ecec.ingredient.domain.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;

import lombok.Getter;
import java.util.Objects;

@Getter
public class IngredientCategory {
    private final IngredientCategoryId id;
    private String name;
    private IngredientCategoryId parentCategoryId;
    private int sortOrder;

    private IngredientCategory(IngredientCategoryId id, String name, IngredientCategoryId parentCategoryId, int sortOrder) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        update(name, parentCategoryId);
        changeSortOrder(sortOrder);
    }

    public static IngredientCategory create(IngredientCategoryId id, String name, IngredientCategoryId parentCategoryId, int sortOrder) {
        return new IngredientCategory(id, name, parentCategoryId, sortOrder);
    }

    public static IngredientCategory restore(IngredientCategoryId id, String name, IngredientCategoryId parentCategoryId, int sortOrder) {
        return new IngredientCategory(id, name, parentCategoryId, sortOrder);
    }

    public void update(String name, IngredientCategoryId parentCategoryId) {
        if (id.equals(parentCategoryId)) {
            throw new IllegalArgumentException("A category cannot be its own parent");
        }
        this.name = requireText(name, "name");
        this.parentCategoryId = parentCategoryId;
    }

    // 같은 그룹 안에서 작은 값이 먼저 노출된다. 동일한 값은 ID로 순서를 결정한다.
    public void changeSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new IllegalArgumentException("sortOrder must not be negative");
        }
        this.sortOrder = sortOrder;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank() || value.length() > 255) {
            throw new IllegalArgumentException(field + " must contain 1 to 255 characters");
        }
        return value;
    }
}
