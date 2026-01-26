package ui.service;

import ui.model.Car;
import ui.util.DataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VehiculeService - Fetches real vehicle data from database
 * Queries from: vehicules, compagnies_location tables
 */
public class VehiculeService implements IService<Car> {

    private final Connection connection;

    public VehiculeService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean add(Car car) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public boolean update(Car car) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public boolean delete(int id) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public Car getById(int id) {
        String query = "SELECT v.*, cl.nom_compagnie " +
                      "FROM vehicules v " +
                      "LEFT JOIN compagnies_location cl ON v.compagnie_id = cl.compagnie_id " +
                      "WHERE v.vehicule_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractCarFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting car by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Car> getAll() {
        List<Car> cars = new ArrayList<>();
        String query = "SELECT v.*, cl.nom_compagnie " +
                      "FROM vehicules v " +
                      "LEFT JOIN compagnies_location cl ON v.compagnie_id = cl.compagnie_id " +
                      "WHERE v.is_available = 1 " +
                      "ORDER BY v.categorie, v.prix_jour ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + cars.size() + " vehicles from database");
        } catch (SQLException e) {
            System.err.println("❌ Error getting all vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    /**
     * Get available cars
     */
    public List<Car> getAvailableCars() {
        List<Car> cars = new ArrayList<>();
        String query = "SELECT v.*, cl.nom_compagnie " +
                      "FROM vehicules v " +
                      "LEFT JOIN compagnies_location cl ON v.compagnie_id = cl.compagnie_id " +
                      "WHERE v.is_available = 1 " +
                      "ORDER BY v.categorie, v.prix_jour ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }
            System.out.println("✅ Retrieved " + cars.size() + " available vehicles");
        } catch (SQLException e) {
            System.err.println("❌ Error getting available vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    /**
     * Search cars by category
     */
    public List<Car> searchByCategory(Car.Categorie category) {
        List<Car> cars = new ArrayList<>();
        String query = "SELECT v.*, cl.nom_compagnie " +
                      "FROM vehicules v " +
                      "LEFT JOIN compagnies_location cl ON v.compagnie_id = cl.compagnie_id " +
                      "WHERE v.categorie = ? AND v.is_available = 1 " +
                      "ORDER BY v.prix_jour ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, category.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }
            System.out.println("✅ Found " + cars.size() + " " + category + " vehicles");
        } catch (SQLException e) {
            System.err.println("❌ Error searching vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return cars;
    }

    /**
     * Search cars by multiple criteria
     */
    public List<Car> searchCars(Car.Categorie category, Double maxPrice, Car.Transmission transmission) {
        List<Car> cars = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT v.*, cl.nom_compagnie " +
            "FROM vehicules v " +
            "LEFT JOIN compagnies_location cl ON v.compagnie_id = cl.compagnie_id " +
            "WHERE v.is_available = 1 "
        );

        boolean hasCategory = category != null;
        boolean hasPrice = maxPrice != null && maxPrice > 0;
        boolean hasTransmission = transmission != null;

        if (hasCategory) {
            query.append("AND v.categorie = ? ");
        }
        if (hasPrice) {
            query.append("AND v.prix_jour <= ? ");
        }
        if (hasTransmission) {
            query.append("AND v.transmission = ? ");
        }

        query.append("ORDER BY v.prix_jour ASC");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (hasCategory) {
                stmt.setString(paramIndex++, category.name());
            }
            if (hasPrice) {
                stmt.setDouble(paramIndex++, maxPrice);
            }
            if (hasTransmission) {
                stmt.setString(paramIndex++, transmission.name());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cars.add(extractCarFromResultSet(rs));
            }

            System.out.println("✅ Found " + cars.size() + " vehicles matching criteria");
        } catch (SQLException e) {
            System.err.println("❌ Error searching vehicles: " + e.getMessage());
            e.printStackTrace();
        }

        return cars;
    }

    /**
     * Extract Car object from ResultSet
     */
    private Car extractCarFromResultSet(ResultSet rs) throws SQLException {
        Car car = new Car();

        car.setVehiculeId(rs.getInt("vehicule_id"));
        car.setCompagnieId(rs.getInt("compagnie_id"));
        car.setMarque(rs.getString("marque"));
        car.setModele(rs.getString("modele"));

        Integer annee = rs.getInt("annee");
        if (!rs.wasNull()) {
            car.setAnnee(annee);
        }

        String categorie = rs.getString("categorie");
        if (categorie != null) {
            car.setCategorie(Car.Categorie.valueOf(categorie));
        }

        String transmission = rs.getString("transmission");
        if (transmission != null) {
            car.setTransmission(Car.Transmission.valueOf(transmission));
        }

        String carburant = rs.getString("carburant");
        if (carburant != null) {
            car.setCarburant(Car.Carburant.valueOf(carburant));
        }

        car.setNombrePlaces(rs.getInt("nombre_places"));

        Integer portes = rs.getInt("nombre_portes");
        if (!rs.wasNull()) {
            car.setNombrePortes(portes);
        }

        car.setClimatisation(rs.getBoolean("climatisation"));
        car.setGps(rs.getBoolean("gps"));
        car.setImageUrl(rs.getString("image_url"));

        BigDecimal prixJour = rs.getBigDecimal("prix_jour");
        if (prixJour != null) {
            car.setPrixJour(prixJour);
        }

        BigDecimal caution = rs.getBigDecimal("caution");
        if (caution != null) {
            car.setCaution(caution);
        }

        car.setKilometrageIllimite(rs.getBoolean("kilometrage_illimite"));
        car.setAvailable(rs.getBoolean("is_available"));

        // Joined data
        car.setCompagnieName(rs.getString("nom_compagnie"));

        return car;
    }
}
