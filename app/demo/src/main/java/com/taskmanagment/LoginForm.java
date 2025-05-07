package com.taskmanagment;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, signUpButton;

    public LoginForm() {
        setTitle("Task Management Login");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel for the login form components
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); 

        // Title label
        JLabel titleLabel = new JLabel("Login to Task Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 51, 102));

        // Username field
        usernameField = new JTextField();
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));
        usernameField.setToolTipText("Enter your username");

        // Password field
        passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));
        passwordField.setToolTipText("Enter your password");

        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2));
        loginButton.setContentAreaFilled(false);
        loginButton.setOpaque(true);
        loginButton.setPreferredSize(new Dimension(200, 40)); 
        loginButton.addActionListener(e -> authenticateUser());

        // Sign-Up button
        signUpButton = new JButton("Sign Up");
        signUpButton.setFont(new Font("Arial", Font.BOLD, 14));
        signUpButton.setBackground(new Color(0, 204, 102));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        signUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpButton.setBorder(BorderFactory.createLineBorder(new Color(0, 204, 102), 2));
        signUpButton.setContentAreaFilled(false);
        signUpButton.setOpaque(true);
        signUpButton.setPreferredSize(new Dimension(200, 40)); 
        signUpButton.addActionListener(e -> openSignUpDialog());

        // Adding components to the form panel
        formPanel.add(usernameField);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(signUpButton);

        // Adding the form panel and title label to the main window
        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    private void authenticateUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed!");
                return;
            }

            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String role = rs.getString("role");
                        JOptionPane.showMessageDialog(this, "Login successful as " + role + "!");
                        switch (role) {
                            case "Admin":
                                new AdminDashboard().setVisible(true);
                                break;
                            case "TeamMember":
                                new TeamMemberDashboard(rs.getInt("id")).setVisible(true);
                                break;
                            default:
                                JOptionPane.showMessageDialog(this, "Unknown role.");
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid credentials. Try again.");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while connecting to the database.");
        }
    }

    private void openSignUpDialog() {
        // Creating a Sign-Up dialog
        JDialog signUpDialog = new JDialog(this, "Sign Up", true);
        signUpDialog.setSize(500, 600);
        signUpDialog.setLayout(new GridLayout(5, 1, 10, 10));
        signUpDialog.setLocationRelativeTo(this);

        JTextField newUsernameField = new JTextField();
        newUsernameField.setBorder(BorderFactory.createTitledBorder("New Username"));
        newUsernameField.setToolTipText("Enter a new username");

        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setBorder(BorderFactory.createTitledBorder("New Password"));
        newPasswordField.setToolTipText("Enter a new password");

       
        String[] roles = {"Admin", "TeamMember"};
        JComboBox<String> roleDropdown = new JComboBox<>(roles);
        roleDropdown.setBorder(BorderFactory.createTitledBorder("Select Role"));

        JButton signUpConfirmButton = new JButton("Sign Up");
        signUpConfirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        signUpConfirmButton.setBackground(new Color(0, 204, 102));
        signUpConfirmButton.setForeground(Color.WHITE);
        signUpConfirmButton.setFocusPainted(false);
        signUpConfirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpConfirmButton.setBorder(BorderFactory.createLineBorder(new Color(0, 204, 102), 2));
        signUpConfirmButton.setContentAreaFilled(false);
        signUpConfirmButton.setOpaque(true);
        signUpConfirmButton.setPreferredSize(new Dimension(200, 40)); // Set preferred size for rounded look
        signUpConfirmButton.addActionListener(e -> {
            String newUsername = newUsernameField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();
            String selectedRole = (String) roleDropdown.getSelectedItem();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(signUpDialog, "Username and Password cannot be empty.");
                return;
            }

            try (Connection conn = DBConnection.connect()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(signUpDialog, "Database connection failed!");
                    return;
                }

                String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setString(1, newUsername);
                    stmt.setString(2, newPassword);
                    stmt.setString(3, selectedRole);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(signUpDialog, "Sign-Up successful! You can now log in.");
                    signUpDialog.dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(signUpDialog, "An error occurred while saving user details.");
            }
        });

        signUpDialog.add(newUsernameField);
        signUpDialog.add(newPasswordField);
        signUpDialog.add(roleDropdown);
        signUpDialog.add(signUpConfirmButton);
        signUpDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
