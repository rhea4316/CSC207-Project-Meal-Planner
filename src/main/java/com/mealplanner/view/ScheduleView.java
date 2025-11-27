package com.mealplanner.view;

// Swing view for viewing user's saved meal schedule - displays weekly meal plan.
// Responsible: Mona (functionality), Everyone (GUI implementation)
// Note: Adjust later because I can't test it

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;
import com.mealplanner.interface_adapter.ViewManagerModel;
import com.mealplanner.interface_adapter.controller.ViewScheduleController;
import com.mealplanner.interface_adapter.view_model.ScheduleViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScheduleView extends JPanel implements PropertyChangeListener {

    private final ScheduleViewModel scheduleViewModel;
    private final ViewScheduleController controller;
    private final ViewManagerModel viewManagerModel;

    private final JLabel titleLabel;
    private final JLabel messageLabel;
    private final JTable scheduleTable;
    private final DefaultTableModel tableModel;
    private JButton saveButton;
    private JButton loadButton;

    private JPanel buttonPanel;
    private JPanel headerPanel;

    // To map table rows to dates
    private final List<LocalDate> rowDates = new ArrayList<>();

    public ScheduleView(ScheduleViewModel scheduleViewModel, ViewScheduleController controller, ViewManagerModel viewManagerModel) {
        this.scheduleViewModel = scheduleViewModel;
        this.scheduleViewModel.addPropertyChangeListener(this);
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;

        setLayout(new BorderLayout(10, 10));

        // --- Header (title + message) ---
        headerPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Meal Schedule");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));

        messageLabel = new JLabel(" ");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(messageLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // --- Table ---
        tableModel = new DefaultTableModel(new Object[]{"Date", "Breakfast", "Lunch", "Dinner"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setFillsViewportHeight(true);

        // Double-click to show basic details of a cell (recipe ID for now)
        scheduleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = scheduleTable.getSelectedRow();
                    int col = scheduleTable.getSelectedColumn();
                    if (row >= 0 && col > 0) { // col 0 is date
                        showMealDetails(row, col);
                    }
                }
            }
        });
        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        createButtonPanel();

    }

    private void createButtonPanel() {
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Save Schedule");
        loadButton = new JButton("Load Schedule");

        saveButton.addActionListener(e -> {
            Schedule schedule = scheduleViewModel.getSchedule();
            if (schedule != null) {
                controller.saveSchedule(schedule);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No schedule to save. Please load a schedule first.",
                        "Save Schedule",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        
        loadButton.addActionListener(e -> {
            String username = scheduleViewModel.getUsername();
            if (username == null || username.trim().isEmpty()) {
                // Prompt for username
                username = JOptionPane.showInputDialog(this,
                        "Enter username to load schedule:",
                        "Load Schedule",
                        JOptionPane.QUESTION_MESSAGE);
                if (username == null || username.trim().isEmpty()) {
                    return;
                }
                scheduleViewModel.setUsername(username.trim());
            }
            controller.execute(username);
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showMealDetails(int row, int col) {
        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule == null || row < 0 || row >= rowDates.size()) {
            return;
        }

        LocalDate date = rowDates.get(row);
        Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
        Map<MealType, String> mealsForDate = allMeals.get(date);
        if (mealsForDate == null) {
            JOptionPane.showMessageDialog(this,
                    "No meals scheduled for this date.",
                    "Meal Details",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MealType mealType;
        String mealLabel;
        switch (col) {
            case 1:
                mealType = MealType.BREAKFAST;
                mealLabel = "Breakfast";
                break;
            case 2:
                mealType = MealType.LUNCH;
                mealLabel = "Lunch";
                break;
            case 3:
                mealType = MealType.DINNER;
                mealLabel = "Dinner";
                break;
            default:
                return;
        }

        String recipeId = mealsForDate.get(mealType);
        if (recipeId == null || recipeId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No " + mealLabel.toLowerCase() + " scheduled.",
                    "Meal Details",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            // For now we only know the recipe ID. Later you can look up the Recipe entity by ID.
            JOptionPane.showMessageDialog(this,
                    "Recipe ID: " + recipeId,
                    mealLabel + " on " + date,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // ViewModel fired a change; update the UI
        updateFromViewModel();
    }

    private void updateFromViewModel() {
        // --- Title ---
        String username = scheduleViewModel.getUsername();
        if (username != null && !username.isEmpty()) {
            titleLabel.setText("Meal Schedule for " + username);
        } else {
            titleLabel.setText("Meal Schedule");
        }

        // --- Error / message label ---
        String error = scheduleViewModel.getError();
        if (error != null) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText(error);
        } else {
            messageLabel.setForeground(Color.BLACK);
            messageLabel.setText(" ");
        }

        // --- Table contents ---
        tableModel.setRowCount(0);
        rowDates.clear();

        Schedule schedule = scheduleViewModel.getSchedule();
        if (schedule == null || schedule.isEmpty()) {
            return;
        }

        Map<LocalDate, Map<MealType, String>> allMeals = schedule.getAllMeals();
        if (allMeals == null || allMeals.isEmpty()) {
            return;
        }

        // Sort dates for stable display
        java.util.List<LocalDate> dates = new ArrayList<>(allMeals.keySet());
        Collections.sort(dates);

        for (LocalDate date : dates) {
            Map<MealType, String> mealsForDate = allMeals.get(date);

            String breakfastId = mealsForDate != null ? mealsForDate.getOrDefault(MealType.BREAKFAST, "") : "";
            String lunchId = mealsForDate != null ? mealsForDate.getOrDefault(MealType.LUNCH, "") : "";
            String dinnerId = mealsForDate != null ? mealsForDate.getOrDefault(MealType.DINNER, "") : "";

            rowDates.add(date);
            tableModel.addRow(new Object[]{
                    date,        // LocalDate's toString() is fine
                    breakfastId,
                    lunchId,
                    dinnerId
            });
        }
    }
}
