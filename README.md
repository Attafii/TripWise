# TripWise 🌍✈️
Your next-gen tool for booking flights, hotels, and car rentals - traveling anywhere in the world made easy!

## Project Overview
TripWise is a comprehensive travel management desktop application built with JavaFX and Java. It features a modern UI with full database integration, AI-powered assistance, and complete booking management for flights, hotels, and car rentals.

### ✨ Key Features
- 🤖 **AI Travel Assistant** - Natural language interface for managing all operations
- ✈️ **Flight Booking** - Search, book, and manage flight reservations
- 🏨 **Hotel Reservations** - Browse hotels, check availability, and book rooms
- 🚗 **Car Rentals** - Rent vehicles from multiple agencies and locations
- 👥 **User Management** - Multi-role system (Visitors, Travelers, Employees, Managers, Admins)
- 💳 **Payment Processing** - Secure payment handling for all bookings
- 📊 **Analytics Dashboard** - Comprehensive reports and statistics
- 📱 **Responsive UI** - Modern JavaFX interface with smooth navigation

---
## Setup Instructions

### 1. Prerequisites
- **Java JDK 17 or higher**
- **Maven** (configured in your IDE or system PATH)
- **IntelliJ IDEA** (recommended)
- **XAMPP** (MySQL/MariaDB server)
- **Git** for version control

### 2. Database Setup
1. **Start XAMPP** and ensure MySQL is running on `localhost:3306`
2. **Import the database**:
   - Open phpMyAdmin or MySQL client
   - Import `database/setup.sql` to create the `tripwise_db` database
   - The script will create all necessary tables and sample data

### 3. Getting Started
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Attafii/Tripwise.git
   cd TripWise
   ```

2. **Open in IntelliJ**:
   - `File > Open...` -> Select the `TripWise` folder
   - Wait for Maven to download dependencies

3. **Configure Database Connection**:
   - Database configuration is in `src/main/java/ui/util/DataSource.java`
   - Default settings: `localhost:3306/tripwise_db`
   - Default credentials: `root` with no password

4. **Run the Application**:
   - Locate `src/main/java/ui/app/Main.java`
   - Right-click and select **Run 'Main.main()'**
   - Or run via terminal: `mvn clean javafx:run`

### 4. Default Login Credentials
- **Admin**: `admin@tripwise.tn` / Password: `admin123`
- **Manager**: `responsable@tripwise.tn` / Password: `admin123`
- **Employee**: `employe@tripwise.tn` / Password: `admin123`
- **Traveler**: `voyageur1@tripwise.tn` / Password: `admin123`

---

## 🤖 AI Travel Assistant

The AI Agent is a powerful feature that allows natural language interaction with the entire system.

### Features
- 👥 **User Management**: View, search, and manage users
- 🏨 **Hotel Operations**: Browse hotels, search by city, view bookings
- ✈️ **Flight Operations**: View flights, search routes, manage reservations
- 🚗 **Car Rentals**: Browse vehicles, check availability, manage rentals
- 📋 **Booking Management**: View, create, update, and cancel all booking types
- 📊 **Analytics**: Generate comprehensive reports and revenue statistics

### Example Commands
```
"Show all users"
"Show hotels in Paris"
"Show all flights"
"Show all bookings"
"Generate analytics"
"Show revenue"
"Show pending bookings"
```

### Access
- Navigate to **AI Agent** from the main dashboard
- The page features a scrollable interface to view all responses
- Type commands in natural language in the chat interface

---
## Contribution Workflow
To keep the base code clean, please follow these steps for your specific modules (Flights, Hotels, Cars, etc.):
### 1. Pull the latest base code
```bash
git checkout main
git pull origin main
```
### 2. Create your module branch
Always work on a separate branch for your assigned feature:
```bash
git checkout -b feature/your-feature-name  # e.g., feature/booking-logic
```
### 3. Commit and Push
```bash
git add .
git commit -m "Add: brief description of what you did"
git push origin feature/your-feature-name
```
### 4. Create a Pull Request (PR)
Go to the GitHub repository and open a PR from your branch to `main`. **Do not merge directly into main** without a review.
---
## Technology Stack

- **Frontend**: JavaFX 20.0.2
- **Backend**: Java 17+
- **Database**: MySQL/MariaDB (via XAMPP)
- **Build Tool**: Maven
- **Architecture**: MVC Pattern
- **ORM**: JDBC with PreparedStatements
- **UI**: FXML + CSS

---

## Project Structure

```
TripWise/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ui/
│   │   │       ├── app/           # Main application entry point
│   │   │       ├── controllers/   # UI controllers (MVC)
│   │   │       ├── model/         # Data models (User, Hotel, Flight, etc.)
│   │   │       ├── service/       # Business logic & DAO
│   │   │       └── util/          # Utilities (DataSource, SceneManager, etc.)
│   │   └── resources/
│   │       └── ui/
│   │           ├── *.fxml         # UI layouts
│   │           ├── style.css      # Global styles
│   │           └── assets/        # Images, icons, logos
��── database/
│   └── setup.sql                  # Database schema and sample data
├── pom.xml                        # Maven configuration
└── README.md
```

### Key Components

#### Controllers
- `AIAgentController.java` - AI-powered natural language interface
- `DashboardController.java` - Main dashboard navigation
- `LoginController.java` - User authentication
- `BookFlightController.java` - Flight booking interface
- `BookHotelController.java` - Hotel reservation interface
- `EmployeeBookingManagementController.java` - Employee booking operations
- And more...

#### Services
- `UserService.java` - User management operations
- `HotelService.java` - Hotel data access
- `HotelBookingService.java` - Hotel reservation logic
- `FlightService.java` - Flight data access
- `VehiculeService.java` - Car rental operations

#### Models
- `User.java` - User entity with multi-role support
- `Hotel.java` - Hotel entity
- `HotelBooking.java` - Hotel reservation entity
- `Flight.java` - Flight entity
- `Car.java` - Vehicle entity

---

## Database Schema

The application uses a comprehensive database with the following main tables:

- **users** - User accounts with role-based access
- **voyageurs** - Traveler profiles with loyalty programs
- **employes** - Employee records
- **responsables** - Manager records
- **administrateurs** - Administrator records
- **hotels** - Hotel listings
- **chambres** - Hotel room inventory
- **reservations_hotel** - Hotel bookings
- **vols** - Flight schedules
- **compagnies_aeriennes** - Airlines
- **aeroports** - Airport information
- **reservations_vol** - Flight bookings
- **vehicules** - Car rental fleet
- **reservations_vehicule** - Car rental bookings
- **paiements** - Payment transactions
- **notifications** - User notifications

---

## Recent Updates (January 2026)

### ✅ AI Agent Enhancements
- Added scrollable interface to AI Agent page
- Enhanced AI to handle ALL operations (not just bookings)
- Added support for user management queries
- Added hotel, flight, and car operations
- Comprehensive analytics and reporting
- Natural language command processing

### ✅ Database Integration
- Fixed all database queries to match actual schema
- Updated table names (e.g., `reservations_hotel`, `vols`, `vehicules`)
- Fixed column names (e.g., `user_id`, `nom_hotel`, `etoiles`, `prix_total`)
- Added proper JOIN queries for complex data
- Enhanced error handling and null checks

### ✅ UI Improvements
- Added ScrollPane to AI Agent for better UX
- Fixed dashboard layout issues
- Improved responsive design
- Enhanced error messages

---
