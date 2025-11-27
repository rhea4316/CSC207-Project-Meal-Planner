package com.mealplanner.view.components;

import com.mealplanner.entity.MealType;
import com.mealplanner.entity.Schedule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// Reusable Swing component for displaying weekly calendar with meal slots.
// Responsible: Grace (primary), Everyone (GUI implementation)

public class CalendarPanel extends JPanel {
    private final Schedule schedule;
    private final JPanel gridPanel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");
    private CalendarCellClickListener cellClickListener;

    public interface CalendarCellClickListener {
        void onCellClicked(LocalDate date, MealType mealType);
    }

    public CalendarPanel(Schedule schedule) {
        this.schedule = schedule;
        this.gridPanel = new JPanel();
        initializeComponents();
        layoutComponents();
        updateCalendar();
    }

    private void initializeComponents() {
        setBorder(BorderFactory.createTitledBorder("Weekly Meal Schedule"));
        gridPanel.setLayout(new GridLayout(8, 4, 5, 5)); // 7 days + header row, 4 columns (date + 3 meals)
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(gridPanel, BorderLayout.CENTER);
    }

    public void updateCalendar() {
        gridPanel.removeAll();

        // Header row
        gridPanel.add(createHeaderCell("Date"));
        gridPanel.add(createHeaderCell("Breakfast"));
        gridPanel.add(createHeaderCell("Lunch"));
        gridPanel.add(createHeaderCell("Dinner"));

        // Date rows
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            
            // Date cell
            JLabel dateLabel = new JLabel(date.format(dateFormatter), SwingConstants.CENTER);
            dateLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            gridPanel.add(dateLabel);

            // Meal cells
            for (MealType mealType : new MealType[]{MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER}) {
                JPanel mealCell = createMealCell(date, mealType);
                gridPanel.add(mealCell);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JLabel createHeaderCell(String text) {
        JLabel header = new JLabel(text, SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBorder(BorderFactory.createRaisedBevelBorder());
        header.setBackground(Color.LIGHT_GRAY);
        header.setOpaque(true);
        return header;
    }

    private JPanel createMealCell(LocalDate date, MealType mealType) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cell.setBackground(Color.WHITE);
        cell.setPreferredSize(new Dimension(100, 50));

        // Get recipe ID for this date and meal type
        String recipeId = "-";
        if (schedule != null && schedule.hasMeal(date, mealType)) {
            recipeId = schedule.getMeal(date, mealType).orElse("-");
        }

        JLabel label = new JLabel(recipeId, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 10));
        cell.add(label, BorderLayout.CENTER);

        // Add click listener
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (cellClickListener != null) {
                    cellClickListener.onCellClicked(date, mealType);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                cell.setBackground(Color.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBackground(Color.WHITE);
            }
        });

        return cell;
    }

    public void setCellClickListener(CalendarCellClickListener listener) {
        this.cellClickListener = listener;
    }

    public void refresh() {
        updateCalendar();
    }
}
