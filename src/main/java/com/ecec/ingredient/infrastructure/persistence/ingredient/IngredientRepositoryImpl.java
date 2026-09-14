package com.ecec.ingredient.infrastructure.persistence.ingredient;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.category.IngredientCategoryId;
import com.ecec.ingredient.domain.ingredient.IngredientId;
import com.ecec.ingredient.domain.ingredient.IngredientRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class IngredientRepositoryImpl implements IngredientRepository {
    private final IngredientJpaRepository jpaRepository;

    public IngredientRepositoryImpl(IngredientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Ingredient save(Ingredient value) {
        Objects.requireNonNull(value, "value must not be null");
        // 직접 할당한 ID를 사용하므로 기존 엔티티는 조회 후 갱신해 중복 INSERT를 피한다.
        IngredientEntity entity = jpaRepository.findById(value.getId().value())
                .map(existing -> {
                    IngredientMapper.updateEntity(value, existing);
                    return existing;
                })
                .orElseGet(() -> IngredientMapper.toEntity(value));
        return IngredientMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Ingredient> findById(IngredientId id) {
        if (id == null) return Optional.empty();
        return jpaRepository.findById(id.value()).map(IngredientMapper::toDomain);
    }

    @Override
    public List<Ingredient> findAll() {
        return jpaRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(IngredientMapper::toDomain).toList();
    }

    @Override
    public List<Ingredient> findByCategoryId(IngredientCategoryId categoryId) {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        return jpaRepository.findByCategoryIdOrderBySortOrderAscIdAsc(
                categoryId.value()).stream()
                .map(IngredientMapper::toDomain).toList();
    }
}
