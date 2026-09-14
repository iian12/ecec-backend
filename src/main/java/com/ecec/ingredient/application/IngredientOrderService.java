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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class IngredientOrderService {
    private final IngredientCategoryRepository categories;
    private final IngredientRepository ingredients;
    private final IngredientAliaseRepository aliases;

    public IngredientOrderService(IngredientCategoryRepository categories,
                                 IngredientRepository ingredients,
                                 IngredientAliaseRepository aliases) {
        this.categories = categories;
        this.ingredients = ingredients;
        this.aliases = aliases;
    }

    // 어드민은 같은 부모 아래의 전체 ID를 원하는 순서대로 전달한다. null 부모는 최상위다.
    public void reorderCategories(IngredientCategoryId parentId, List<IngredientCategoryId> orderedIds) {
        reorder(categories.findByParentCategoryId(parentId), orderedIds,
                IngredientCategory::getId, IngredientCategory::changeSortOrder, categories::save);
    }

    public void reorderIngredients(IngredientCategoryId categoryId, List<IngredientId> orderedIds) {
        reorder(ingredients.findByCategoryId(categoryId), orderedIds,
                Ingredient::getId, Ingredient::changeSortOrder, ingredients::save);
    }

    public void reorderAliases(IngredientId ingredientId, List<IngredientAliaseId> orderedIds) {
        reorder(aliases.findByIngredientId(ingredientId), orderedIds,
                IngredientAliase::getId, IngredientAliase::changeSortOrder, aliases::save);
    }

    private <T, ID> void reorder(List<T> values, List<ID> orderedIds, Function<T, ID> idOf,
                                BiConsumer<T, Integer> changeOrder, Consumer<T> save) {
        if (orderedIds == null || orderedIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("orderedIds must not be null or contain null");
        }
        Map<ID, T> byId = values.stream().collect(Collectors.toMap(idOf, Function.identity()));
        if (orderedIds.size() != byId.size() || !new HashSet<>(orderedIds).equals(byId.keySet())) {
            throw new IllegalArgumentException("orderedIds must contain every ID in the group exactly once");
        }
        // 일부만 저장되는 것을 막고, 화면에 전달된 순서를 0부터 연속된 값으로 반영한다.
        for (int index = 0; index < orderedIds.size(); index++) {
            T value = byId.get(orderedIds.get(index));
            changeOrder.accept(value, index);
            save.accept(value);
        }
    }
}
