package com.ecec.ingredient.domain.ingredient;

import com.ecec.ingredient.domain.category.IngredientCategoryId;

import lombok.Getter;
import java.util.Objects;

@Getter
public class Ingredient {
    private final IngredientId id;
    private String name;
    private IngredientCategoryId categoryId;
    private String iconKey;
    private int sortOrder;

    private Ingredient(IngredientId id, String name, IngredientCategoryId categoryId, String iconKey, int sortOrder) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        update(name, categoryId, iconKey);
        changeSortOrder(sortOrder);
    }

    public static Ingredient create(IngredientId id, String name, IngredientCategoryId categoryId, String iconUrl, int sortOrder) {
        return new Ingredient(id, name, categoryId, iconUrl, sortOrder);
    }

    public static Ingredient restore(IngredientId id, String name, IngredientCategoryId categoryId, String iconUrl, int sortOrder) {
        return new Ingredient(id, name, categoryId, iconUrl, sortOrder);
    }

    public void update(String name, IngredientCategoryId categoryId, String iconUrl) {
        // 검증 실패 시 기존 상태가 일부만 변경되지 않도록 모든 입력을 먼저 검증한다.
        requireText(name, "name");
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        this.name = name;
        this.categoryId = categoryId;
        this.iconKey = iconUrl;
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
