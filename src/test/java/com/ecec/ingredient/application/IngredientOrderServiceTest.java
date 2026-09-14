package com.ecec.ingredient.application;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.alias.IngredientAliase;
import com.ecec.ingredient.domain.alias.IngredientAliaseId;
import com.ecec.ingredient.domain.alias.IngredientAliaseRepository;
import com.ecec.ingredient.domain.category.IngredientCategory;
import com.ecec.ingredient.domain.category.IngredientCategoryId;
import com.ecec.ingredient.domain.category.IngredientCategoryRepository;
import com.ecec.ingredient.domain.ingredient.IngredientId;
import com.ecec.ingredient.domain.ingredient.IngredientRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngredientOrderServiceTest {
    private final IngredientCategoryRepository categories = mock(IngredientCategoryRepository.class);
    private final IngredientRepository ingredients = mock(IngredientRepository.class);
    private final IngredientAliaseRepository aliases = mock(IngredientAliaseRepository.class);
    private final IngredientOrderService service = new IngredientOrderService(categories, ingredients, aliases);
    private final IngredientCategoryId categoryId = IngredientCategoryId.of(10L);

    static Stream<List<IngredientId>> invalidOrders() {
        var first = IngredientId.of(1L);
        var second = IngredientId.of(2L);
        return Stream.of(null, Arrays.asList(first, null), List.of(first, first), List.of(first),
                List.of(first, IngredientId.of(999L)), List.of(first, second, IngredientId.of(999L)), List.of());
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    void rejectsInvalidOrdersBeforeAnyMutationOrSave(List<IngredientId> ids) {
        var first = Ingredient.create(IngredientId.of(1L), "양파", categoryId, null, 5);
        var second = Ingredient.create(IngredientId.of(2L), "감자", categoryId, null, 8);
        when(ingredients.findByCategoryId(categoryId)).thenReturn(List.of(first, second));
        assertThatIllegalArgumentException().isThrownBy(() -> service.reorderIngredients(categoryId, ids));
        verify(ingredients, never()).save(any());
        assertThat(first.getSortOrder()).isEqualTo(5);
        assertThat(second.getSortOrder()).isEqualTo(8);
    }

    @Test
    void reordersIngredientsWithinCategory() {
        var first = Ingredient.create(IngredientId.of(1L), "양파", categoryId, null, 5);
        var second = Ingredient.create(IngredientId.of(2L), "감자", categoryId, null, 8);
        when(ingredients.findByCategoryId(categoryId)).thenReturn(List.of(first, second));
        service.reorderIngredients(categoryId, List.of(second.getId(), first.getId()));
        assertThat(second.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isEqualTo(1);
        verify(ingredients).save(first);
        verify(ingredients).save(second);
        verifyNoInteractions(categories, aliases);
    }

    @Test
    void reordersRootCategories() {
        var first = IngredientCategory.create(categoryId, "채소", null, 8);
        var second = IngredientCategory.create(IngredientCategoryId.of(11L), "과일", null, 9);
        when(categories.findByParentCategoryId(null)).thenReturn(List.of(first, second));
        service.reorderCategories(null, List.of(second.getId(), first.getId()));
        assertThat(second.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isEqualTo(1);
        verify(categories).save(first);
        verify(categories).save(second);
    }

    @Test
    void reordersAliasesWithinIngredient() {
        var ingredient = IngredientId.of(1L);
        var first = IngredientAliase.create(IngredientAliaseId.of(1L), ingredient, "양파", 8);
        var second = IngredientAliase.create(IngredientAliaseId.of(2L), ingredient, "어니언", 9);
        when(aliases.findByIngredientId(ingredient)).thenReturn(List.of(first, second));
        service.reorderAliases(ingredient, List.of(second.getId(), first.getId()));
        assertThat(second.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isEqualTo(1);
        verify(aliases).save(first);
        verify(aliases).save(second);
    }

    @Test
    void emptyGroupAcceptsEmptyOrder() {
        when(ingredients.findByCategoryId(categoryId)).thenReturn(List.of());
        service.reorderIngredients(categoryId, List.of());
        verify(ingredients, never()).save(any());
    }
}
