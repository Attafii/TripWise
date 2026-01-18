# TripWise
Your next-gen tool for booking flights and traveling anywhere in the world.
## ?? Project Overview
TripWise is a modern travel management desktop application built with JavaFX. This repository serves as the base UI template for the team.
---
## ??? Setup Instructions
### 1. Prerequisites
- **Java JDK 17 or higher**
- **Maven** (configured in your IDE)
- **IntelliJ IDEA** (recommended)
### 2. Getting Started
1. **Clone the repository**:
   ```bash
   git clone https://github.com/Attafii/Tripwise.git
   ```
2. **Open in IntelliJ**:
   - `File > Open...` -> Select the `Tripwise` folder.
   - Wait for Maven to download dependencies.
3. **Run the App**:
   - Locate `src/main/java/ui/app/Main.java`.
   - Right-click and select **Run 'Main.main()'**.
   - Or run via terminal: `mvn clean javafx:run`
---
## ?? Contribution Workflow
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
## ?? Project Structure
- `src/main/java/ui/app`: Main entry point.
- `src/main/java/ui/controllers`: Logic for each screen.
- `src/main/java/ui/model`: Data models.
- `src/main/java/ui/util`: Utilities like `SceneManager`.
- `src/main/resources/ui`: FXML layouts and CSS.
- `src/main/resources/ui/assets`: Images and icons.
