-- =========================
-- Bus Reservation Database Setup
-- =========================

CREATE DATABASE IF NOT EXISTS bus_reservation;
USE bus_reservation;

-- =========================
-- DROP TABLES IF THEY EXIST
-- =========================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Reservations;
DROP TABLE IF EXISTS Passengers;
DROP TABLE IF EXISTS Buses;
DROP TABLE IF EXISTS Routes;
DROP TABLE IF EXISTS Users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- Users Table
-- =========================
CREATE TABLE Users (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('admin','user') NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default admin account
INSERT INTO Users (name, email, password, role) VALUES 
('Admin', 'admin@bus.com', 'admin', 'admin');

-- =========================
-- Routes Table
-- =========================
CREATE TABLE Routes (
    routeId INT PRIMARY KEY AUTO_INCREMENT,
    source VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    fare DOUBLE NOT NULL,
    routeName VARCHAR(255) GENERATED ALWAYS AS (CONCAT(source,' -> ',destination)) STORED
);

-- =========================
-- Buses Table
-- =========================
CREATE TABLE Buses (
    busNumber VARCHAR(50) PRIMARY KEY,
    busType VARCHAR(50) NOT NULL,
    totalSeats INT NOT NULL,
    availableSeats INT NOT NULL,
    routeId INT,
    departureTime TIME NOT NULL DEFAULT '07:00:00',
    arrivalTime TIME NOT NULL DEFAULT '20:00:00',
    FOREIGN KEY (routeId) REFERENCES Routes(routeId) ON DELETE SET NULL
);

-- =========================
-- Passengers Table
-- =========================
CREATE TABLE Passengers (
    passengerId INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    contact VARCHAR(100) NOT NULL
);

-- =========================
-- Reservations Table
-- =========================
CREATE TABLE Reservations (
    reservationId INT PRIMARY KEY AUTO_INCREMENT,
    passengerId INT,
    userEmail VARCHAR(100),
    busNumber VARCHAR(50),
    seatNumber INT NOT NULL,
    journeyDate DATE NOT NULL,
    status ENUM('Booked','Cancelled') DEFAULT 'Booked',
    bookingDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (passengerId) REFERENCES Passengers(passengerId) ON DELETE CASCADE,
    FOREIGN KEY (busNumber) REFERENCES Buses(busNumber) ON DELETE CASCADE,
    FOREIGN KEY (userEmail) REFERENCES Users(email) ON DELETE CASCADE
);

-- =========================
-- Sample Routes (with IDs)
-- =========================
INSERT INTO Routes (routeId, source, destination, fare) VALUES
(1, 'Kerala', 'Bangalore', 700),
(2, 'Kerala', 'Chennai', 900),
(3, 'Bangalore', 'Hyderabad', 800),
(4, 'Chennai', 'Hyderabad', 850),
(5, 'Kerala', 'Hyderabad', 1200),
(6, 'Bangalore', 'Chennai', 650);

-- =========================
-- Sample Buses
-- =========================
INSERT INTO Buses (busNumber, busType, totalSeats, availableSeats, routeId, departureTime, arrivalTime) VALUES
('KL01AB1234', 'AC Sleeper', 30, 30, 1, '07:00:00', '20:00:00'),
('KL02BC5678', 'Non-AC Seater', 40, 40, 2, '08:00:00', '22:00:00'),
('KA01CD9876', 'AC Seater', 35, 35, 3, '06:30:00', '18:30:00'),
('TN01EF5432', 'Non-AC Sleeper', 28, 28, 4, '09:00:00', '21:00:00'),
('KL03GH1111', 'AC Sleeper', 32, 32, 5, '05:30:00', '19:30:00'),
('KA02IJ2222', 'Non-AC Seater', 40, 40, 6, '12:00:00', '18:30:00');
