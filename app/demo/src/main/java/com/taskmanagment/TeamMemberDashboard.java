package com.taskmanagment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class TeamMemberDashboard extends JFrame {
    private int userId;
    private JTextField titleField;
    private JTextArea descriptionField;
    private JComboBox<String> statusDropdown;
    private JButton addButton, updateButton, clearButton, logoutButton;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel memberNameLabel;

    public TeamMemberDashboard(int userId) {
        this.userId = userId;

        setTitle("Team Member Dashboard");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));  
        memberNameLabel = new JLabel("Loading member name...");
        fetchMemberName();

        // Create form components with padding and margin
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  
        titleField = new JTextField();
        titleField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));  

        descriptionField = new JTextArea(3, 20);
        descriptionField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));  
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);

        statusDropdown = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});
        statusDropdown.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));  
        addButton = new JButton("Add Task");
        addButton.setFocusPainted(false);
        addButton.setBackground(new Color(52, 152, 219));
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createLineBorder(new Color(34, 193, 195), 2));
        addButton.setPreferredSize(new Dimension(120, 40));
        addButton.addActionListener(e -> addTask());

        updateButton = new JButton("Update Selected Task");
        updateButton.setFocusPainted(false);
        updateButton.setBackground(new Color(31, 97, 141));
        updateButton.setForeground(Color.WHITE);
        updateButton.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
        updateButton.setPreferredSize(new Dimension(160, 40));
        updateButton.addActionListener(e -> updateTask());

        clearButton = new JButton("Clear Fields");
        clearButton.setFocusPainted(false);
        clearButton.setBackground(new Color(39, 174, 96));
        clearButton.setForeground(Color.WHITE);
        clearButton.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2));
        clearButton.setPreferredSize(new Dimension(120, 40));
        clearButton.addActionListener(e -> clearFields());

        logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 2));
        logoutButton.setPreferredSize(new Dimension(100, 40));
        logoutButton.addActionListener(e -> logout());

        // Add form fields to formPanel
        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionField));
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusDropdown);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(logoutButton);

        // Create task table with customized styling
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Status"}, 0);
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(30);
        taskTable.setFont(new Font("Arial", Font.PLAIN, 14));
        taskTable.setSelectionBackground(new Color(173, 216, 230));
        taskTable.setSelectionForeground(Color.BLACK);
        taskTable.setGridColor(Color.GRAY);
        taskTable.getTableHeader().setBackground(new Color(0, 123, 255));
        taskTable.getTableHeader().setForeground(Color.WHITE);

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                populateFieldsForSelectedTask();
            }
        });

        loadTasks();

        // Add components to the frame
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(memberNameLabel, BorderLayout.NORTH);
        headerPanel.add(formPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchMemberName() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String memberName = rs.getString("username");
                    memberNameLabel.setText("Welcome, " + memberName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching member username.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        tableModel.setRowCount(0);  // Clear the table rows
        String query = "SELECT t.id, t.title, t.description, t.status FROM tasks t WHERE t.assigned_to = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);  
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tasks.");
        }
    }

    private void addTask() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String status = (String) statusDropdown.getSelectedItem();
    
        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
    
        try (Connection conn = DBConnection.connect()) {
            String query = "INSERT INTO tasks (title, description, assigned_to, status) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, userId);  
            stmt.setString(4, status);
    
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task added successfully!");
            loadTasks();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding task.");
        }
    }

    private void updateTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a task to update.");
            return;
        }

        int taskId = (int) tableModel.getValueAt(selectedRow, 0);
        String title = titleField.getText();
        String description = descriptionField.getText();
        String status = (String) statusDropdown.getSelectedItem();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "UPDATE tasks SET title = ?, description = ?, status = ? WHERE id = ? AND assigned_to = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, status);
            stmt.setInt(4, taskId);
            stmt.setInt(5, userId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task updated successfully!");
            loadTasks();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating task.");
        }
    }

    private void populateFieldsForSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow != -1) {
            titleField.setText((String) tableModel.getValueAt(selectedRow, 1));
            descriptionField.setText((String) tableModel.getValueAt(selectedRow, 2));
            statusDropdown.setSelectedItem(tableModel.getValueAt(selectedRow, 3));
        }
    }

    private void clearFields() {
        titleField.setText("");
        descriptionField.setText("");
        statusDropdown.setSelectedIndex(0);
    }
    private void logout() {
        this.dispose(); 
        new LoginForm().setVisible(true); 
    }
}
