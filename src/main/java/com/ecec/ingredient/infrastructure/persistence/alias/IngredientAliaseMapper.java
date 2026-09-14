package com.ecec.ingredient.infrastructure.persistence.alias;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.alias.IngredientAliase;
import com.ecec.ingredient.domain.alias.IngredientAliaseId;
import com.ecec.ingredient.domain.ingredient.IngredientId;

public final class IngredientAliaseMapper {
    private IngredientAliaseMapper() {}

    public static IngredientAliaseEntity toEntity(IngredientAliase domain) {
        return IngredientAliaseEntity.builder()
                .id(domain.getId().value())
                .ingredientId(domain.getIngredientId().value())
                .alias(domain.getAlias())
                .sortOrder(domain.getSortOrder())
                .build();
    }

    public static IngredientAliase toDomain(IngredientAliaseEntity entity) {
        return IngredientAliase.restore(IngredientAliaseId.of(entity.getId()),
                IngredientId.of(entity.getIngredientId()),
                entity.getAlias(),
                entity.getSortOrder());
    }

    public static void updateEntity(IngredientAliase domain, IngredientAliaseEntity entity) {
        entity.update(domain.getIngredientId().value(),
                domain.getAlias(),
                domain.getSortOrder());
    }
}
