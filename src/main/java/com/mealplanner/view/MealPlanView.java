package com.mealplanner.view;

import com.mealplanner.entity.MealType;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.AddMealController;
import com.mealplanner.interface_adapter.controller.DeleteMealController;
import com.mealplanner.interface_adapter.controller.EditMealController;
import com.mealplanner.interface_adapter.view_model.MealPlanViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MealPlanView extends JPanel implements PropertyChangeListener, ActionListener {
    private final MealPlanViewModel mealPlanViewModel;
    private final ViewManagerModel viewManagerModel;
    private final AddMealController addMealController;
    private final EditMealController editMealController;
    private final DeleteMealController deleteMealController;

    private JPanel calendarPanel;
    private JPanel controlPanel;
    private JLabel statusLabel;
    private JComboBox<String> mealTypeComboBox;
    private JTextField recipeIdField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JSpinner dateSpinner;

    public MealPlanView(MealPlanViewModel mealPlanViewModel, ViewManagerModel viewManagerModel,
                        AddMealController addMealController, EditMealController editMealController,
                        DeleteMealController deleteMealController) {
        if (mealPlanViewModel == null) {
            throw new IllegalArgumentException("ViewModel cannot be null");
        }
        if (addMealController == null) {
            throw new IllegalArgumentException("AddMealController cannot be null");
        }
        if (editMealController == null) {
            throw new IllegalArgumentException("EditMealController cannot be null");
        }
        if (deleteMealController == null) {
            throw new IllegalArgumentException("DeleteMealController cannot be null");
        }

        this.mealPlanViewModel = mealPlanViewModel;
        this.viewManagerModel = viewManagerModel;
        this.addMealController = addMealController;
        this.editMealController = editMealController;
        this.deleteMealController = deleteMealController;

        mealPlanViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        if (viewManagerModel != null) {
            add(createNavigationPanel(), BorderLayout.NORTH);
        }

        createControlPanel();
        createCalendarPanel();
        createStatusLabel();

        add(controlPanel, BorderLayout.WEST);
        add(calendarPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));

        JButton scheduleButton = new JButton("View Schedule");
        scheduleButton.addActionListener(e -> viewManagerModel.setActiveView("ScheduleView"));

        JButton browseButton = new JButton("Browse Recipes");
        browseButton.addActionListener(e -> viewManagerModel.setActiveView("BrowseRecipeView"));

        navPanel.add(scheduleButton);
        navPanel.add(browseButton);

        return navPanel;
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(8, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Meal Plan Controls"));

        controlPanel.add(new JLabel("Date:"));
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        controlPanel.add(dateSpinner);

        controlPanel.add(new JLabel("Meal Type:"));
        mealTypeComboBox = new JComboBox<>(new String[]{"BREAKFAST", "LUNCH", "DINNER"});
        controlPanel.add(mealTypeComboBox);

        controlPanel.add(new JLabel("Recipe ID:"));
        recipeIdField = new JTextField();
        controlPanel.add(recipeIdField);

        addButton = new JButton("Add Meal");
        addButton.addActionListener(this);
        addButton.setActionCommand("add");
        controlPanel.add(addButton);

        editButton = new JButton("Edit Meal");
        editButton.addActionListener(this);
        editButton.setActionCommand("edit");
        controlPanel.add(editButton);

        deleteButton = new JButton("Delete Meal");
        deleteButton.addActionListener(this);
        deleteButton.setActionCommand("delete");
        controlPanel.add(deleteButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);
        refreshButton.setActionCommand("refresh");
        controlPanel.add(refreshButton);
    }

    private void createCalendarPanel() {
        calendarPanel = new JPanel();
        calendarPanel.setLayout(new GridLayout(8, 4, 5, 5));
        calendarPanel.setBorder(BorderFactory.createTitledBorder("Weekly Meal Schedule"));

        calendarPanel.add(new JLabel("Date", SwingConstants.CENTER));
        calendarPanel.add(new JLabel("Breakfast", SwingConstants.CENTER));
        calendarPanel.add(new JLabel("Lunch", SwingConstants.CENTER));
        calendarPanel.add(new JLabel("Dinner", SwingConstants.CENTER));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            calendarPanel.add(new JLabel(date.format(DateTimeFormatter.ofPattern("MM/dd")), SwingConstants.CENTER));
            calendarPanel.add(new JLabel("-", SwingConstants.CENTER));
            calendarPanel.add(new JLabel("-", SwingConstants.CENTER));
            calendarPanel.add(new JLabel("-", SwingConstants.CENTER));
        }
    }

    private void createStatusLabel() {
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
    }

    private void updateCalendar() {
        calendarPanel.removeAll();

        calendarPanel.add(new JLabel("Date", SwingConstants.CENTER));
        calendarPanel.add(new JLabel("Breakfast", SwingConstants.CENTER));
        calendarPanel.add(new JLabel("Lunch", SwingConstants.CENTER));
        calendarPanel.add(new JLabel("Dinner", SwingConstants.CENTER));

        Map<LocalDate, Map<MealType, String>> weeklyMeals = mealPlanViewModel.getWeeklyMeals();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            calendarPanel.add(new JLabel(date.format(DateTimeFormatter.ofPattern("MM/dd")), SwingConstants.CENTER));

            Map<MealType, String> mealsForDate = weeklyMeals.get(date);

            String breakfast = "-";
            String lunch = "-";
            String dinner = "-";

            if (mealsForDate != null) {
                breakfast = mealsForDate.getOrDefault(MealType.BREAKFAST, "-");
                lunch = mealsForDate.getOrDefault(MealType.LUNCH, "-");
                dinner = mealsForDate.getOrDefault(MealType.DINNER, "-");
            }

            calendarPanel.add(new JLabel(breakfast, SwingConstants.CENTER));
            calendarPanel.add(new JLabel(lunch, SwingConstants.CENTER));
            calendarPanel.add(new JLabel(dinner, SwingConstants.CENTER));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "add":
                handleAddMeal();
                break;
            case "edit":
                handleEditMeal();
                break;
            case "delete":
                handleDeleteMeal();
                break;
            case "refresh":
                updateCalendar();
                break;
        }
    }

    private void handleAddMeal() {
        String recipeId = recipeIdField.getText().trim();
        if (recipeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a recipe ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String mealTypeStr = (String) mealTypeComboBox.getSelectedItem();
            
            addMealController.execute(date.toString(), mealTypeStr, recipeId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding meal: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEditMeal() {
        String recipeId = recipeIdField.getText().trim();
        if (recipeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a recipe ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String mealTypeStr = (String) mealTypeComboBox.getSelectedItem();
            
            editMealController.execute(date.toString(), mealTypeStr, recipeId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error editing meal: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteMeal() {
        try {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String mealTypeStr = (String) mealTypeComboBox.getSelectedItem();
            
            deleteMealController.execute(date.toString(), mealTypeStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting meal: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "weeklyMeals":
            case "schedule":
                updateCalendar();
                break;
            case "errorMessage":
                String errorMsg = mealPlanViewModel.getErrorMessage();
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    statusLabel.setText(errorMsg);
                    statusLabel.setForeground(Color.RED);
                }
                break;
            case "successMessage":
                String successMsg = mealPlanViewModel.getSuccessMessage();
                if (successMsg != null && !successMsg.isEmpty()) {
                    statusLabel.setText(successMsg);
                    statusLabel.setForeground(Color.GREEN);
                }
                break;
        }
    }
}
