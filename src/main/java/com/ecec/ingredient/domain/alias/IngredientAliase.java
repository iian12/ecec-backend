package com.ecec.ingredient.domain.alias;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.ingredient.IngredientId;

import lombok.Getter;
import java.util.Objects;

@Getter
public class IngredientAliase {
    private final IngredientAliaseId id;
    private IngredientId ingredientId;
    private String alias;
    private int sortOrder;

    private IngredientAliase(IngredientAliaseId id, IngredientId ingredientId, String alias, int sortOrder) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        update(ingredientId, alias);
        changeSortOrder(sortOrder);
    }

    public static IngredientAliase create(IngredientAliaseId id, IngredientId ingredientId, String alias, int sortOrder) {
        return new IngredientAliase(id, ingredientId, alias, sortOrder);
    }

    public static IngredientAliase restore(IngredientAliaseId id, IngredientId ingredientId, String alias, int sortOrder) {
        return new IngredientAliase(id, ingredientId, alias, sortOrder);
    }

    public void update(IngredientId ingredientId, String alias) {
        // 잘못된 별칭 입력 때문에 소속 재료만 먼저 바뀌는 것을 방지한다.
        Objects.requireNonNull(ingredientId, "ingredientId must not be null");
        requireText(alias, "alias");
        this.ingredientId = ingredientId;
        this.alias = alias;
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
