	CREATE DATABASE TaskManagement;

	USE TaskManagement;

	CREATE TABLE users (
		id INT AUTO_INCREMENT PRIMARY KEY,
		
		password VARCHAR(100) NOT NULL,
		role ENUM('Admin', 'TeamMember') NOT NULL
	);

	CREATE TABLE tasks (
		id INT AUTO_INCREMENT PRIMARY KEY,

	title VARCHAR(100) NOT NULL,
		description TEXT,
		assigned_to INT,
		status ENUM('Pending', 'In Progress', 'Completed') NOT NULL,
		FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
	);
	CREATE TABLE access_logs (
		id INT AUTO_INCREMENT PRIMARY KEY,
		user_id INT,
		action VARCHAR(255),
		timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
	);
	CREATE TABLE projects (
		id INT AUTO_INCREMENT PRIMARY KEY,
		name VARCHAR(255),
		description TEXT
	);
	CREATE TABLE access_logs (
		id INT AUTO_INCREMENT PRIMARY KEY,
		user_id INT NOT NULL,
		action VARCHAR(255) NOT NULL,
		timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		FOREIGN KEY (user_id) REFERENCES users(id)
	);
	CREATE TABLE team_projects (
		project_id INT NOT NULL,
		team_name VARCHAR(255) NOT NULL,
		PRIMARY KEY (project_id, team_name),
		FOREIGN KEY (project_id) REFERENCES projects(id)
	);

	Select *from users;
	Alter TABLE tasks ADD COLUMN user_name VARCHAR(100);
	ALTER TABLE tasks DROP COLUMN user_name;
	SELECT t.id, t.title, t.description, t.status, u.username 
	FROM tasks t
	JOIN users u ON t.assigned_to = u.id 
	WHERE t.assigned_to = ?;

