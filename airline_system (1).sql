-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 13, 2026 at 10:05 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `airline_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `booking`
--

CREATE TABLE `booking` (
  `booking_id` int(11) NOT NULL,
  `passenger_name` varchar(100) DEFAULT NULL,
  `flight_id` int(11) DEFAULT NULL,
  `seat_number` varchar(5) DEFAULT NULL,
  `booking_date` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking`
--

INSERT INTO `booking` (`booking_id`, `passenger_name`, `flight_id`, `seat_number`, `booking_date`) VALUES
(5, 'Moncef', 1, '12B', '2026-02-13 19:08:04'),
(7, 'Moncef', 3, '12A', '2026-02-13 19:08:04'),
(8, 'Moncef', 4, '12A', '2026-02-13 19:08:04'),
(9, 'Moncef', 5, '12A', '2026-02-13 19:08:04'),
(10, 'Moncef', 6, '12A', '2026-02-13 19:08:04'),
(11, 'Moncef', 6, '12A', '2026-02-13 19:08:04'),
(12, 'mon', 8, '12R', '2026-02-13 19:08:04'),
(14, 'Moncef', 1, '12B', '2026-02-13 19:08:04'),
(15, 'AHMED', 6, '12C', '2026-02-13 19:08:04'),
(17, 'MOTEZ', 7, '13A', '2026-02-13 19:09:30');

-- --------------------------------------------------------

--
-- Table structure for table `flight`
--

CREATE TABLE `flight` (
  `flight_id` int(11) NOT NULL,
  `flight_number` varchar(20) DEFAULT NULL,
  `departure` varchar(50) DEFAULT NULL,
  `destination` varchar(50) DEFAULT NULL,
  `departure_time` datetime DEFAULT NULL,
  `seats_available` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `flight`
--

INSERT INTO `flight` (`flight_id`, `flight_number`, `departure`, `destination`, `departure_time`, `seats_available`) VALUES
(1, 'TU101', 'Tunis', 'Paris', '2026-01-06 00:03:00', 0),
(2, 'TU101', 'Tunis', 'Paris', NULL, NULL),
(3, 'TU101', 'Tunis', 'Paris', NULL, NULL),
(4, 'TU101', 'Tunis', 'Paris', NULL, NULL),
(5, 'TU101', 'Tunis', 'Paris', NULL, NULL),
(6, 'TU101', 'Tunis', 'Paris', NULL, NULL),
(7, 'TU101', 'Tunis', 'Paris', NULL, NULL),
(8, 'TU10233', 'France', 'ENGLAND', '0000-00-00 00:00:00', 11);

-- --------------------------------------------------------

--
-- Table structure for table `luggage`
--

CREATE TABLE `luggage` (
  `luggage_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `status` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `luggage`
--

INSERT INTO `luggage` (`luggage_id`, `booking_id`, `weight`, `status`) VALUES
(4, 5, 23.5, 'Checked'),
(6, 7, 23.5, 'Checked'),
(7, 8, 23.5, 'Checked'),
(8, 9, 23.5, 'Checked'),
(9, 10, 23.5, 'Checked'),
(10, 11, 23.5, 'Checked'),
(11, 12, 35, 'checked');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `flight_id` (`flight_id`);

--
-- Indexes for table `flight`
--
ALTER TABLE `flight`
  ADD PRIMARY KEY (`flight_id`);

--
-- Indexes for table `luggage`
--
ALTER TABLE `luggage`
  ADD PRIMARY KEY (`luggage_id`),
  ADD KEY `luggage_ibfk_1` (`booking_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `booking`
--
ALTER TABLE `booking`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `flight`
--
ALTER TABLE `flight`
  MODIFY `flight_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `luggage`
--
ALTER TABLE `luggage`
  MODIFY `luggage_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`flight_id`) REFERENCES `flight` (`flight_id`);

--
-- Constraints for table `luggage`
--
ALTER TABLE `luggage`
  ADD CONSTRAINT `luggage_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
