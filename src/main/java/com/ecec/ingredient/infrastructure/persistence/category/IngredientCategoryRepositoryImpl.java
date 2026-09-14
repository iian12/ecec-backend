package com.ecec.ingredient.infrastructure.persistence.category;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.category.IngredientCategory;
import com.ecec.ingredient.domain.category.IngredientCategoryId;
import com.ecec.ingredient.domain.category.IngredientCategoryRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class IngredientCategoryRepositoryImpl implements IngredientCategoryRepository {
    private final IngredientCategoryJpaRepository jpaRepository;

    public IngredientCategoryRepositoryImpl(IngredientCategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public IngredientCategory save(IngredientCategory value) {
        Objects.requireNonNull(value, "value must not be null");
        // 직접 할당한 ID를 사용하므로 기존 엔티티는 조회 후 갱신해 중복 INSERT를 피한다.
        IngredientCategoryEntity entity = jpaRepository.findById(value.getId().value())
                .map(existing -> {
                    IngredientCategoryMapper.updateEntity(value, existing);
                    return existing;
                })
                .orElseGet(() -> IngredientCategoryMapper.toEntity(value));
        return IngredientCategoryMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<IngredientCategory> findById(IngredientCategoryId id) {
        if (id == null) return Optional.empty();
        return jpaRepository.findById(id.value()).map(IngredientCategoryMapper::toDomain);
    }

    @Override
    public List<IngredientCategory> findAll() {
        return jpaRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(IngredientCategoryMapper::toDomain).toList();
    }

    @Override
    public List<IngredientCategory> findByParentCategoryId(IngredientCategoryId parentCategoryId) {
        return jpaRepository.findByParentCategoryIdOrderBySortOrderAscIdAsc(
                parentCategoryId == null ? null : parentCategoryId.value()).stream()
                .map(IngredientCategoryMapper::toDomain).toList();
    }
}
