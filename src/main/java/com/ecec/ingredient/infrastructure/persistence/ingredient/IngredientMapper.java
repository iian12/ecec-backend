package com.ecec.ingredient.infrastructure.persistence.ingredient;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.category.IngredientCategoryId;
import com.ecec.ingredient.domain.ingredient.IngredientId;

public final class IngredientMapper {
    private IngredientMapper() {}

    public static IngredientEntity toEntity(Ingredient domain) {
        return IngredientEntity.builder()
                .id(domain.getId().value())
                .name(domain.getName())
                .categoryId(domain.getCategoryId().value())
                .iconUrl(domain.getIconUrl())
                .sortOrder(domain.getSortOrder())
                .build();
    }

    public static Ingredient toDomain(IngredientEntity entity) {
        return Ingredient.restore(IngredientId.of(entity.getId()),
                entity.getName(),
                IngredientCategoryId.of(entity.getCategoryId()),
                entity.getIconUrl(),
                entity.getSortOrder());
    }

    public static void updateEntity(Ingredient domain, IngredientEntity entity) {
        entity.update(domain.getName(),
                domain.getCategoryId().value(),
                domain.getIconUrl(),
                domain.getSortOrder());
    }
}
