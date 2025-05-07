package com.taskmanagment;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection connect() {
        try {
            String url = "jdbc:mysql://localhost:3306/TaskManagement"; 
            String user = "root";
            String password = "1234"; 

           
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }
    }
}
