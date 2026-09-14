package com.ecec.ingredient.infrastructure.persistence.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.category.IngredientCategory;
import com.ecec.ingredient.domain.category.IngredientCategoryId;

public final class IngredientCategoryMapper {
    private IngredientCategoryMapper() {}

    public static IngredientCategoryEntity toEntity(IngredientCategory domain) {
        return IngredientCategoryEntity.builder()
                .id(domain.getId().value())
                .name(domain.getName())
                .parentCategoryId(domain.getParentCategoryId() == null ? null : domain.getParentCategoryId().value())
                .sortOrder(domain.getSortOrder())
                .build();
    }

    public static IngredientCategory toDomain(IngredientCategoryEntity entity) {
        return IngredientCategory.restore(IngredientCategoryId.of(entity.getId()),
                entity.getName(),
                entity.getParentCategoryId() == null ? null : IngredientCategoryId.of(entity.getParentCategoryId()),
                entity.getSortOrder());
    }

    public static void updateEntity(IngredientCategory domain, IngredientCategoryEntity entity) {
        entity.update(domain.getName(),
                domain.getParentCategoryId() == null ? null : domain.getParentCategoryId().value(),
                domain.getSortOrder());
    }
}
