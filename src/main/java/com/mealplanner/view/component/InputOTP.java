package com.mealplanner.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A One-Time Password (OTP) Input component.
 * Corresponds to input-otp.tsx
 */
public class InputOTP extends HBox {

    private final List<TextField> slots = new ArrayList<>();
    private final int length;

    public InputOTP(int length) {
        this.length = length;
        setSpacing(10);
        setAlignment(Pos.CENTER);
        getStyleClass().add("input-otp-group");

        for (int i = 0; i < length; i++) {
            TextField slot = createSlot(i);
            slots.add(slot);
            getChildren().add(slot);
        }
    }

    private TextField createSlot(int index) {
        TextField slot = new TextField();
        slot.setPrefWidth(40);
        slot.setPrefHeight(40);
        slot.setAlignment(Pos.CENTER);
        slot.getStyleClass().add("input-otp-slot");
        
        // Limit to 1 character
        slot.setTextFormatter(new TextFormatter<>(change -> 
            (change.getControlNewText().length() <= 1) ? change : null));

        // Auto-focus next/prev
        slot.setOnKeyReleased(event -> handleKeyRelease(event, index));

        return slot;
    }

    private void handleKeyRelease(KeyEvent event, int index) {
        TextField current = slots.get(index);
        String text = current.getText();

        if (text.length() == 1 && index < length - 1) {
            slots.get(index + 1).requestFocus();
        }
        
        // Backspace handling
        if (event.getCode().toString().equals("BACK_SPACE") && text.isEmpty() && index > 0) {
            slots.get(index - 1).requestFocus();
        }
    }

    public String getValue() {
        StringBuilder sb = new StringBuilder();
        for (TextField slot : slots) {
            sb.append(slot.getText());
        }
        return sb.toString();
    }
}

