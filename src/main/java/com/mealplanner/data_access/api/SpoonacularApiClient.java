package com.mealplanner.data_access.api;


// API client for Spoonacular API - handles recipe search and nutrition data retrieval.
// Responsible: Everyone (API integration shared responsibility)
// TODO: Implement methods for searchByIngredients, getRecipeById, getNutritionInfo - parse JSON responses to Recipe entities

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpoonacularApiClient {
    private static final String API_KEY = "e7d3d0daf46c44faad53de6c006ba563";
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/complexSearch";

    public List<String> searchRecipes(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlString = BASE_URL + "?query=" + encodedQuery
                + "&number=10"
                + "&apiKey=" + API_KEY;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("API error: HTTP " + status);
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();

            // For now, just parse out recipe titles
            return extractTitlesFromJson(json);
        } finally {
            conn.disconnect();
        }
    }

    private List<String> extractTitlesFromJson(String json) {
        List<String> titles = new ArrayList<>();

        // the JSON looks like: {"results":[{"id":..,"title":"..."}, ...], ...}
        int index = 0;
        while (true) {
            int titlePos = json.indexOf("\"title\":", index);
            if (titlePos == -1) break;
            int firstQuote = json.indexOf('"', titlePos + 8);
            int secondQuote = json.indexOf('"', firstQuote + 1);
            if (firstQuote == -1 || secondQuote == -1) break;

            String title = json.substring(firstQuote + 1, secondQuote);
            titles.add(title);
            index = secondQuote + 1;
        }

        return titles;
    }
}
