package com.ecec.ingredient.infrastructure.persistence.ingredient;

import com.ecec.global.id.AssignedIdEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ingredient", indexes = {
        @Index(name = "idx_ingredient_order", columnList = "category_id, sort_order, id")
})
public class IngredientEntity extends AssignedIdEntity {
    // 동시에 같은 항목을 편집하면 오래된 변경을 덮어쓰지 않고 충돌로 처리한다.
    @Version
    private Long version;
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "icon_url", nullable = true)
    private String iconKey;

    // 목록 순서는 PK와 분리해 어드민에서 자유롭게 변경할 수 있다.
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    public IngredientEntity(Long id, String name, Long categoryId, String iconKey, int sortOrder) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.iconKey = iconKey;
        this.sortOrder = sortOrder;
    }

    void update(String name, Long categoryId, String iconUrl, int sortOrder) {
        this.name = name;
        this.categoryId = categoryId;
        this.iconKey = iconUrl;
        this.sortOrder = sortOrder;
    }
}
