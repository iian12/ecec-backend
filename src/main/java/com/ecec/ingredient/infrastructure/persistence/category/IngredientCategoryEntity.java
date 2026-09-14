package com.ecec.ingredient.infrastructure.persistence.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;

import com.ecec.global.id.AssignedIdEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ingredient_categories", indexes = {
        @Index(name = "idx_ingredient_categories_order", columnList = "parent_category_id, sort_order, id")
})
public class IngredientCategoryEntity extends AssignedIdEntity {
    @Version
    private Long version;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "parent_category_id", nullable = true)
    private Long parentCategoryId;

    // 목록 순서는 PK와 분리해 어드민에서 자유롭게 변경할 수 있다.
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    public IngredientCategoryEntity(Long id, String name, Long parentCategoryId, int sortOrder) {
        this.id = id;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.sortOrder = sortOrder;
    }

    void update(String name, Long parentCategoryId, int sortOrder) {
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.sortOrder = sortOrder;
    }
}
