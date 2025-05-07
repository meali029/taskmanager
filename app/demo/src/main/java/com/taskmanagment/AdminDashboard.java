package com.taskmanagment;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create the menu bar with modern styling
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(30, 30, 30));
        menuBar.setForeground(Color.WHITE);

        // User Management Menu
        JMenu userMenu = createMenu("User Management");
        JMenuItem manageUsers = new JMenuItem("Manage Users");
        manageUsers.addActionListener(e -> manageUsers());
        userMenu.add(manageUsers);

        // Project Management Menu
        JMenu projectMenu = createMenu("Project Management");
        JMenuItem manageProjects = new JMenuItem("Manage Projects");
        manageProjects.addActionListener(e -> manageProjects());
        projectMenu.add(manageProjects);

        // Team Projects Menu
        JMenu teamMenu = createMenu("Team Projects");
        JMenuItem manageTeamProjects = new JMenuItem("Manage Team Projects");
        manageTeamProjects.addActionListener(e -> manageTeamProjects());
        teamMenu.add(manageTeamProjects);
        
        // Reporting Menu
        JMenu reportingMenu = createMenu("Reporting");
        JMenuItem generateReports = new JMenuItem("Generate Reports");
        generateReports.addActionListener(e -> generateReports());
        reportingMenu.add(generateReports);

        // Notifications Menu
        JMenu notificationMenu = createMenu("Notifications");
        JMenuItem sendNotification = new JMenuItem("Send Notification");
        sendNotification.addActionListener(e -> sendNotification());
        notificationMenu.add(sendNotification);

    

        // System Status Menu
        JMenu systemMenu = createMenu("System Status");
        JMenuItem viewStatus = new JMenuItem("View Status");
        viewStatus.addActionListener(e -> viewSystemStatus());
        systemMenu.add(viewStatus);

        // Add menus to menu bar
        menuBar.add(userMenu);
        menuBar.add(projectMenu);
        menuBar.add(teamMenu);
        menuBar.add(reportingMenu);
        menuBar.add(notificationMenu);
        menuBar.add(systemMenu);

        setJMenuBar(menuBar);

        // Welcome label with custom font and styling
        JLabel welcomeLabel = new JLabel("Welcome to the Admin Dashboard", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(0, 123, 255));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(welcomeLabel, BorderLayout.CENTER);

        // Add Logout button with custom styling
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(255, 87, 34));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(e -> logout());

        // Panel for top-right logout button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);
    }

    private JMenu createMenu(String title) {
        JMenu menu = new JMenu(title);
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Arial", Font.PLAIN, 16));
        menu.setBackground(new Color(30, 30, 30));
        return menu;
    }

    private void manageUsers() {
        SwingUtilities.invokeLater(() -> {
            UserManagement userManagement = new UserManagement();
            userManagement.setVisible(true);
        });
    }

    private void manageProjects() {
        JFrame projectFrame = new JFrame("Manage Projects");
        projectFrame.setSize(600, 400);
        projectFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        projectFrame.setLocationRelativeTo(this);
        projectFrame.setLayout(new BorderLayout());

        JTable projectTable = new JTable();
        DefaultTableModel projectModel = new DefaultTableModel(new String[]{"ID", "Name", "Description"}, 0);
        projectTable.setModel(projectModel);
        projectTable.setRowHeight(30);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addProjectButton = new JButton("Add Project");
        styleButton(addProjectButton);
        addProjectButton.addActionListener(e -> addProject(projectModel));

        JButton editProjectButton = new JButton("Edit Project");
        styleButton(editProjectButton);
        editProjectButton.addActionListener(e -> editProject(projectModel, projectTable));

        JButton deleteProjectButton = new JButton("Delete Project");
        styleButton(deleteProjectButton);
        deleteProjectButton.addActionListener(e -> deleteProject(projectModel, projectTable));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(addProjectButton);
        buttonPanel.add(editProjectButton);
        buttonPanel.add(deleteProjectButton);

        projectFrame.add(new JScrollPane(projectTable), BorderLayout.CENTER);
        projectFrame.add(buttonPanel, BorderLayout.SOUTH);

        loadProjects(projectModel); 
        projectFrame.setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
    }

    private void deleteProject(DefaultTableModel projectModel, JTable projectTable) {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a project to delete.");
            return;
        }

        int projectId = (int) projectModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "DELETE FROM projects WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, projectId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project deleted successfully!");
            loadProjects(projectModel);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting project: " + e.getMessage());
        }
    }

    private void editProject(DefaultTableModel projectModel, JTable projectTable) {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a project to edit.");
            return;
        }

        int projectId = (int) projectModel.getValueAt(selectedRow, 0);
        String newName = JOptionPane.showInputDialog(this, "Enter new project name:",
                projectModel.getValueAt(selectedRow, 1));
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }
        String newDescription = JOptionPane.showInputDialog(this, "Enter new project description:",
                projectModel.getValueAt(selectedRow, 2));
        if (newDescription == null || newDescription.trim().isEmpty()) {
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "UPDATE projects SET name = ?, description = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newName);
            stmt.setString(2, newDescription);
            stmt.setInt(3, projectId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project updated successfully!");
            loadProjects(projectModel); 
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating project: " + e.getMessage());
        }
    }

    private void loadProjects(DefaultTableModel projectModel) {
        projectModel.setRowCount(0);
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, description FROM projects")) {
            while (rs.next()) {
                projectModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading projects: " + e.getMessage());
        }
    }

    private void addProject(DefaultTableModel projectModel) {
        String name = JOptionPane.showInputDialog(this, "Enter Project Name:");
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        String description = JOptionPane.showInputDialog(this, "Enter Project Description:");
        if (description == null || description.trim().isEmpty()) {
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "INSERT INTO projects (name, description) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project added successfully!");
            loadProjects(projectModel); 
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding project: " + e.getMessage());
        }
    }

    private void manageTeamProjects() {
        AddProject addProject = new AddProject();
        addProject.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();  
            new LoginForm().setVisible(true); 
        }
    }

    private void generateReports() {
        ReportPanel reportPanel = new ReportPanel();
        reportPanel.setVisible(true);
    }

    private void sendNotification() {
        String notification = JOptionPane.showInputDialog(this, "Enter Notification:");
        if (notification != null && !notification.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Notification sent successfully!");
        }
    }


    private void viewSystemStatus() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_users FROM users")) {
            if (rs.next()) {
                int totalUsers = rs.getInt("total_users");
                JOptionPane.showMessageDialog(this, "Total Users: " + totalUsers);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching system status.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}
