package com.mealplanner.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

/**
 * A styled Pagination component.
 * Corresponds to pagination.tsx
 */
public class Pagination extends HBox {

    private final int totalPages;
    private int currentPage;
    private final Consumer<Integer> onPageChange;

    public Pagination(int totalPages, int initialPage, Consumer<Integer> onPageChange) {
        this.totalPages = totalPages;
        this.currentPage = initialPage;
        this.onPageChange = onPageChange;

        setSpacing(5);
        setAlignment(Pos.CENTER);
        getStyleClass().add("pagination");

        render();
    }

    private void render() {
        getChildren().clear();

        // Previous Button
        Button prevBtn = new Button("< Prev");
        prevBtn.getStyleClass().add("pagination-button");
        prevBtn.setDisable(currentPage <= 1);
        prevBtn.setOnAction(e -> changePage(currentPage - 1));
        getChildren().add(prevBtn);

        // Page Numbers (Simplified logic for brevity)
        // In a full implementation, you'd handle ellipsis (...) for many pages
        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add("pagination-button");
            if (i == currentPage) {
                pageBtn.getStyleClass().add("active");
            } else {
                pageBtn.setOnAction(e -> changePage(pageNum));
            }
            getChildren().add(pageBtn);
        }

        // Next Button
        Button nextBtn = new Button("Next >");
        nextBtn.getStyleClass().add("pagination-button");
        nextBtn.setDisable(currentPage >= totalPages);
        nextBtn.setOnAction(e -> changePage(currentPage + 1));
        getChildren().add(nextBtn);
    }

    private void changePage(int newPage) {
        if (newPage >= 1 && newPage <= totalPages) {
            currentPage = newPage;
            render();
            if (onPageChange != null) {
                onPageChange.accept(currentPage);
            }
        }
    }
}

