package com.taskmanagment;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserManagement extends JFrame {
    private JTextField usernameField, searchField;
    private JPasswordField passwordField;
    private JComboBox<String> roleDropdown;
    private JButton addButton, deleteButton, updateButton, searchButton, resetPasswordButton;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public UserManagement() {
        setTitle("User Management");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Apply Nimbus Look and Feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "Search User"));
        searchField = new JTextField();
        searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search by Username:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchButton.addActionListener(e -> searchUsers());

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "User Details"));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        roleDropdown = new JComboBox<>(new String[]{"Admin", "TeamMember"});

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleDropdown);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        addButton = createButton("Add User", "Add a new user to the system");
        updateButton = createButton("Update User", "Update the selected user's information");
        deleteButton = createButton("Delete User", "Delete the selected user");
        resetPasswordButton = createButton("Reset Password", "Reset the selected user's password");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(resetPasswordButton);

        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        resetPasswordButton.addActionListener(e -> resetPassword());

        // Table Panel
        tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0);
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        tableScrollPane.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "Users List"));
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                populateFieldsForSelectedUser();
            }
        });

        // Status Bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.EAST);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadUsers();
    }

    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        return button;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM users")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("username"), rs.getString("role")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading users: " + e.getMessage());
        }
    }
    private void addUser() {
        
        UserAddDialog addUserDialog = new UserAddDialog(this);
        addUserDialog.setVisible(true);
        loadUsers();
      }

    
    private void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to update.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = usernameField.getText();
        String role = (String) roleDropdown.getSelectedItem();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "UPDATE users SET username = ?, role = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, role);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User updated successfully.");
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect()) {
                String query = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }

    private void resetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to reset password.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            String query = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Password reset successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error resetting password: " + e.getMessage());
        }
    }

    private void searchUsers() {
        String keyword = searchField.getText();
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, username, role FROM users WHERE username LIKE ? OR role LIKE ?")) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("username"), rs.getString("role")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching users: " + e.getMessage());
        }
    }

    private void populateFieldsForSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            usernameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            roleDropdown.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
        }
    }
}
