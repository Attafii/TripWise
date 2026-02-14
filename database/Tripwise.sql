-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: tripwise_db
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `administrateurs`
--

DROP TABLE IF EXISTS `administrateurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `administrateurs` (
  `admin_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `admin_level` enum('SUPER_ADMIN','ADMIN','MODERATEUR') DEFAULT 'ADMIN',
  `permissions` text DEFAULT NULL,
  `last_admin_action` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `administrateurs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `administrateurs`
--

LOCK TABLES `administrateurs` WRITE;
/*!40000 ALTER TABLE `administrateurs` DISABLE KEYS */;
INSERT INTO `administrateurs` VALUES (1,1,'SUPER_ADMIN','ALL',NULL),(2,7,'SUPER_ADMIN','ALL',NULL);
/*!40000 ALTER TABLE `administrateurs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `aeroports`
--

DROP TABLE IF EXISTS `aeroports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `aeroports` (
  `aeroport_id` int(11) NOT NULL AUTO_INCREMENT,
  `nom_aeroport` varchar(150) NOT NULL,
  `code_iata` varchar(3) NOT NULL,
  `code_icao` varchar(4) DEFAULT NULL,
  `ville` varchar(100) NOT NULL,
  `pays` varchar(50) NOT NULL,
  `timezone` varchar(50) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  PRIMARY KEY (`aeroport_id`),
  UNIQUE KEY `code_iata` (`code_iata`),
  UNIQUE KEY `code_icao` (`code_icao`),
  KEY `idx_ville` (`ville`),
  KEY `idx_pays` (`pays`),
  KEY `idx_code_iata` (`code_iata`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aeroports`
--

LOCK TABLES `aeroports` WRITE;
/*!40000 ALTER TABLE `aeroports` DISABLE KEYS */;
INSERT INTO `aeroports` VALUES (1,'Charles de Gaulle Airport','CDG','LFPG','Paris','France','Europe/Paris',49.012798,2.550000),(2,'John F. Kennedy International','JFK','KJFK','New York','USA','America/New_York',40.639801,-73.778900),(3,'Dubai International','DXB','OMDB','Dubai','UAE','Asia/Dubai',25.252800,55.364399),(4,'Heathrow Airport','LHR','EGLL','London','UK','Europe/London',51.470020,-0.454295),(5,'Los Angeles International','LAX','KLAX','Los Angeles','USA','America/Los_Angeles',33.942501,-118.407997),(6,'Tokyo Narita','NRT','RJAA','Tokyo','Japan','Asia/Tokyo',35.764702,140.386002),(7,'Frankfurt Airport','FRA','EDDF','Frankfurt','Germany','Europe/Berlin',50.033333,8.570556),(8,'Singapore Changi','SIN','WSSS','Singapore','Singapore','Asia/Singapore',1.350190,103.994003);
/*!40000 ALTER TABLE `aeroports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `agences_location`
--

DROP TABLE IF EXISTS `agences_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agences_location` (
  `agence_id` int(11) NOT NULL AUTO_INCREMENT,
  `compagnie_id` int(11) NOT NULL,
  `nom_agence` varchar(100) NOT NULL,
  `adresse` text NOT NULL,
  `ville` varchar(100) NOT NULL,
  `code_postal` varchar(20) DEFAULT NULL,
  `aeroport_id` int(11) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `horaires_ouverture` text DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  PRIMARY KEY (`agence_id`),
  KEY `compagnie_id` (`compagnie_id`),
  KEY `aeroport_id` (`aeroport_id`),
  KEY `idx_ville` (`ville`),
  CONSTRAINT `agences_location_ibfk_1` FOREIGN KEY (`compagnie_id`) REFERENCES `compagnies_location` (`compagnie_id`),
  CONSTRAINT `agences_location_ibfk_2` FOREIGN KEY (`aeroport_id`) REFERENCES `aeroports` (`aeroport_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agences_location`
--

LOCK TABLES `agences_location` WRITE;
/*!40000 ALTER TABLE `agences_location` DISABLE KEYS */;
INSERT INTO `agences_location` VALUES (1,1,'Hertz CDG','Terminal 2E','Paris','95700',1,'+33123456789',NULL,NULL,NULL),(2,2,'Enterprise JFK','Terminal 4','New York','11430',2,'+12125551111',NULL,NULL,NULL),(3,3,'Avis Dubai Airport','Terminal 3','Dubai','00000',3,'+97143335555',NULL,NULL,NULL),(4,4,'Budget Heathrow','Terminal 5','London','TW6 2GA',4,'+442079991234',NULL,NULL,NULL);
/*!40000 ALTER TABLE `agences_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bagages`
--

DROP TABLE IF EXISTS `bagages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bagages` (
  `bagage_id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_vol_id` int(11) NOT NULL,
  `numero_etiquette` varchar(50) NOT NULL,
  `type_bagage` enum('ENREGISTRE','CABINE','SPECIAL') NOT NULL,
  `poids` decimal(5,2) DEFAULT NULL,
  `dimensions` varchar(50) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `frais_supplementaires` decimal(8,2) DEFAULT 0.00,
  `statut_bagage` enum('ENREGISTRE','EN_TRANSIT','CHARGE','ARRIVE','LIVRE','PERDU','ENDOMMAGE') DEFAULT 'ENREGISTRE',
  `date_enregistrement` timestamp NOT NULL DEFAULT current_timestamp(),
  `derniere_position` varchar(100) DEFAULT NULL,
  `date_derniere_mise_a_jour` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`bagage_id`),
  UNIQUE KEY `numero_etiquette` (`numero_etiquette`),
  KEY `idx_reservation` (`reservation_vol_id`),
  KEY `idx_etiquette` (`numero_etiquette`),
  KEY `idx_statut` (`statut_bagage`),
  CONSTRAINT `bagages_ibfk_1` FOREIGN KEY (`reservation_vol_id`) REFERENCES `reservations_vol` (`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bagages`
--

LOCK TABLES `bagages` WRITE;
/*!40000 ALTER TABLE `bagages` DISABLE KEYS */;
/*!40000 ALTER TABLE `bagages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chambres`
--

DROP TABLE IF EXISTS `chambres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chambres` (
  `chambre_id` int(11) NOT NULL AUTO_INCREMENT,
  `hotel_id` int(11) NOT NULL,
  `type_chambre` varchar(50) NOT NULL,
  `numero_chambre` varchar(20) DEFAULT NULL,
  `capacite` int(11) NOT NULL,
  `superficie` int(11) DEFAULT NULL,
  `prix_nuit` decimal(10,2) NOT NULL,
  `description` text DEFAULT NULL,
  `equipements` text DEFAULT NULL,
  `nombre_lits` int(11) DEFAULT NULL,
  `type_lit` varchar(50) DEFAULT NULL,
  `vue` varchar(50) DEFAULT NULL,
  `fumeur` tinyint(1) DEFAULT 0,
  `accessible_handicap` tinyint(1) DEFAULT 0,
  `is_available` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`chambre_id`),
  KEY `idx_hotel` (`hotel_id`),
  KEY `idx_type` (`type_chambre`),
  KEY `idx_prix` (`prix_nuit`),
  CONSTRAINT `chambres_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chambres`
--

LOCK TABLES `chambres` WRITE;
/*!40000 ALTER TABLE `chambres` DISABLE KEYS */;
INSERT INTO `chambres` VALUES (1,1,'Suite Deluxe','501',2,45,350.00,'Luxurious suite with city view','King Bed,Minibar,TV,Safe,Balcony',1,'King','Ville',0,1,1),(2,1,'Chambre Standard','301',2,30,180.00,'Comfortable standard room','Queen Bed,TV,Minibar,Safe',1,'Queen','Ville',0,0,1),(3,1,'Suite Prestige','701',4,60,500.00,'Presidential suite with panoramic view','King Bed,Sofa Bed,Minibar,TV,Safe,Balcony,Jacuzzi',2,'King','Panoramique',0,1,1),(4,2,'Executive Room','1205',2,35,400.00,'Modern room with Manhattan skyline view','King Bed,TV,Minibar,Work Desk',1,'King','Ville',0,1,1),(5,2,'Standard Room','805',2,28,280.00,'Comfortable city room','Queen Bed,TV,Minibar',1,'Queen','Ville',0,0,1),(6,3,'Ocean View Suite','2001',2,50,450.00,'Stunning ocean view suite','King Bed,Balcony,Minibar,TV,Safe',1,'King','Mer',0,1,1),(7,3,'Deluxe Room','1501',2,40,320.00,'Luxury room with palm view','King Bed,TV,Minibar,Balcony',1,'King','Jardin',0,0,1),(8,4,'Superior Room','405',2,32,220.00,'Modern room near attractions','Queen Bed,TV,Minibar,Work Desk',1,'Queen','Ville',0,1,1),(9,4,'Standard Room','205',2,25,160.00,'Comfortable standard accommodation','Double Bed,TV,Minibar',1,'Double','Ville',0,0,1),(10,5,'Ocean Front Suite','601',2,48,380.00,'Direct beach access suite','King Bed,Balcony,Minibar,TV,Safe',1,'King','Mer',0,1,1),(11,5,'Standard Room','301',2,30,200.00,'Beach-style comfortable room','Queen Bed,TV,Minibar',1,'Queen','Ville',0,0,1);
/*!40000 ALTER TABLE `chambres` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classes_vol`
--

DROP TABLE IF EXISTS `classes_vol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classes_vol` (
  `classe_id` int(11) NOT NULL AUTO_INCREMENT,
  `vol_id` int(11) NOT NULL,
  `type_classe` enum('ECONOMIQUE','AFFAIRES','PREMIERE') NOT NULL,
  `prix` decimal(10,2) NOT NULL,
  `places_disponibles` int(11) NOT NULL,
  `bagages_inclus` int(11) DEFAULT 1,
  `poids_bagage_max` int(11) DEFAULT 23,
  `conditions_annulation` text DEFAULT NULL,
  `modifiable` tinyint(1) DEFAULT 1,
  `remboursable` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`classe_id`),
  KEY `idx_vol_classe` (`vol_id`,`type_classe`),
  CONSTRAINT `classes_vol_ibfk_1` FOREIGN KEY (`vol_id`) REFERENCES `vols` (`vol_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classes_vol`
--

LOCK TABLES `classes_vol` WRITE;
/*!40000 ALTER TABLE `classes_vol` DISABLE KEYS */;
INSERT INTO `classes_vol` VALUES (1,1,'ECONOMIQUE',450.00,200,1,23,NULL,1,0),(2,1,'AFFAIRES',1800.00,40,2,32,NULL,1,1),(3,1,'PREMIERE',4500.00,10,3,32,NULL,1,1),(4,2,'ECONOMIQUE',480.00,150,1,23,NULL,1,0),(5,2,'AFFAIRES',1900.00,40,2,32,NULL,1,1),(6,2,'PREMIERE',4700.00,10,3,32,NULL,1,1),(7,3,'ECONOMIQUE',650.00,380,2,30,NULL,1,0),(8,3,'AFFAIRES',2500.00,60,3,40,NULL,1,1),(9,3,'PREMIERE',6000.00,10,4,50,NULL,1,1),(10,4,'ECONOMIQUE',680.00,250,2,30,NULL,1,0),(11,4,'AFFAIRES',2600.00,40,3,40,NULL,1,1),(12,4,'PREMIERE',6200.00,10,4,50,NULL,1,1),(13,5,'ECONOMIQUE',280.00,140,1,23,NULL,1,0),(14,5,'AFFAIRES',850.00,10,2,32,NULL,1,1),(15,6,'ECONOMIQUE',300.00,170,1,23,NULL,1,0),(16,6,'AFFAIRES',900.00,10,2,32,NULL,1,1),(17,7,'ECONOMIQUE',120.00,120,1,23,NULL,1,0),(18,8,'ECONOMIQUE',130.00,140,1,23,NULL,1,0),(19,9,'ECONOMIQUE',550.00,230,1,23,NULL,1,0),(20,9,'AFFAIRES',2200.00,40,2,32,NULL,1,1),(21,9,'PREMIERE',5500.00,10,3,32,NULL,1,1),(22,10,'ECONOMIQUE',850.00,340,2,30,NULL,1,0),(23,10,'AFFAIRES',3500.00,50,3,40,NULL,1,1),(24,10,'PREMIERE',8000.00,10,4,50,NULL,1,1);
/*!40000 ALTER TABLE `classes_vol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compagnies_aeriennes`
--

DROP TABLE IF EXISTS `compagnies_aeriennes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `compagnies_aeriennes` (
  `compagnie_id` int(11) NOT NULL AUTO_INCREMENT,
  `nom_compagnie` varchar(100) NOT NULL,
  `code_iata` varchar(3) NOT NULL,
  `code_icao` varchar(4) DEFAULT NULL,
  `pays` varchar(50) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`compagnie_id`),
  UNIQUE KEY `code_iata` (`code_iata`),
  UNIQUE KEY `code_icao` (`code_icao`),
  KEY `idx_code_iata` (`code_iata`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compagnies_aeriennes`
--

LOCK TABLES `compagnies_aeriennes` WRITE;
/*!40000 ALTER TABLE `compagnies_aeriennes` DISABLE KEYS */;
INSERT INTO `compagnies_aeriennes` VALUES (1,'Air France','AF','AFR','France',NULL,NULL,NULL,1,'2026-01-19 10:08:26'),(2,'Emirates','EK','UAE','UAE',NULL,NULL,NULL,1,'2026-01-19 10:08:26'),(3,'American Airlines','AA','AAL','USA',NULL,NULL,NULL,1,'2026-01-19 10:08:26'),(4,'British Airways','BA','BAW','UK',NULL,NULL,NULL,1,'2026-01-19 10:08:26'),(5,'Lufthansa','LH','DLH','Germany',NULL,NULL,NULL,1,'2026-01-19 10:08:26'),(6,'Qatar Airways','QR','QTR','Qatar',NULL,NULL,NULL,1,'2026-01-19 10:08:26');
/*!40000 ALTER TABLE `compagnies_aeriennes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compagnies_location`
--

DROP TABLE IF EXISTS `compagnies_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `compagnies_location` (
  `compagnie_id` int(11) NOT NULL AUTO_INCREMENT,
  `nom_compagnie` varchar(100) NOT NULL,
  `adresse` text DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `pays` varchar(50) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `site_web` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`compagnie_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compagnies_location`
--

LOCK TABLES `compagnies_location` WRITE;
/*!40000 ALTER TABLE `compagnies_location` DISABLE KEYS */;
INSERT INTO `compagnies_location` VALUES (1,'Hertz',NULL,'Paris','France','+33123456789','contact@hertz.fr',NULL,1,'2026-01-19 10:08:26'),(2,'Enterprise',NULL,'New York','USA','+12125551111','info@enterprise.com',NULL,1,'2026-01-19 10:08:26'),(3,'Avis',NULL,'Dubai','UAE','+97143335555','dubai@avis.ae',NULL,1,'2026-01-19 10:08:26'),(4,'Budget',NULL,'London','UK','+442079991234','uk@budget.com',NULL,1,'2026-01-19 10:08:26');
/*!40000 ALTER TABLE `compagnies_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demandes_remboursement`
--

DROP TABLE IF EXISTS `demandes_remboursement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `demandes_remboursement` (
  `demande_id` int(11) NOT NULL AUTO_INCREMENT,
  `employe_id` int(11) NOT NULL,
  `reservation_type` enum('VOL','HOTEL','VEHICULE') NOT NULL,
  `reservation_id` int(11) NOT NULL,
  `montant_demande` decimal(12,2) NOT NULL,
  `motif` text NOT NULL,
  `justificatifs` text DEFAULT NULL,
  `date_demande` timestamp NOT NULL DEFAULT current_timestamp(),
  `statut_demande` enum('EN_ATTENTE','EN_REVISION','APPROUVEE','REJETEE') DEFAULT 'EN_ATTENTE',
  `conforme_politique` tinyint(1) DEFAULT NULL,
  `responsable_id` int(11) DEFAULT NULL,
  `date_traitement` timestamp NULL DEFAULT NULL,
  `commentaire_responsable` text DEFAULT NULL,
  `montant_approuve` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`demande_id`),
  KEY `idx_employe` (`employe_id`),
  KEY `idx_statut` (`statut_demande`),
  KEY `idx_responsable` (`responsable_id`),
  CONSTRAINT `demandes_remboursement_ibfk_1` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`employe_id`),
  CONSTRAINT `demandes_remboursement_ibfk_2` FOREIGN KEY (`responsable_id`) REFERENCES `responsables` (`responsable_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demandes_remboursement`
--

LOCK TABLES `demandes_remboursement` WRITE;
/*!40000 ALTER TABLE `demandes_remboursement` DISABLE KEYS */;
/*!40000 ALTER TABLE `demandes_remboursement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employes`
--

DROP TABLE IF EXISTS `employes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employes` (
  `employe_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `department` varchar(50) DEFAULT NULL,
  `position` varchar(50) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `manager_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`employe_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `manager_id` (`manager_id`),
  KEY `idx_department` (`department`),
  CONSTRAINT `employes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `employes_ibfk_2` FOREIGN KEY (`manager_id`) REFERENCES `employes` (`employe_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employes`
--

LOCK TABLES `employes` WRITE;
/*!40000 ALTER TABLE `employes` DISABLE KEYS */;
INSERT INTO `employes` VALUES (1,3,'Support Client','Agent Support','2023-01-15',NULL),(2,7,'Hotel Management','Booking Manager','2024-01-15',NULL);
/*!40000 ALTER TABLE `employes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historique_navigation_visiteur`
--

DROP TABLE IF EXISTS `historique_navigation_visiteur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `historique_navigation_visiteur` (
  `historique_id` int(11) NOT NULL AUTO_INCREMENT,
  `visiteur_id` int(11) NOT NULL,
  `type_recherche` enum('VOL','HOTEL','VEHICULE') NOT NULL,
  `criteres_recherche` text DEFAULT NULL,
  `nombre_resultats` int(11) DEFAULT NULL,
  `date_recherche` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`historique_id`),
  KEY `idx_visiteur` (`visiteur_id`),
  KEY `idx_type` (`type_recherche`),
  KEY `idx_date` (`date_recherche`),
  CONSTRAINT `historique_navigation_visiteur_ibfk_1` FOREIGN KEY (`visiteur_id`) REFERENCES `visiteurs` (`visiteur_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historique_navigation_visiteur`
--

LOCK TABLES `historique_navigation_visiteur` WRITE;
/*!40000 ALTER TABLE `historique_navigation_visiteur` DISABLE KEYS */;
INSERT INTO `historique_navigation_visiteur` VALUES (1,1,'VOL','{\"depart\":\"TUN\",\"arrivee\":\"CDG\",\"date\":\"2025-02-15\"}',5,'2026-01-19 09:35:51'),(2,1,'HOTEL','{\"ville\":\"Paris\",\"checkin\":\"2025-02-15\",\"checkout\":\"2025-02-18\"}',12,'2026-01-19 09:35:51'),(3,2,'VOL','{\"depart\":\"CDG\",\"arrivee\":\"DXB\",\"date\":\"2025-03-10\"}',8,'2026-01-19 09:35:51'),(4,3,'VEHICULE','{\"ville\":\"Tunis\",\"date_retrait\":\"2025-02-20\",\"date_retour\":\"2025-02-25\"}',6,'2026-01-19 09:35:51');
/*!40000 ALTER TABLE `historique_navigation_visiteur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotels`
--

DROP TABLE IF EXISTS `hotels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hotels` (
  `hotel_id` int(11) NOT NULL AUTO_INCREMENT,
  `nom_hotel` varchar(150) NOT NULL,
  `adresse` text NOT NULL,
  `ville` varchar(100) NOT NULL,
  `pays` varchar(50) NOT NULL,
  `code_postal` varchar(20) DEFAULT NULL,
  `etoiles` decimal(2,1) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `site_web` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `equipements` text DEFAULT NULL,
  `politique_annulation` text DEFAULT NULL,
  `heure_checkin` time DEFAULT '14:00:00',
  `heure_checkout` time DEFAULT '11:00:00',
  `image_url` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`hotel_id`),
  KEY `idx_ville` (`ville`),
  KEY `idx_pays` (`pays`),
  KEY `idx_etoiles` (`etoiles`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotels`
--

LOCK TABLES `hotels` WRITE;
/*!40000 ALTER TABLE `hotels` DISABLE KEYS */;
INSERT INTO `hotels` VALUES (1,'Le Grand Hotel Paris','123 Avenue des Champs-??lys??es','Paris','France',NULL,5.0,'+33123456789','contact@grandhotelparis.fr',NULL,'Luxury 5-star hotel in the heart of Paris','WiFi,Pool,Spa,Restaurant,Bar,Gym,Parking',NULL,'14:00:00','11:00:00',NULL,1,'2026-01-19 10:08:26'),(2,'New York Plaza','768 5th Avenue','New York','USA',NULL,5.0,'+12125551234','info@nyplaza.com',NULL,'Iconic luxury hotel in Manhattan','WiFi,Restaurant,Bar,Gym,Concierge,Room Service',NULL,'15:00:00','12:00:00',NULL,1,'2026-01-19 10:08:26'),(3,'Dubai Palm Resort','Palm Jumeirah','Dubai','UAE',NULL,5.0,'+97143334444','reservations@dubaipalm.ae',NULL,'Beachfront resort with stunning views','WiFi,Beach,Pool,Spa,Multiple Restaurants,Water Sports',NULL,'14:00:00','11:00:00',NULL,1,'2026-01-19 10:08:26'),(4,'London Westminster Hotel','45 Westminster Bridge Road','London','UK',NULL,4.0,'+442079461234','stay@londonwestminster.co.uk',NULL,'Modern hotel near Big Ben','WiFi,Restaurant,Bar,Gym',NULL,'14:00:00','11:00:00',NULL,1,'2026-01-19 10:08:26'),(5,'Los Angeles Beach Hotel','1234 Santa Monica Blvd','Los Angeles','USA',NULL,4.5,'+13105551234','hello@labeachhotel.com',NULL,'Beachside hotel in sunny LA','WiFi,Pool,Beach Access,Restaurant,Parking',NULL,'15:00:00','11:00:00',NULL,1,'2026-01-19 10:08:26');
/*!40000 ALTER TABLE `hotels` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotels_consultes_visiteur`
--

DROP TABLE IF EXISTS `hotels_consultes_visiteur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hotels_consultes_visiteur` (
  `consultation_id` int(11) NOT NULL AUTO_INCREMENT,
  `visiteur_id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `duree_consultation` int(11) DEFAULT NULL,
  `date_consultation` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`consultation_id`),
  KEY `idx_visiteur` (`visiteur_id`),
  KEY `idx_hotel` (`hotel_id`),
  KEY `idx_date` (`date_consultation`),
  CONSTRAINT `hotels_consultes_visiteur_ibfk_1` FOREIGN KEY (`visiteur_id`) REFERENCES `visiteurs` (`visiteur_id`) ON DELETE CASCADE,
  CONSTRAINT `hotels_consultes_visiteur_ibfk_2` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotels_consultes_visiteur`
--

LOCK TABLES `hotels_consultes_visiteur` WRITE;
/*!40000 ALTER TABLE `hotels_consultes_visiteur` DISABLE KEYS */;
/*!40000 ALTER TABLE `hotels_consultes_visiteur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logs_systeme`
--

DROP TABLE IF EXISTS `logs_systeme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logs_systeme` (
  `log_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `action` varchar(150) NOT NULL,
  `entite_type` varchar(50) DEFAULT NULL,
  `entite_id` int(11) DEFAULT NULL,
  `details` text DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `user_agent` text DEFAULT NULL,
  `date_action` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`log_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_action` (`action`),
  KEY `idx_date` (`date_action`),
  CONSTRAINT `logs_systeme_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logs_systeme`
--

LOCK TABLES `logs_systeme` WRITE;
/*!40000 ALTER TABLE `logs_systeme` DISABLE KEYS */;
/*!40000 ALTER TABLE `logs_systeme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages_chat`
--

DROP TABLE IF EXISTS `messages_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages_chat` (
  `message_id` int(11) NOT NULL AUTO_INCREMENT,
  `session_id` int(11) NOT NULL,
  `expediteur` enum('USER','AI') NOT NULL,
  `message` text NOT NULL,
  `date_envoi` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`message_id`),
  KEY `idx_session` (`session_id`),
  CONSTRAINT `messages_chat_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `sessions_chat` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages_chat`
--

LOCK TABLES `messages_chat` WRITE;
/*!40000 ALTER TABLE `messages_chat` DISABLE KEYS */;
/*!40000 ALTER TABLE `messages_chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `type_notification` enum('CONFIRMATION_RESERVATION','CONFIRMATION_PAIEMENT','ANNULATION','MODIFICATION','REMBOURSEMENT','RETARD_VOL','RAPPEL','GENERALE') NOT NULL,
  `titre` varchar(150) NOT NULL,
  `message` text NOT NULL,
  `lien_action` varchar(255) DEFAULT NULL,
  `is_lu` tinyint(1) DEFAULT 0,
  `date_envoi` timestamp NOT NULL DEFAULT current_timestamp(),
  `canal` enum('EMAIL','SMS','PUSH','IN_APP') DEFAULT 'IN_APP',
  PRIMARY KEY (`notification_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_lu` (`is_lu`),
  KEY `idx_type` (`type_notification`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paiements`
--

DROP TABLE IF EXISTS `paiements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paiements` (
  `paiement_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `reservation_type` enum('VOL','HOTEL','VEHICULE') NOT NULL,
  `reservation_id` int(11) NOT NULL,
  `montant` decimal(12,2) NOT NULL,
  `devise` varchar(3) DEFAULT 'EUR',
  `methode_paiement` enum('CARTE_CREDIT','CARTE_DEBIT','PAYPAL','VIREMENT','ESPECES') NOT NULL,
  `statut_paiement` enum('EN_ATTENTE','TRAITEMENT','COMPLETE','ECHOUE','REMBOURSE') DEFAULT 'EN_ATTENTE',
  `transaction_id` varchar(100) DEFAULT NULL,
  `date_paiement` timestamp NOT NULL DEFAULT current_timestamp(),
  `carte_derniers_chiffres` varchar(4) DEFAULT NULL,
  `fournisseur_paiement` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`paiement_id`),
  UNIQUE KEY `transaction_id` (`transaction_id`),
  KEY `idx_transaction` (`transaction_id`),
  KEY `idx_statut` (`statut_paiement`),
  KEY `idx_user` (`user_id`),
  KEY `idx_reservation` (`reservation_type`,`reservation_id`),
  CONSTRAINT `paiements_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paiements`
--

LOCK TABLES `paiements` WRITE;
/*!40000 ALTER TABLE `paiements` DISABLE KEYS */;
/*!40000 ALTER TABLE `paiements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rapports_statistiques`
--

DROP TABLE IF EXISTS `rapports_statistiques`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rapports_statistiques` (
  `rapport_id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` int(11) NOT NULL,
  `type_rapport` enum('RESERVATIONS','REVENUES','UTILISATEURS','REMBOURSEMENTS','PERFORMANCE') NOT NULL,
  `periode_debut` date NOT NULL,
  `periode_fin` date NOT NULL,
  `contenu_rapport` text DEFAULT NULL,
  `fichier_url` varchar(255) DEFAULT NULL,
  `date_generation` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`rapport_id`),
  KEY `idx_admin` (`admin_id`),
  KEY `idx_type` (`type_rapport`),
  KEY `idx_periode` (`periode_debut`,`periode_fin`),
  CONSTRAINT `rapports_statistiques_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `administrateurs` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rapports_statistiques`
--

LOCK TABLES `rapports_statistiques` WRITE;
/*!40000 ALTER TABLE `rapports_statistiques` DISABLE KEYS */;
/*!40000 ALTER TABLE `rapports_statistiques` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations_hotel`
--

DROP TABLE IF EXISTS `reservations_hotel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reservations_hotel` (
  `reservation_id` int(11) NOT NULL AUTO_INCREMENT,
  `voyageur_id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `chambre_id` int(11) NOT NULL,
  `date_checkin` date NOT NULL,
  `date_checkout` date NOT NULL,
  `nombre_nuits` int(11) NOT NULL,
  `nombre_adultes` int(11) NOT NULL,
  `nombre_enfants` int(11) DEFAULT 0,
  `prix_total` decimal(10,2) NOT NULL,
  `statut_reservation` enum('EN_ATTENTE','CONFIRMEE','ANNULEE','TERMINEE') DEFAULT 'EN_ATTENTE',
  `demandes_speciales` text DEFAULT NULL,
  `numero_confirmation` varchar(50) DEFAULT NULL,
  `date_reservation` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `numero_confirmation` (`numero_confirmation`),
  KEY `hotel_id` (`hotel_id`),
  KEY `chambre_id` (`chambre_id`),
  KEY `idx_voyageur` (`voyageur_id`),
  KEY `idx_dates` (`date_checkin`,`date_checkout`),
  KEY `idx_statut` (`statut_reservation`),
  CONSTRAINT `reservations_hotel_ibfk_1` FOREIGN KEY (`voyageur_id`) REFERENCES `voyageurs` (`voyageur_id`),
  CONSTRAINT `reservations_hotel_ibfk_2` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`hotel_id`),
  CONSTRAINT `reservations_hotel_ibfk_3` FOREIGN KEY (`chambre_id`) REFERENCES `chambres` (`chambre_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations_hotel`
--

LOCK TABLES `reservations_hotel` WRITE;
/*!40000 ALTER TABLE `reservations_hotel` DISABLE KEYS */;
INSERT INTO `reservations_hotel` VALUES (1,1,3,1,'2026-01-26','2026-01-28',2,2,0,640.00,'CONFIRMEE',NULL,'DS0150','2026-01-25 17:27:30'),(2,1,1,1,'2026-01-26','2026-01-28',2,2,0,360.00,'ANNULEE',NULL,'JM7413','2026-01-25 17:28:45'),(3,1,3,1,'2026-01-26','2026-01-28',2,2,0,640.00,'CONFIRMEE',NULL,'JP4461','2026-01-25 18:55:14'),(4,1,3,1,'2026-01-27','2026-01-29',2,2,0,640.00,'CONFIRMEE',NULL,'JY3626','2026-01-26 16:15:25'),(5,1,1,1,'2026-01-27','2026-01-29',2,2,0,360.00,'CONFIRMEE',NULL,'PK8306','2026-01-26 16:15:28'),(6,1,3,1,'2026-02-05','2026-02-07',2,2,0,640.00,'EN_ATTENTE',NULL,'ZF8973','2026-02-04 10:32:43');
/*!40000 ALTER TABLE `reservations_hotel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations_vehicule`
--

DROP TABLE IF EXISTS `reservations_vehicule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reservations_vehicule` (
  `reservation_id` int(11) NOT NULL AUTO_INCREMENT,
  `voyageur_id` int(11) NOT NULL,
  `vehicule_id` int(11) NOT NULL,
  `agence_retrait_id` int(11) NOT NULL,
  `agence_retour_id` int(11) NOT NULL,
  `date_retrait` datetime NOT NULL,
  `date_retour` datetime NOT NULL,
  `nombre_jours` int(11) NOT NULL,
  `prix_total` decimal(10,2) NOT NULL,
  `assurance_incluse` tinyint(1) DEFAULT 0,
  `conducteur_supplementaire` tinyint(1) DEFAULT 0,
  `siege_bebe` tinyint(1) DEFAULT 0,
  `statut_reservation` enum('EN_ATTENTE','CONFIRMEE','EN_COURS','TERMINEE','ANNULEE') DEFAULT 'EN_ATTENTE',
  `numero_confirmation` varchar(50) DEFAULT NULL,
  `date_reservation` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `numero_confirmation` (`numero_confirmation`),
  KEY `vehicule_id` (`vehicule_id`),
  KEY `agence_retrait_id` (`agence_retrait_id`),
  KEY `agence_retour_id` (`agence_retour_id`),
  KEY `idx_voyageur` (`voyageur_id`),
  KEY `idx_dates` (`date_retrait`,`date_retour`),
  KEY `idx_statut` (`statut_reservation`),
  CONSTRAINT `reservations_vehicule_ibfk_1` FOREIGN KEY (`voyageur_id`) REFERENCES `voyageurs` (`voyageur_id`),
  CONSTRAINT `reservations_vehicule_ibfk_2` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicules` (`vehicule_id`),
  CONSTRAINT `reservations_vehicule_ibfk_3` FOREIGN KEY (`agence_retrait_id`) REFERENCES `agences_location` (`agence_id`),
  CONSTRAINT `reservations_vehicule_ibfk_4` FOREIGN KEY (`agence_retour_id`) REFERENCES `agences_location` (`agence_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations_vehicule`
--

LOCK TABLES `reservations_vehicule` WRITE;
/*!40000 ALTER TABLE `reservations_vehicule` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservations_vehicule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations_vol`
--

DROP TABLE IF EXISTS `reservations_vol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reservations_vol` (
  `reservation_id` int(11) NOT NULL AUTO_INCREMENT,
  `voyageur_id` int(11) NOT NULL,
  `vol_id` int(11) NOT NULL,
  `classe_id` int(11) NOT NULL,
  `numero_siege` varchar(10) DEFAULT NULL,
  `date_reservation` timestamp NOT NULL DEFAULT current_timestamp(),
  `nombre_passagers` int(11) DEFAULT 1,
  `prix_total` decimal(10,2) NOT NULL,
  `statut_reservation` enum('EN_ATTENTE','CONFIRMEE','ANNULEE','TERMINEE') DEFAULT 'EN_ATTENTE',
  `numero_confirmation` varchar(50) DEFAULT NULL,
  `checked_in` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `numero_confirmation` (`numero_confirmation`),
  KEY `vol_id` (`vol_id`),
  KEY `classe_id` (`classe_id`),
  KEY `idx_voyageur` (`voyageur_id`),
  KEY `idx_statut` (`statut_reservation`),
  KEY `idx_confirmation` (`numero_confirmation`),
  CONSTRAINT `reservations_vol_ibfk_1` FOREIGN KEY (`voyageur_id`) REFERENCES `voyageurs` (`voyageur_id`),
  CONSTRAINT `reservations_vol_ibfk_2` FOREIGN KEY (`vol_id`) REFERENCES `vols` (`vol_id`),
  CONSTRAINT `reservations_vol_ibfk_3` FOREIGN KEY (`classe_id`) REFERENCES `classes_vol` (`classe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations_vol`
--

LOCK TABLES `reservations_vol` WRITE;
/*!40000 ALTER TABLE `reservations_vol` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservations_vol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `responsables`
--

DROP TABLE IF EXISTS `responsables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `responsables` (
  `responsable_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `department` varchar(50) DEFAULT NULL,
  `authorization_level` int(11) DEFAULT 1,
  `max_approval_amount` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`responsable_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `responsables_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `responsables`
--

LOCK TABLES `responsables` WRITE;
/*!40000 ALTER TABLE `responsables` DISABLE KEYS */;
/*!40000 ALTER TABLE `responsables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessions_chat`
--

DROP TABLE IF EXISTS `sessions_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sessions_chat` (
  `session_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `session_token` varchar(100) NOT NULL,
  `date_debut` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_fin` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`session_id`),
  UNIQUE KEY `session_token` (`session_token`),
  KEY `user_id` (`user_id`),
  KEY `idx_token` (`session_token`),
  CONSTRAINT `sessions_chat_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sessions_chat`
--

LOCK TABLES `sessions_chat` WRITE;
/*!40000 ALTER TABLE `sessions_chat` DISABLE KEYS */;
/*!40000 ALTER TABLE `sessions_chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suivi_bagages`
--

DROP TABLE IF EXISTS `suivi_bagages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `suivi_bagages` (
  `suivi_id` int(11) NOT NULL AUTO_INCREMENT,
  `bagage_id` int(11) NOT NULL,
  `aeroport_id` int(11) DEFAULT NULL,
  `statut` enum('ENREGISTRE','EN_TRANSIT','CHARGE','ARRIVE','LIVRE','PERDU','ENDOMMAGE') NOT NULL,
  `localisation` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date_evenement` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`suivi_id`),
  KEY `aeroport_id` (`aeroport_id`),
  KEY `idx_bagage` (`bagage_id`),
  KEY `idx_date` (`date_evenement`),
  CONSTRAINT `suivi_bagages_ibfk_1` FOREIGN KEY (`bagage_id`) REFERENCES `bagages` (`bagage_id`) ON DELETE CASCADE,
  CONSTRAINT `suivi_bagages_ibfk_2` FOREIGN KEY (`aeroport_id`) REFERENCES `aeroports` (`aeroport_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suivi_bagages`
--

LOCK TABLES `suivi_bagages` WRITE;
/*!40000 ALTER TABLE `suivi_bagages` DISABLE KEYS */;
/*!40000 ALTER TABLE `suivi_bagages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions_points`
--

DROP TABLE IF EXISTS `transactions_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transactions_points` (
  `transaction_id` int(11) NOT NULL AUTO_INCREMENT,
  `voyageur_id` int(11) NOT NULL,
  `points` int(11) NOT NULL,
  `type_transaction` enum('GAIN_RESERVATION','GAIN_VOL','UTILISATION','BONUS','EXPIRATION') NOT NULL,
  `reservation_type` enum('VOL','HOTEL','VEHICULE') DEFAULT NULL,
  `reservation_id` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date_transaction` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`transaction_id`),
  KEY `idx_voyageur` (`voyageur_id`),
  KEY `idx_date` (`date_transaction`),
  CONSTRAINT `transactions_points_ibfk_1` FOREIGN KEY (`voyageur_id`) REFERENCES `voyageurs` (`voyageur_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions_points`
--

LOCK TABLES `transactions_points` WRITE;
/*!40000 ALTER TABLE `transactions_points` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `user_type` enum('VISITEUR','VOYAGEUR','EMPLOYE','RESPONSABLE','ADMIN') NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(50) DEFAULT NULL,
  `passport_number` varchar(50) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin@tripwise.tn','adminn123','Ahmed','Admin','+21612345678','ADMIN','1990-01-15','Tunisienne',NULL,NULL,NULL,1,'2026-01-19 09:35:50','2026-02-06 14:27:07','2026-02-06 13:27:07'),(3,'employe@tripwise.tn','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Hamza','Support','+21612345680','EMPLOYE','1992-06-10','Tunisienne',NULL,NULL,NULL,1,'2026-01-19 09:35:50','2026-01-19 09:35:50',NULL),(4,'voyageur1@tripwise.tn','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Sophie','Martin','+21612345681','VOYAGEUR','1995-08-25','Française',NULL,NULL,NULL,1,'2026-01-19 09:35:50','2026-01-19 09:35:50',NULL),(5,'voyageur2@tripwise.tn','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Marc','Dubois','+21612345682','VOYAGEUR','1987-11-30','Française',NULL,NULL,NULL,1,'2026-01-19 09:35:50','2026-01-19 09:35:50',NULL),(6,'visiteur@tripwise.tn','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Jean','Visiteur','+21612345683','VISITEUR','2000-02-14','Belge',NULL,NULL,NULL,1,'2026-01-19 09:35:50','2026-01-19 09:35:50',NULL),(7,'admin@tripwise.com','admin123','Admin','User','+1234567890','EMPLOYE',NULL,NULL,NULL,NULL,NULL,1,'2026-01-19 10:08:26','2026-02-04 14:37:51','2026-02-04 13:37:51'),(8,'john.doe@email.com','password123','John','Doe','+1987654321','VOYAGEUR',NULL,NULL,NULL,NULL,NULL,1,'2026-01-19 10:08:26','2026-02-04 13:30:12','2026-02-04 12:30:12'),(10,'manager@tripwise.com','manager123','Bob','Manager','+1444567890','RESPONSABLE',NULL,NULL,NULL,NULL,NULL,1,'2026-01-19 10:08:26','2026-02-04 13:30:36','2026-02-04 12:30:36'),(11,'employee@tripwise.com','employee123','Alice','Employee','+1333567890','EMPLOYE',NULL,NULL,NULL,NULL,NULL,1,'2026-01-19 10:08:26','2026-01-25 20:32:37','2026-01-25 19:32:37');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicules`
--

DROP TABLE IF EXISTS `vehicules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vehicules` (
  `vehicule_id` int(11) NOT NULL AUTO_INCREMENT,
  `compagnie_id` int(11) NOT NULL,
  `marque` varchar(50) NOT NULL,
  `modele` varchar(50) NOT NULL,
  `annee` int(11) DEFAULT NULL,
  `categorie` enum('ECONOMIQUE','COMPACTE','INTERMEDIAIRE','SUV','LUXE','MONOSPACE') NOT NULL,
  `transmission` enum('MANUELLE','AUTOMATIQUE') NOT NULL,
  `carburant` enum('ESSENCE','DIESEL','ELECTRIQUE','HYBRIDE') NOT NULL,
  `nombre_places` int(11) NOT NULL,
  `nombre_portes` int(11) DEFAULT NULL,
  `climatisation` tinyint(1) DEFAULT 1,
  `gps` tinyint(1) DEFAULT 0,
  `image_url` varchar(255) DEFAULT NULL,
  `prix_jour` decimal(10,2) NOT NULL,
  `caution` decimal(10,2) DEFAULT NULL,
  `kilometrage_illimite` tinyint(1) DEFAULT 1,
  `is_available` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`vehicule_id`),
  KEY `compagnie_id` (`compagnie_id`),
  KEY `idx_categorie` (`categorie`),
  KEY `idx_prix` (`prix_jour`),
  CONSTRAINT `vehicules_ibfk_1` FOREIGN KEY (`compagnie_id`) REFERENCES `compagnies_location` (`compagnie_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicules`
--

LOCK TABLES `vehicules` WRITE;
/*!40000 ALTER TABLE `vehicules` DISABLE KEYS */;
INSERT INTO `vehicules` VALUES (1,1,'Renault','Clio',2024,'ECONOMIQUE','MANUELLE','ESSENCE',5,5,1,0,NULL,35.00,300.00,1,1),(2,1,'Peugeot','3008',2024,'SUV','AUTOMATIQUE','DIESEL',5,5,1,1,NULL,75.00,500.00,1,1),(3,1,'Mercedes','Classe E',2025,'LUXE','AUTOMATIQUE','HYBRIDE',5,4,1,1,NULL,150.00,1000.00,1,1),(4,2,'Toyota','Camry',2024,'INTERMEDIAIRE','AUTOMATIQUE','ESSENCE',5,4,1,1,NULL,55.00,400.00,1,1),(5,2,'Ford','Mustang',2025,'LUXE','AUTOMATIQUE','ESSENCE',4,2,1,1,NULL,120.00,800.00,1,1),(6,2,'Chevrolet','Suburban',2024,'MONOSPACE','AUTOMATIQUE','ESSENCE',7,5,1,1,NULL,95.00,600.00,1,1),(7,3,'Nissan','Sunny',2024,'ECONOMIQUE','AUTOMATIQUE','ESSENCE',5,4,1,0,NULL,40.00,350.00,1,1),(8,3,'BMW','X5',2025,'SUV','AUTOMATIQUE','DIESEL',5,5,1,1,NULL,130.00,900.00,1,1),(9,3,'Range Rover','Evoque',2025,'LUXE','AUTOMATIQUE','HYBRIDE',5,5,1,1,NULL,180.00,1200.00,1,1),(10,4,'Volkswagen','Golf',2024,'COMPACTE','MANUELLE','DIESEL',5,5,1,0,NULL,38.00,300.00,1,1),(11,4,'Audi','A4',2024,'INTERMEDIAIRE','AUTOMATIQUE','DIESEL',5,4,1,1,NULL,80.00,500.00,1,1),(12,4,'Tesla','Model 3',2025,'LUXE','AUTOMATIQUE','ELECTRIQUE',5,4,1,1,NULL,140.00,1000.00,1,1);
/*!40000 ALTER TABLE `vehicules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicules_consultes_visiteur`
--

DROP TABLE IF EXISTS `vehicules_consultes_visiteur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vehicules_consultes_visiteur` (
  `consultation_id` int(11) NOT NULL AUTO_INCREMENT,
  `visiteur_id` int(11) NOT NULL,
  `vehicule_id` int(11) NOT NULL,
  `duree_consultation` int(11) DEFAULT NULL,
  `date_consultation` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`consultation_id`),
  KEY `idx_visiteur` (`visiteur_id`),
  KEY `idx_vehicule` (`vehicule_id`),
  KEY `idx_date` (`date_consultation`),
  CONSTRAINT `vehicules_consultes_visiteur_ibfk_1` FOREIGN KEY (`visiteur_id`) REFERENCES `visiteurs` (`visiteur_id`) ON DELETE CASCADE,
  CONSTRAINT `vehicules_consultes_visiteur_ibfk_2` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicules` (`vehicule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicules_consultes_visiteur`
--

LOCK TABLES `vehicules_consultes_visiteur` WRITE;
/*!40000 ALTER TABLE `vehicules_consultes_visiteur` DISABLE KEYS */;
/*!40000 ALTER TABLE `vehicules_consultes_visiteur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visiteurs`
--

DROP TABLE IF EXISTS `visiteurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visiteurs` (
  `visiteur_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `session_token` varchar(100) NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `user_agent` text DEFAULT NULL,
  `date_visite` timestamp NOT NULL DEFAULT current_timestamp(),
  `derniere_activite` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`visiteur_id`),
  UNIQUE KEY `session_token` (`session_token`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_session` (`session_token`),
  KEY `idx_date` (`date_visite`),
  CONSTRAINT `visiteurs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visiteurs`
--

LOCK TABLES `visiteurs` WRITE;
/*!40000 ALTER TABLE `visiteurs` DISABLE KEYS */;
INSERT INTO `visiteurs` VALUES (1,NULL,'VISITOR_SESSION_001','192.168.1.100','Mozilla/5.0 (Windows NT 10.0; Win64; x64)','2026-01-19 09:35:51','2026-01-19 09:35:51'),(2,NULL,'VISITOR_SESSION_002','192.168.1.101','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)','2026-01-19 09:35:51','2026-01-19 09:35:51'),(3,NULL,'VISITOR_SESSION_003','192.168.1.102','Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)','2026-01-19 09:35:51','2026-01-19 09:35:51'),(4,6,'VISITOR_SESSION_REGISTERED_001','192.168.1.103',NULL,'2026-01-19 09:35:51','2026-01-19 09:35:51');
/*!40000 ALTER TABLE `visiteurs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vols`
--

DROP TABLE IF EXISTS `vols`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vols` (
  `vol_id` int(11) NOT NULL AUTO_INCREMENT,
  `numero_vol` varchar(20) NOT NULL,
  `compagnie_id` int(11) NOT NULL,
  `aeroport_depart_id` int(11) NOT NULL,
  `aeroport_arrivee_id` int(11) NOT NULL,
  `date_depart` datetime NOT NULL,
  `date_arrivee` datetime NOT NULL,
  `duree_vol` int(11) DEFAULT NULL,
  `type_avion` varchar(50) DEFAULT NULL,
  `capacite_totale` int(11) NOT NULL,
  `places_disponibles` int(11) NOT NULL,
  `statut_vol` enum('PROGRAMME','EN_COURS','ATTERRI','ANNULE','RETARDE') DEFAULT 'PROGRAMME',
  `porte_embarquement` varchar(10) DEFAULT NULL,
  `terminal` varchar(10) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`vol_id`),
  KEY `compagnie_id` (`compagnie_id`),
  KEY `aeroport_depart_id` (`aeroport_depart_id`),
  KEY `aeroport_arrivee_id` (`aeroport_arrivee_id`),
  KEY `idx_date_depart` (`date_depart`),
  KEY `idx_statut` (`statut_vol`),
  KEY `idx_numero_vol` (`numero_vol`),
  CONSTRAINT `vols_ibfk_1` FOREIGN KEY (`compagnie_id`) REFERENCES `compagnies_aeriennes` (`compagnie_id`),
  CONSTRAINT `vols_ibfk_2` FOREIGN KEY (`aeroport_depart_id`) REFERENCES `aeroports` (`aeroport_id`),
  CONSTRAINT `vols_ibfk_3` FOREIGN KEY (`aeroport_arrivee_id`) REFERENCES `aeroports` (`aeroport_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vols`
--

LOCK TABLES `vols` WRITE;
/*!40000 ALTER TABLE `vols` DISABLE KEYS */;
INSERT INTO `vols` VALUES (1,'AF001',1,1,2,'2026-02-01 08:00:00','2026-02-01 10:30:00',510,'Boeing 777',300,250,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(2,'AF002',1,1,2,'2026-02-05 14:00:00','2026-02-05 16:30:00',510,'Airbus A350',280,200,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(3,'EK007',2,3,4,'2026-02-03 02:00:00','2026-02-03 06:30:00',450,'Airbus A380',500,450,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(4,'EK008',2,3,4,'2026-02-10 10:00:00','2026-02-10 14:30:00',450,'Boeing 777',350,300,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(5,'AA100',3,2,5,'2026-02-02 09:00:00','2026-02-02 12:30:00',390,'Boeing 737',180,150,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(6,'AA101',3,2,5,'2026-02-08 15:00:00','2026-02-08 18:30:00',390,'Airbus A321',200,180,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(7,'BA303',4,4,1,'2026-02-04 11:00:00','2026-02-04 13:15:00',75,'Airbus A320',150,120,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(8,'BA304',4,4,1,'2026-02-15 16:00:00','2026-02-15 18:15:00',75,'Boeing 737',160,140,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(9,'AF505',1,1,3,'2026-02-06 22:00:00','2026-02-07 07:00:00',420,'Boeing 777',320,280,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26'),(10,'EK312',2,3,6,'2026-02-12 03:00:00','2026-02-12 16:00:00',600,'Airbus A380',480,400,'PROGRAMME',NULL,NULL,1,'2026-01-19 10:08:26');
/*!40000 ALTER TABLE `vols` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vols_consultes_visiteur`
--

DROP TABLE IF EXISTS `vols_consultes_visiteur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vols_consultes_visiteur` (
  `consultation_id` int(11) NOT NULL AUTO_INCREMENT,
  `visiteur_id` int(11) NOT NULL,
  `vol_id` int(11) NOT NULL,
  `duree_consultation` int(11) DEFAULT NULL,
  `date_consultation` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`consultation_id`),
  KEY `idx_visiteur` (`visiteur_id`),
  KEY `idx_vol` (`vol_id`),
  KEY `idx_date` (`date_consultation`),
  CONSTRAINT `vols_consultes_visiteur_ibfk_1` FOREIGN KEY (`visiteur_id`) REFERENCES `visiteurs` (`visiteur_id`) ON DELETE CASCADE,
  CONSTRAINT `vols_consultes_visiteur_ibfk_2` FOREIGN KEY (`vol_id`) REFERENCES `vols` (`vol_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vols_consultes_visiteur`
--

LOCK TABLES `vols_consultes_visiteur` WRITE;
/*!40000 ALTER TABLE `vols_consultes_visiteur` DISABLE KEYS */;
/*!40000 ALTER TABLE `vols_consultes_visiteur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voyageurs`
--

DROP TABLE IF EXISTS `voyageurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `voyageurs` (
  `voyageur_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `emergency_contact_name` varchar(100) DEFAULT NULL,
  `emergency_contact_phone` varchar(20) DEFAULT NULL,
  `travel_preferences` text DEFAULT NULL,
  `seating_preference` enum('FENETRE','COULOIR','PAS_DE_PREFERENCE') DEFAULT 'PAS_DE_PREFERENCE',
  `meal_preference` enum('STANDARD','VEGETARIEN','HALAL','KOSHER','SANS_GLUTEN') DEFAULT 'STANDARD',
  `loyalty_number` varchar(50) DEFAULT NULL,
  `loyalty_points` int(11) DEFAULT 0,
  `loyalty_status` enum('BRONZE','ARGENT','OR','PLATINE') DEFAULT 'BRONZE',
  PRIMARY KEY (`voyageur_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_loyalty` (`loyalty_number`),
  CONSTRAINT `voyageurs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voyageurs`
--

LOCK TABLES `voyageurs` WRITE;
/*!40000 ALTER TABLE `voyageurs` DISABLE KEYS */;
INSERT INTO `voyageurs` VALUES (1,4,'Pierre Martin','+33612345678',NULL,'PAS_DE_PREFERENCE','STANDARD',NULL,1500,'OR'),(2,5,'Claire Dubois','+33612345679',NULL,'PAS_DE_PREFERENCE','STANDARD',NULL,500,'BRONZE'),(3,8,'Mary Doe','+1987654322',NULL,'FENETRE','STANDARD',NULL,1500,'ARGENT');
/*!40000 ALTER TABLE `voyageurs` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-13 20:46:33
