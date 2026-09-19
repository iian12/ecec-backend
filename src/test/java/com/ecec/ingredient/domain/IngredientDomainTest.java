package com.ecec.ingredient.domain;

import com.ecec.ingredient.domain.ingredient.Ingredient;
import com.ecec.ingredient.domain.alias.IngredientAliase;
import com.ecec.ingredient.domain.alias.IngredientAliaseId;
import com.ecec.ingredient.domain.category.IngredientCategory;
import com.ecec.ingredient.domain.category.IngredientCategoryId;
import com.ecec.ingredient.domain.ingredient.IngredientId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class IngredientDomainTest {
    private final IngredientCategoryId categoryId = IngredientCategoryId.of(10L);
    private final IngredientId ingredientId = IngredientId.of(20L);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsBlankNamesAndAliases(String text) {
        assertThatIllegalArgumentException().isThrownBy(() -> Ingredient.create(ingredientId, text, categoryId, null, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> IngredientCategory.create(categoryId, text, null, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> IngredientAliase.create(IngredientAliaseId.of(30L), ingredientId, text, 0));
    }

    @Test
    void enforcesNameLengthBoundary() {
        String allowed = "가".repeat(255);
        assertThat(Ingredient.create(ingredientId, allowed, categoryId, null, 0).getName()).isEqualTo(allowed);
        assertThat(IngredientCategory.create(categoryId, allowed, null, 0).getName()).isEqualTo(allowed);
        assertThat(IngredientAliase.create(IngredientAliaseId.of(30L), ingredientId, allowed, 0).getAlias()).isEqualTo(allowed);
        assertThatIllegalArgumentException().isThrownBy(() -> Ingredient.create(ingredientId, allowed + "가", categoryId, null, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> IngredientCategory.create(categoryId, allowed + "가", null, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> IngredientAliase.create(IngredientAliaseId.of(30L), ingredientId, allowed + "가", 0));
    }

    @Test
    void categoryAllowsRootButRejectsSelfParentWithoutChangingState() {
        var category = IngredientCategory.create(categoryId, "채소", null, 0);
        assertThat(category.getParentCategoryId()).isNull();
        assertThatIllegalArgumentException().isThrownBy(() -> category.update("다른 이름", categoryId));
        assertThat(category.getName()).isEqualTo("채소");
        assertThat(category.getParentCategoryId()).isNull();
    }

    @Test
    void requiresIngredientCategoryAndAliasIngredient() {
        assertThatNullPointerException().isThrownBy(() -> Ingredient.create(ingredientId, "양파", null, null, 0));
        assertThatNullPointerException().isThrownBy(() -> IngredientAliase.create(IngredientAliaseId.of(30L), null, "양파", 0));
    }

    @Test
    void negativeOrderIsRejectedWithoutChangingExistingOrder() {
        var ingredient = Ingredient.create(ingredientId, "양파", categoryId, null, 5);
        var category = IngredientCategory.create(categoryId, "채소", null, 6);
        var alias = IngredientAliase.create(IngredientAliaseId.of(30L), ingredientId, "어니언", 7);
        assertThatIllegalArgumentException().isThrownBy(() -> ingredient.changeSortOrder(-1));
        assertThatIllegalArgumentException().isThrownBy(() -> category.changeSortOrder(-1));
        assertThatIllegalArgumentException().isThrownBy(() -> alias.changeSortOrder(-1));
        assertThat(ingredient.getSortOrder()).isEqualTo(5);
        assertThat(category.getSortOrder()).isEqualTo(6);
        assertThat(alias.getSortOrder()).isEqualTo(7);
    }

    @Test
    void invalidIngredientUpdateDoesNotPartiallyChangeName() {
        var ingredient = Ingredient.create(ingredientId, "양파", categoryId, "icon.png", 0);
        assertThatNullPointerException().isThrownBy(() -> ingredient.update("감자", null, null));
        assertThat(ingredient.getName()).isEqualTo("양파");
        assertThat(ingredient.getCategoryId()).isEqualTo(categoryId);
        assertThat(ingredient.getIconKey()).isEqualTo("icon.png");
    }

    @Test
    void invalidAliasUpdateDoesNotMoveAliasToAnotherIngredient() {
        var alias = IngredientAliase.create(IngredientAliaseId.of(30L), ingredientId, "어니언", 0);
        assertThatIllegalArgumentException().isThrownBy(() -> alias.update(IngredientId.of(99L), " "));
        assertThat(alias.getIngredientId()).isEqualTo(ingredientId);
        assertThat(alias.getAlias()).isEqualTo("어니언");
    }
}
