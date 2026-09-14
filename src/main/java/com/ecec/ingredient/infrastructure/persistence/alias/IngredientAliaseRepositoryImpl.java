package com.ecec.ingredient.infrastructure.persistence.alias;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.alias.IngredientAliase;
import com.ecec.ingredient.domain.alias.IngredientAliaseId;
import com.ecec.ingredient.domain.alias.IngredientAliaseRepository;
import com.ecec.ingredient.domain.ingredient.IngredientId;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class IngredientAliaseRepositoryImpl implements IngredientAliaseRepository {
    private final IngredientAliaseJpaRepository jpaRepository;

    public IngredientAliaseRepositoryImpl(IngredientAliaseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public IngredientAliase save(IngredientAliase value) {
        Objects.requireNonNull(value, "value must not be null");
        // 직접 할당한 ID를 사용하므로 기존 엔티티는 조회 후 갱신해 중복 INSERT를 피한다.
        IngredientAliaseEntity entity = jpaRepository.findById(value.getId().value())
                .map(existing -> {
                    IngredientAliaseMapper.updateEntity(value, existing);
                    return existing;
                })
                .orElseGet(() -> IngredientAliaseMapper.toEntity(value));
        return IngredientAliaseMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<IngredientAliase> findById(IngredientAliaseId id) {
        if (id == null) return Optional.empty();
        return jpaRepository.findById(id.value()).map(IngredientAliaseMapper::toDomain);
    }

    @Override
    public List<IngredientAliase> findAll() {
        return jpaRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(IngredientAliaseMapper::toDomain).toList();
    }

    @Override
    public List<IngredientAliase> findByIngredientId(IngredientId ingredientId) {
        Objects.requireNonNull(ingredientId, "ingredientId must not be null");
        return jpaRepository.findByIngredientIdOrderBySortOrderAscIdAsc(
                ingredientId.value()).stream()
                .map(IngredientAliaseMapper::toDomain).toList();
    }
}
