package com.mealplanner.view.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

/**
 * Wrapper around JavaFX DatePicker to mimic Calendar component.
 * Corresponds to calendar.tsx
 */
public class Calendar extends VBox {
    
    private final DatePicker datePicker;

    public Calendar() {
        datePicker = new DatePicker();
        datePicker.setShowWeekNumbers(false);
        datePicker.getStyleClass().add("calendar-picker");
        
        getChildren().add(datePicker);
        getStyleClass().add("calendar");
    }
    
    public DatePicker getDatePicker() {
        return datePicker;
    }

    public void setValue(LocalDate date) {
        datePicker.setValue(date);
    }

    public LocalDate getValue() {
        return datePicker.getValue();
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        datePicker.setOnAction(value);
    }
}
