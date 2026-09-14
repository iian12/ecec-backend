package com.ecec.ingredient.infrastructure.persistence.ingredient;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.alias.IngredientAliase;
import com.ecec.ingredient.domain.alias.IngredientAliaseId;
import com.ecec.ingredient.domain.alias.IngredientAliaseRepository;
import com.ecec.ingredient.domain.category.IngredientCategory;
import com.ecec.ingredient.domain.category.IngredientCategoryId;
import com.ecec.ingredient.domain.category.IngredientCategoryRepository;
import com.ecec.ingredient.domain.ingredient.IngredientId;
import com.ecec.ingredient.application.IngredientOrderService;
import com.ecec.ingredient.domain.ingredient.IngredientRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientPersistenceTest {
    @Autowired IngredientRepository ingredients;
    @Autowired IngredientCategoryRepository categories;
    @Autowired IngredientAliaseRepository aliases;
    @Autowired IngredientOrderService orders;
    @Autowired IngredientJpaRepository ingredientJpa;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    private final IngredientCategoryId categoryId = IngredientCategoryId.of(100L);

    private Ingredient ingredient(long id, int order) {
        return Ingredient.create(IngredientId.of(id), "재료" + id, categoryId, "https://example.com/icon.png", order);
    }

    private void reload() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void ingredientsAreSortedByOrderThenIdAndFilteredByCategory() {
        ingredients.save(ingredient(3L, 1));
        ingredients.save(ingredient(2L, 0));
        ingredients.save(ingredient(1L, 0));
        ingredients.save(Ingredient.create(IngredientId.of(4L), "다른 그룹", IngredientCategoryId.of(200L), null, 0));
        reload();
        assertThat(ingredients.findAll()).extracting(value -> value.getId().value()).containsExactly(1L, 2L, 4L, 3L);
        assertThat(ingredients.findByCategoryId(categoryId)).extracting(value -> value.getId().value()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void categoriesSeparateRootAndChildGroupsAndKeepStableOrder() {
        categories.save(IngredientCategory.create(IngredientCategoryId.of(2L), "과일", null, 0));
        categories.save(IngredientCategory.create(IngredientCategoryId.of(1L), "채소", null, 0));
        categories.save(IngredientCategory.create(IngredientCategoryId.of(3L), "뿌리채소", IngredientCategoryId.of(1L), 0));
        categories.save(IngredientCategory.create(IngredientCategoryId.of(4L), "잎채소", IngredientCategoryId.of(1L), 2));
        reload();
        assertThat(categories.findByParentCategoryId(null)).extracting(value -> value.getId().value()).containsExactly(1L, 2L);
        assertThat(categories.findByParentCategoryId(IngredientCategoryId.of(1L)))
                .extracting(value -> value.getId().value()).containsExactly(3L, 4L);
        assertThat(categories.findAll()).extracting(value -> value.getId().value()).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void aliasesAreSortedAndIsolatedByIngredient() {
        aliases.save(IngredientAliase.create(IngredientAliaseId.of(3L), IngredientId.of(1L), "별칭3", 1));
        aliases.save(IngredientAliase.create(IngredientAliaseId.of(2L), IngredientId.of(1L), "별칭2", 0));
        aliases.save(IngredientAliase.create(IngredientAliaseId.of(1L), IngredientId.of(1L), "별칭1", 0));
        aliases.save(IngredientAliase.create(IngredientAliaseId.of(4L), IngredientId.of(2L), "다른 재료", 0));
        reload();
        assertThat(aliases.findByIngredientId(IngredientId.of(1L))).extracting(value -> value.getId().value()).containsExactly(1L, 2L, 3L);
        assertThat(aliases.findAll()).extracting(value -> value.getId().value()).containsExactly(1L, 2L, 4L, 3L);
    }

    @Test
    void savingRestoredDomainsUpdatesRowsAndPreservesAllFields() {
        categories.save(IngredientCategory.create(categoryId, "채소", null, 0));
        ingredients.save(ingredient(1L, 0));
        aliases.save(IngredientAliase.create(IngredientAliaseId.of(1L), IngredientId.of(1L), "이전 별칭", 0));
        reload();
        var category = categories.findById(categoryId).orElseThrow();
        category.update("이름 변경", IngredientCategoryId.of(200L));
        category.changeSortOrder(7);
        categories.save(category);
        var ingredient = ingredients.findById(IngredientId.of(1L)).orElseThrow();
        ingredient.update("양파", IngredientCategoryId.of(200L), "https://example.com/new.png");
        ingredient.changeSortOrder(8);
        ingredients.save(ingredient);
        var alias = aliases.findById(IngredientAliaseId.of(1L)).orElseThrow();
        alias.update(IngredientId.of(2L), "어니언");
        alias.changeSortOrder(9);
        aliases.save(alias);
        reload();
        assertThat(categories.findAll()).singleElement().satisfies(value -> {
            assertThat(value.getName()).isEqualTo("이름 변경");
            assertThat(value.getParentCategoryId()).isEqualTo(IngredientCategoryId.of(200L));
            assertThat(value.getSortOrder()).isEqualTo(7);
        });
        assertThat(ingredients.findAll()).singleElement().satisfies(value -> {
            assertThat(value.getName()).isEqualTo("양파");
            assertThat(value.getCategoryId()).isEqualTo(IngredientCategoryId.of(200L));
            assertThat(value.getIconUrl()).isEqualTo("https://example.com/new.png");
            assertThat(value.getSortOrder()).isEqualTo(8);
        });
        assertThat(aliases.findAll()).singleElement().satisfies(value -> {
            assertThat(value.getIngredientId()).isEqualTo(IngredientId.of(2L));
            assertThat(value.getAlias()).isEqualTo("어니언");
            assertThat(value.getSortOrder()).isEqualTo(9);
        });
    }

    @Test
    void orderServicePersistsRequestedSequence() {
        ingredients.save(ingredient(1L, 0));
        ingredients.save(ingredient(2L, 1));
        ingredients.save(ingredient(3L, 2));
        reload();
        orders.reorderIngredients(categoryId, List.of(IngredientId.of(3L), IngredientId.of(1L), IngredientId.of(2L)));
        reload();
        assertThat(ingredients.findByCategoryId(categoryId)).extracting(value -> value.getId().value()).containsExactly(3L, 1L, 2L);
        assertThat(ingredients.findByCategoryId(categoryId)).extracting(Ingredient::getSortOrder).containsExactly(0, 1, 2);
    }

    @Test
    void staleEntityCannotOverwriteNewerOrder() {
        ingredients.save(ingredient(1L, 0));
        reload();
        var stale = entityManager.find(IngredientEntity.class, 1L);
        entityManager.detach(stale);
        var current = entityManager.find(IngredientEntity.class, 1L);
        current.update(current.getName(), current.getCategoryId(), current.getIconUrl(), 5);
        entityManager.flush();
        stale.update(stale.getName(), stale.getCategoryId(), stale.getIconUrl(), 9);
        assertThatThrownBy(() -> entityManager.merge(stale)).isInstanceOf(OptimisticLockException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedTransactionRollsBackTheWholeOrderChange() {
        var transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            ingredients.save(ingredient(91L, 5));
            ingredients.save(ingredient(92L, 8));
        });
        try {
            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
                orders.reorderIngredients(categoryId, List.of(IngredientId.of(92L), IngredientId.of(91L)));
                ingredientJpa.flush();
                throw new IllegalStateException("simulate failure after database writes");
            })).isInstanceOf(IllegalStateException.class);
            assertThat(ingredients.findByCategoryId(categoryId)).extracting(Ingredient::getSortOrder).containsExactly(5, 8);
        } finally {
            transaction.executeWithoutResult(status -> ingredientJpa.deleteAllById(List.of(91L, 92L)));
        }
    }

    @Test
    void unknownAndNullIdsReturnEmpty() {
        assertThat(ingredients.findById(null)).isEmpty();
        assertThat(categories.findById(null)).isEmpty();
        assertThat(aliases.findById(null)).isEmpty();
        assertThat(ingredients.findById(IngredientId.of(999L))).isEmpty();
        assertThat(categories.findById(IngredientCategoryId.of(999L))).isEmpty();
        assertThat(aliases.findById(IngredientAliaseId.of(999L))).isEmpty();
    }
}
