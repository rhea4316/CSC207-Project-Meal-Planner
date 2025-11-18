package com.mealplanner.view;
import com.mealplanner.data_access.api.SpoonacularApiClient;

// Swing view for browsing available recipes - displays recipe list and selection interface.
// Responsible: Regina (functionality), Everyone (GUI implementation)
// TODO: Create JPanel with recipe list display - listen to RecipeBrowseViewModel and call controller on recipe selection

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BrowseRecipeView extends JPanel {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("API Search Browser");
            BrowseRecipeView view = new BrowseRecipeView();

            frame.setContentPane(view);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    JTextField searchField;
    JButton searchButton;
    JList<String> resultList;
    DefaultListModel<String> resultListModel;
    JLabel statusLabel;

    private final SpoonacularApiClient apiClient = new SpoonacularApiClient();

    public BrowseRecipeView() {
        setLayout(new BorderLayout(10, 10));

        //Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        searchButton = new JButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        //add a listener
        searchButton.addActionListener(e -> doSearch());

        //result list
        resultListModel = new DefaultListModel<>();
        resultList = new JList<>(resultListModel);
        JScrollPane scrollPane = new JScrollPane(resultList);

        //status label
        statusLabel = new JLabel("Enter a query and click search.");

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            statusLabel.setText("Please enter something to search.");
            return;
        }

        statusLabel.setText("Searching...");
        resultListModel.clear();

        // Run API call in background so GUI doesn't freeze
        SwingWorker<java.util.List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<String> doInBackground() throws Exception {
                return apiClient.searchRecipes(query);
            }

            @Override
            protected void done() {
                try {
                    List<String> titles = get();
                    for (String t : titles) {
                        resultListModel.addElement(t);
                    }
                    statusLabel.setText("Found " + titles.size() + " recipes.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    public String getQueryText() {
        return searchField.getText().trim();
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public void setResults(java.util.List<String> results) {
        resultListModel.clear();
        for (String r : results) {
            resultListModel.addElement(r);
        }
    }

    }


