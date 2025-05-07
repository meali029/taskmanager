package com.taskmanagment;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class TaskManagement extends JFrame {
    private JTextField titleField;
    private JTextArea descriptionField;
    private JComboBox<String> assignedToDropdown;
    private JComboBox<String> statusDropdown;
    private JButton addButton, updateButton;
    private JTable taskTable;
    private DefaultTableModel tableModel;

    public TaskManagement() {
        setTitle("Task Management");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        titleField = new JTextField();
        descriptionField = new JTextArea(3, 20);
        assignedToDropdown = new JComboBox<>();
        statusDropdown = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});

        addButton = new JButton("Add Task");
        addButton.addActionListener(e -> addTask());

        updateButton = new JButton("Update Selected Task");
        updateButton.addActionListener(e -> updateTask());

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionField));
        formPanel.add(new JLabel("Assigned To:"));
        formPanel.add(assignedToDropdown);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusDropdown);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Assigned To", "Status"}, 0);
        taskTable = new JTable(tableModel);

        loadTasks();
        loadUsers();

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTasks() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT tasks.id, tasks.title, tasks.description, users.username AS assigned_to, tasks.status FROM tasks LEFT JOIN users ON tasks.assigned_to = users.id")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("assigned_to"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tasks.");
        }
    }

    private void loadUsers() {
        assignedToDropdown.removeAllItems();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username FROM users WHERE role = 'TeamMember'")) {
            while (rs.next()) {
                assignedToDropdown.addItem(rs.getInt("id") + " - " + rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users.");
        }
    }

    private void addTask() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String assignedTo = (String) assignedToDropdown.getSelectedItem();
        String status = (String) statusDropdown.getSelectedItem();

        if (title.isEmpty() || description.isEmpty() || assignedTo == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        int assignedToId = Integer.parseInt(assignedTo.split(" - ")[0]);

        try (Connection conn = DBConnection.connect()) {
            String query = "INSERT INTO tasks (title, description, assigned_to, status) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, assignedToId);
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
        String assignedTo = (String) assignedToDropdown.getSelectedItem();
        String status = (String) statusDropdown.getSelectedItem();

        if (title.isEmpty() || description.isEmpty() || assignedTo == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        int assignedToId = Integer.parseInt(assignedTo.split(" - ")[0]);

        try (Connection conn = DBConnection.connect()) {
            String query = "UPDATE tasks SET title = ?, description = ?, assigned_to = ?, status = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, assignedToId);
            stmt.setString(4, status);
            stmt.setInt(5, taskId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task updated successfully!");
            loadTasks();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating task.");
        }
    }
}
