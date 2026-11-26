CREATE DATABASE IF NOT EXISTS todo_yola;
USE todo_yola;

CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    priority ENUM('Low', 'Medium', 'High') NOT NULL DEFAULT 'Medium',
    status ENUM('Pending', 'In Progress', 'Completed') NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tasks (title, description, priority, status) VALUES
('Belajar Java JDBC', 'Memahami koneksi database dan DAO', 'High', 'In Progress'),
('Mengerjakan tugas PBO', 'Menyelesaikan latihan GUI Java', 'Medium', 'Pending'),
('Rapikan catatan kuliah', 'Merapikan catatan minggu ini', 'Low', 'Pending');
