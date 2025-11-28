package com.mealplanner.view.component;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * A styled Table component.
 * Corresponds to table.tsx
 */
public class Table<T> extends TableView<T> {

    public Table() {
        super();
        initialize();
    }

    public Table(ObservableList<T> items) {
        super(items);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("styled-table");
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}

