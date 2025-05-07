package com.taskmanagment;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ReportPanel extends JFrame {

    public ReportPanel() {
        setTitle("Report - Projects Overview");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Set a background color for the window
        getContentPane().setBackground(new Color(240, 240, 240));

        // Create a JTable to display report data
        JTable reportTable = new JTable();
        DefaultTableModel reportModel = new DefaultTableModel(new String[]{"Project ID", "Project Name", "Description", "Assigned Member"}, 0);
        reportTable.setModel(reportModel);

        // Customize table style
        reportTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reportTable.setRowHeight(30);
        reportTable.setSelectionBackground(new Color(173, 216, 230));
        
        JTableHeader tableHeader = reportTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));
        tableHeader.setBackground(new Color(70, 130, 180));
        tableHeader.setForeground(Color.WHITE);
        
        // Load report data from the database
        loadReportData(reportModel);

        // Add the table to a JScrollPane with customized border
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Add a close button with enhanced styling
        JButton closeButton = new JButton("Close Report");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
        closeButton.addActionListener(e -> dispose()); 

        // Add button to the bottom with some padding
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadReportData(DefaultTableModel reportModel) {
        // Updated query to fetch all projects assigned to each member
        String query = "SELECT p.id, p.name, p.description, u.username " +
                       "FROM projects p " +
                       "JOIN team_projects tp ON p.id = tp.project_id " +
                       "JOIN users u ON tp.team_name = u.username";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

           
            while (rs.next()) {
                reportModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("username") 
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading report data.");
        }
    }

    public static void main(String[] args) {
       
        SwingUtilities.invokeLater(() -> new ReportPanel().setVisible(true));
    }
}
