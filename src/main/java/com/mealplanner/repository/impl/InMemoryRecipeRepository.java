package com.mealplanner.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mealplanner.entity.Recipe;
import com.mealplanner.exception.DataAccessException;
import com.mealplanner.repository.RecipeRepository;

/**
 * Simple in-memory RecipeRepository implementation for demos and tests.
 * Not intended for production use.
 */
public class InMemoryRecipeRepository implements RecipeRepository {

    private final ConcurrentMap<String, Recipe> store = new ConcurrentHashMap<>();

    @Override
    public void save(Recipe recipe) throws DataAccessException {
        if (recipe == null) throw new DataAccessException("Cannot save null recipe");
        // Use recipe name as key for this simple demo (assumes names are unique)
        store.put(recipe.getName(), recipe);
    }

    @Override
    public Optional<Recipe> findById(String recipeId) throws DataAccessException {
        if (recipeId == null) return Optional.empty();
        return Optional.ofNullable(store.get(recipeId));
    }

    @Override
    public List<Recipe> findAll() throws DataAccessException {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Recipe> findByName(String name) throws DataAccessException {
        if (name == null) return Collections.emptyList();
        List<Recipe> result = new ArrayList<>();
        String lower = name.toLowerCase();
        for (Recipe r : store.values()) {
            if (r.getName() != null && r.getName().toLowerCase().contains(lower)) result.add(r);
        }
        return result;
    }

    @Override
    public boolean delete(String recipeId) throws DataAccessException {
        return store.remove(recipeId) != null;
    }

    @Override
    public boolean exists(String recipeId) throws DataAccessException {
        return store.containsKey(recipeId);
    }

    @Override
    public int count() throws DataAccessException {
        return store.size();
    }

    @Override
    public void clear() throws DataAccessException {
        store.clear();
    }
}
