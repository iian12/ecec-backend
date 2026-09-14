package com.ecec.ingredient.infrastructure.persistence.alias;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.ingredient.IngredientId;

import com.ecec.global.id.AssignedIdEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ingredient_aliases", indexes = {
        @Index(name = "idx_ingredient_aliases_order", columnList = "ingredient_id, sort_order, id")
})
public class IngredientAliaseEntity extends AssignedIdEntity {
    @Version
    private Long version;
    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @Column(name = "alias", nullable = false)
    private String alias;

    // 목록 순서는 PK와 분리해 어드민에서 자유롭게 변경할 수 있다.
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    public IngredientAliaseEntity(Long id, Long ingredientId, String alias, int sortOrder) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.alias = alias;
        this.sortOrder = sortOrder;
    }

    void update(Long ingredientId, String alias, int sortOrder) {
        this.ingredientId = ingredientId;
        this.alias = alias;
        this.sortOrder = sortOrder;
    }
}
