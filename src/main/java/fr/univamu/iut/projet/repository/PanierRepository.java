package fr.univamu.iut.projet.repository;

import fr.univamu.iut.projet.entity.Panier;
import fr.univamu.iut.projet.entity.PanierProduit;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PanierRepository implements Closeable {

    protected Connection dbConnection;
    private final String dbUrl = "jdbc:mariadb://mysql-architecture-exam.alwaysdata.net/architecture-exam_paniers";
    private final String dbUser = "398207";
    private final String dbPassword = "lechatrouge13";

    public PanierRepository() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to connect to database!", e);
        }
    }

    @Override
    public void close() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    private Panier mapResultSetToPanier(ResultSet rs) throws SQLException {
        Integer panierId = rs.getInt("panier_id");
        Timestamp lastUpdateDate = rs.getTimestamp("date_maj");
        return new Panier(panierId, lastUpdateDate);
    }

    private PanierProduit mapResultSetToPanierProduit(ResultSet rs) throws SQLException {
        return new PanierProduit(
                rs.getInt("panier_produit_id"),
                rs.getInt("panier_id"),
                rs.getString("product_id"),
                rs.getInt("quantity"),
                rs.getString("unit")
        );
    }


    public Panier findById(Integer id) {
        String query = "SELECT * FROM paniers WHERE panier_id=?";
        Panier panier = null;
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                panier = mapResultSetToPanier(rs);
                panier.setPanierProduits(findPanierProduitsByPanierId(id)); // Load associated products
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Panier by id: " + id, e);
        }
        return panier;
    }

    public List<Panier> findAll() {
        String query = "SELECT * FROM paniers";
        List<Panier> paniers = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Panier panier = mapResultSetToPanier(rs);
                panier.setPanierProduits(findPanierProduitsByPanierId(panier.getPanierId())); // Load associated products
                paniers.add(panier);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Paniers", e);
        }
        return paniers;
    }

    private List<PanierProduit> findPanierProduitsByPanierId(Integer panierId) {
        String query = "SELECT * FROM panier_produits WHERE panier_id=?";
        List<PanierProduit> panierProduits = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                panierProduits.add(mapResultSetToPanierProduit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding PanierProduits for panierId: " + panierId, e);
        }
        return panierProduits;
    }


    public Panier save(Panier panier) {
        if (panier.getPanierId() == null) {
            return insert(panier);
        } else {
            return update(panier);
        }
    }

    private Panier insert(Panier panier) {
        String query = "INSERT INTO paniers (date_maj) VALUES (NOW())"; // Use NOW() for current timestamp
        try (PreparedStatement ps = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating panier failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    panier.setPanierId(generatedId);
                    updatePanierProducts(panier); // Insert/Update PanierProduits
                    return findById(generatedId); // Retrieve the complete panier with products
                } else {
                    throw new SQLException("Creating panier failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting Panier", e);
        }
    }


    private Panier update(Panier panier) {
        String query = "UPDATE paniers SET date_maj = NOW() WHERE panier_id = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panier.getPanierId());
            ps.executeUpdate();
            updatePanierProducts(panier); // Update PanierProduits
            return findById(panier.getPanierId()); // Retrieve the complete panier with updated products
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Panier", e);
        }
    }

    private void updatePanierProducts(Panier panier) {
        if (panier.getPanierProduits() != null) {
            // Delete existing panier products for this panier and re-insert
            deletePanierProductsForPanierId(panier.getPanierId());
            for (PanierProduit product : panier.getPanierProduits()) {
                insertPanierProduct(panier.getPanierId(), product);
            }
        }
    }

    private void insertPanierProduct(Integer panierId, PanierProduit product) {
        String query = "INSERT INTO panier_produits (panier_id, product_id, quantity, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ps.setString(2, product.getProductId());
            ps.setInt(3, product.getQuantity());
            ps.setString(4, product.getUnit());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting PanierProduit for panierId: " + panierId, e);
        }
    }

    private void deletePanierProductsForPanierId(Integer panierId) {
        String query = "DELETE FROM panier_produits WHERE panier_id = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting PanierProduits for panierId: " + panierId, e);
        }
    }


    public void delete(Integer id) {
        String query = "DELETE FROM paniers WHERE panier_id=?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Panier by id: " + id, e);
        }
    }
}