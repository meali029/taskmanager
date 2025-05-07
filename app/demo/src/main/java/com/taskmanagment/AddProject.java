package com.taskmanagment;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AddProject extends JFrame {
    private JTextField taskTitleField, taskDescriptionField;
    private JComboBox<String> taskStatusDropdown, teamMemberDropdown;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JButton assignTaskButton, addTaskButton, logoutButton;

    public AddProject() {
        setTitle("Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(242, 242, 242)); 

        // Create form components for adding tasks and assigning tasks
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        formPanel.setBackground(new Color(242, 242, 242)); 

        taskTitleField = new JTextField();
        taskDescriptionField = new JTextField();
        taskStatusDropdown = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});
        teamMemberDropdown = new JComboBox<>();

        assignTaskButton = new JButton("Assign Task");
        addTaskButton = new JButton("Add Task");
        logoutButton = new JButton("Logout");

        assignTaskButton.addActionListener(e -> assignTask());
        addTaskButton.addActionListener(e -> addTask());
        logoutButton.addActionListener(e -> logout());

        // Button hover effect
        assignTaskButton.setBackground(new Color(0, 123, 255));
        addTaskButton.setBackground(new Color(40, 167, 69));
        logoutButton.setBackground(new Color(220, 53, 69));

        assignTaskButton.setForeground(Color.WHITE);
        addTaskButton.setForeground(Color.WHITE);
        logoutButton.setForeground(Color.WHITE);

        assignTaskButton.setFocusPainted(false);
        addTaskButton.setFocusPainted(false);
        logoutButton.setFocusPainted(false);

        assignTaskButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addTaskButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        assignTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                assignTaskButton.setBackground(new Color(28, 134, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                assignTaskButton.setBackground(new Color(0, 123, 255));
            }
        });

        addTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addTaskButton.setBackground(new Color(30, 145, 85));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addTaskButton.setBackground(new Color(40, 167, 69));
            }
        });

        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(180, 20, 40));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(220, 53, 69));
            }
        });

        // Add components to the form panel
        formPanel.add(new JLabel("Title:"));
        formPanel.add(taskTitleField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(taskDescriptionField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(taskStatusDropdown);
        formPanel.add(new JLabel("Assign to:"));
        formPanel.add(teamMemberDropdown);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(242, 242, 242));
        buttonPanel.add(assignTaskButton);
        buttonPanel.add(addTaskButton);
        buttonPanel.add(logoutButton);

        // Create task table to view existing tasks
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Status", "Assigned To"}, 0);
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setBackground(Color.WHITE);
        taskTable.setGridColor(new Color(220, 220, 220));
        taskTable.setRowHeight(30);
        taskTable.setFont(new Font("Arial", Font.PLAIN, 14));

        loadTasks();

        // Add components to the frame
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load team members into dropdown
        loadTeamMembers();
    }

    private void loadTasks() {
        tableModel.setRowCount(0);  
        String query = "SELECT t.id, t.title, t.description, t.status, u.username FROM tasks t INNER JOIN users u ON t.assigned_to = u.id";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("username")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tasks.");
        }
    }

    private void loadTeamMembers() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, username FROM users WHERE role = 'TeamMember'");
             ResultSet rs = stmt.executeQuery()) {
            teamMemberDropdown.removeAllItems(); 
            while (rs.next()) {
                teamMemberDropdown.addItem(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading team members.");
        }
    }

    private void assignTask() {
        String title = taskTitleField.getText();
        String description = taskDescriptionField.getText();
        String status = (String) taskStatusDropdown.getSelectedItem();
        String assignee = (String) teamMemberDropdown.getSelectedItem();

        if (title.isEmpty() || description.isEmpty() || assignee == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            // Get team member ID
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            stmt.setString(1, assignee);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");

                // Assign task to team member
                String query = "INSERT INTO tasks (title, description, assigned_to, status) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setInt(3, userId);
                stmt.setString(4, status);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Task assigned successfully!");
                loadTasks(); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error assigning task.");
        }
    }

    private void addTask() {
        String title = taskTitleField.getText();
        String description = taskDescriptionField.getText();
        String status = (String) taskStatusDropdown.getSelectedItem();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "INSERT INTO tasks (title, description, status) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, status);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task added successfully!");
            loadTasks();  
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding task.");
        }
    }

    private void logout() {
        this.dispose();
        new LoginForm().setVisible(true);
    }

    public static void main(String[] args) {
        new AddProject().setVisible(true); 
    }
}
